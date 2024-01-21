package com.proudlobster.stacks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.Entity;
import com.proudlobster.stacks.ecp.ManagedEntity;
import com.proudlobster.stacks.ecp.ManagedProcessor;
import com.proudlobster.stacks.ecp.ManagedTransaction;
import com.proudlobster.stacks.ecp.Transaction;
import com.proudlobster.stacks.storage.EntityReader;
import com.proudlobster.stacks.storage.EntityWriter;
import com.proudlobster.stacks.storage.InMemoryStorage;
import com.proudlobster.stacks.storage.JdbcEntityReader;
import com.proudlobster.stacks.storage.JdbcEntityWriter;
import com.proudlobster.stacks.structure.Configuration;
import com.proudlobster.stacks.structure.Dictionary;
import com.proudlobster.stacks.structure.Librarian;
import com.proudlobster.stacks.structure.Library;
import com.proudlobster.stacks.structure.Single;
import com.proudlobster.stacks.structure.Tuple;
import com.proudlobster.stacks.utility.IdentifierSequence;

/**
 * Primary library interface.
 */
@FunctionalInterface
public interface Stacks extends Managed {

    /**
     * @return a new instance of Stacks, configured according to stacks.properties
     */
    public static Stacks create() {
        return create(Dictionary.of(Configuration.ofProperties("stacks")
                .collect(Collectors.toMap(Configuration::name, Function.identity()))));
    }

    /**
     * @param conf configuration to use for the Stacks instance
     * @return a new instance of Stacks, configured according to the provided
     *         dictionary
     */
    public static Stacks create(final Dictionary<Configuration> conf) {
        final Librarian lib = Librarian.create();
        lib.registerDictionary(Configuration.class);
        lib.registerDictionary(ManagedProcessor.class);
        Configuration.ofProperties("default")
                .map(e -> conf.lookup(e.name()).map(Configuration::value).map(f -> e.overlay(f)).orElse(e))
                .forEach(c -> lib.registerEntry(Configuration.class, c.name(), c));
        return create(lib);
    }

    /**
     * @param l librarian (with configuration loaded) to use for the Stacks instance
     * @return a new instance of Stacks, configured according to the provided
     *         librarian
     */
    public static Stacks create(final Librarian l) {
        final InMemoryStorage storage = InMemoryStorage.of();
        final Dictionary<Configuration> c = l.accessLibrary().lookup(Configuration.class);
        return create(Transaction.Lock.create(), IdentifierSequence.create(), l, buildEntityReader(c, storage),
                buildEntityWriter(c, storage));
    }

    /**
     * @param lock      the transaction lock to use
     * @param sequence  the ID sequence to use
     * @param librarian the librarian to use
     * @param reader    the entity reader to use
     * @param writer    the entity writer to use
     * @return a new instance of Stacks using all of the provided components
     */
    public static Stacks create(final Transaction.Lock lock, final IdentifierSequence sequence,
            final Librarian librarian, final EntityReader reader, final EntityWriter writer) {
        return () -> () -> Stream.of(lock, sequence, librarian, reader, writer);
    }

    private static EntityReader buildEntityReader(final Dictionary<Configuration> c, final InMemoryStorage s) {
        final List<EntityReader> ers = new ArrayList<>();
        if (c.lookup("stacks.entity.storage.inmemory.reader.enabled").map(Configuration::value).filter("true"::equals)
                .isPresent()) {
            ers.add(s);
        }
        if (c.lookup("stacks.entity.storage.jdbc.reader.enabled").map(Configuration::value).filter("true"::equals)
                .isPresent()) {
            ers.add(JdbcEntityReader.of(c));
        }
        return EntityReader.of(ers.toArray(new EntityReader[0]));
    }

    private static EntityWriter buildEntityWriter(final Dictionary<Configuration> c, final InMemoryStorage s) {
        final List<EntityWriter> ews = new ArrayList<>();
        if (c.lookup("stacks.entity.storage.inmemory.writer.enabled").map(Configuration::value).filter("true"::equals)
                .isPresent()) {
            ews.add(s);
        }
        if (c.lookup("stacks.entity.storage.jdbc.writer.enabled").map(Configuration::value).filter("true"::equals)
                .isPresent()) {
            ews.add(JdbcEntityWriter.of(c));
        }
        return EntityWriter.of(ews.toArray(new EntityWriter[0]));
    }

    /**
     * @return the internal modules of the Stacks instance
     */
    Tuple<Object> delegate();

    /**
     * @return a new unmanaged transaction
     */
    default Transaction startUnmanagedTransaction() {
        return Transaction.start(delegate().requiredFirstOf(Transaction.Lock.class));
    }

    /**
     * @return a new managed transaction
     */
    default ManagedTransaction startTransaction() {
        return ManagedTransaction.of(startUnmanagedTransaction(), this);
    }

    /**
     * @param t a transaction to commit to this instance's writer
     */
    default void commitTransaction(final Transaction t) {
        t.commit(delegate().requiredFirstOf(EntityWriter.class));
    }

    /**
     * @return this instance's entity reader
     */
    default EntityReader entityReader() {
        return delegate().requiredFirstOf(EntityReader.class);
    }

    /**
     * @param l the ID of the entity to look up
     * @return the unmanaged entity, if it exists in the instance's entity reader
     */
    default Optional<Entity> lookupUnmanagedEntity(final Long l) {
        return entityReader().read(l);
    }

    /**
     * @param ls the IDs of the entities to look up
     * @return the unmanaged entities found in the instance's entity reader
     */
    default Stream<Entity> lookupUnmanagedEntities(final Long[] ls) {
        return Arrays.stream(ls).map(this::lookupUnmanagedEntity).filter(Optional::isPresent).map(Optional::get);
    }

    /**
     * @param cs the components belonging to the entities to look up
     * @return the unmanaged entities which possess all given components in the
     *         instance's entity reader
     */
    default Stream<Entity> lookupUnmanagedEntities(final Component[] cs) {
        return entityReader().read(cs);
    }

    /**
     * @param l the ID of the entity to look up
     * @return the managed entity found in the instance's entity reader
     */
    default Optional<ManagedEntity> lookupManagedEntity(final Long l) {
        return lookupUnmanagedEntity(l).map(e -> ManagedEntity.of(e, this));
    }

    /**
     * @param ls the IDs of the entities to look up
     * @return the managed entities found in the instance's entity reader
     */
    default Stream<ManagedEntity> lookupManagedEntities(final Long[] ls) {
        return Arrays.stream(ls).map(this::lookupManagedEntity).filter(Optional::isPresent).map(Optional::get);
    }

    /**
     * @param cs the components belonging to the entities to look up
     * @return the managed entities which possess all given components in the
     *         instance's entity reader
     */
    default Stream<ManagedEntity> lookupManagedEntities(final Component[] cs) {
        return lookupUnmanagedEntities(cs).map(e -> ManagedEntity.of(e, this));
    }

    /**
     * @return the instance's library
     */
    default Library library() {
        return librarian().accessLibrary();
    }

    /**
     * @return the instance's librarian
     */
    default Librarian librarian() {
        return delegate().requiredFirstOf(Librarian.class);
    }

    /**
     * @return the next available entity ID for the instance
     */
    default Long nextId() {
        return delegate().requiredFirstOf(IdentifierSequence.class).get();
    }

    /**
     * @param n the name of the processor to get status for
     * @return a supplier of the processor's active state
     */
    default Supplier<Boolean> processorStatus(final String n) {
        return () -> $(Configuration.class, PROC_ACTIVE_CONF_NAME.get(n)).map(Configuration::value)
                .map(Boolean::parseBoolean).orElse(Boolean.FALSE);
    }

    /**
     * @param n the name of the processor
     * @param s the new status for the processor
     * @return a supplier of the processor's active state
     */
    default Supplier<Boolean> setProcessorStatus(final String n, final Boolean s) {
        stacks().librarian().registerEntry(Configuration.class, PROC_ACTIVE_CONF_NAME.get(n),
                Configuration.of(n, s.toString()));
        return processorStatus(n);
    }

    /**
     * @return names of all registered (not necessarily active) processors
     */
    default Stream<String> processorNames() {
        return Arrays.stream($(Configuration.class, PROC_CONF).orElseThrow(ERR_NO_PROC_CONF).value().split(","));
    }

    /*
     * Side-effect: Run all active and registered processors once.
     */
    default void runProcessors() {
        processorNames().filter(s -> processorStatus(s).get()).map(s -> $(ManagedProcessor.class, s))
                .filter(Optional::isPresent).map(Optional::get).map(ManagedProcessor::process)
                .reduce(ManagedTransaction::compose).ifPresent(ManagedTransaction::commit);
    }

    @Override
    default Tuple<Object> managedDelegate() {
        return Single.of(this);
    }

}
