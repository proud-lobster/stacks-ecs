package com.proudlobster.stacks;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.ManagedEntity;
import com.proudlobster.stacks.ecp.ManagedProcessor;
import com.proudlobster.stacks.ecp.ManagedTransaction;
import com.proudlobster.stacks.ecp.ManagedProcessor.ManagedProcessorFunction;
import com.proudlobster.stacks.structure.Configuration;
import com.proudlobster.stacks.structure.Tuple;
import com.proudlobster.stacks.utility.StringTemplate;

/**
 * Used to wrap an object with an instance of Stacks.
 */
@FunctionalInterface
public interface Managed {

    String PROC_CONF = "stacks.processor.list";
    Fallible ERR_NOT_MANAGED = Fallible.of("Stacks cannot be found.  This is not a managed instance.");
    Fallible ERR_NO_INSTANCE = Fallible.of("No managed instance found.");
    Fallible ERR_NO_PROC_CONF = Fallible.of("Missing configuration for '" + PROC_CONF + "'.");
    StringTemplate PROC_ACTIVE_CONF_NAME = StringTemplate.of("stacks.processor.{0}.active");

    /**
     * @return the internal modules of the managed object
     */
    Tuple<Object> managedDelegate();

    /**
     * @return the wrapped Stacks instance
     */
    default Stacks stacks() {
        return managedDelegate().firstOf(Stacks.class).orElseThrow(ERR_NOT_MANAGED.apply(this));
    }

    /**
     * @param <E> the type of the original wrapped object
     * @param c   the type reference of the original wrapped object
     * @return the original wrapped object
     */
    default <E> E instance(final Class<E> c) {
        return managedDelegate().firstOf(c).orElseThrow(ERR_NO_INSTANCE.apply(this));
    }

    /**
     * @return a new managed transaction
     */
    default ManagedTransaction $() {
        return stacks().startTransaction();
    }

    /**
     * @param c  at least one component to find entities matching
     * @param cs additional components to match
     * @return the stream of managed entities matching those components
     */
    default Stream<ManagedEntity> $(final Component c, final Component... cs) {
        return $(Stream.of(cs, new Component[] { c }).flatMap(Stream::of).toArray(Component[]::new));
    }

    /**
     * @param cs components to find entities matching
     * @return the stream of managed entities matching those components
     */
    default Stream<ManagedEntity> $(final Component[] cs) {
        return stacks().lookupManagedEntities(cs);
    }

    /**
     * @param e  at least one entity ID to find the entities for
     * @param es additional entity IDs to find
     * @return the stream of managed entities for those IDs
     */
    default Stream<ManagedEntity> $(final Long e, final Long... es) {
        return $(Stream.of(es, new Long[] { e }).flatMap(Stream::of).toArray(Long[]::new));
    }

    /**
     * @param cs entity IDs to find the entities for
     * @return the stream of managed entities for those IDs
     */
    default Stream<ManagedEntity> $(final Long[] cs) {
        return stacks().lookupManagedEntities(cs);
    }

    /**
     * @param <T> the type of the library element to find
     * @param c   the type reference of the element to find
     * @param s   the index for the element to find
     * @return the element matching the type and index in the library
     */
    default <T> Optional<T> $(final Class<T> c, final String s) {
        return stacks().library().lookup(c, s);
    }

    /**
     * Side-effect: Registers a new processor.
     * 
     * @param n the processor's name to register
     * @param c the processor's component
     * @param f the processor's function
     * @return a supplier of the registered processor's active state
     */
    default Supplier<Boolean> $(final String n, final Component c, final ManagedProcessorFunction f) {
        stacks().librarian().registerEntry(ManagedProcessor.class, n, ManagedProcessor.of(c, f, stacks()));
        final String o = $(Configuration.class, PROC_CONF).orElseThrow(ERR_NO_PROC_CONF).value();
        stacks().librarian().registerEntry(Configuration.class, PROC_CONF, Configuration.of(PROC_CONF, o + "," + n));
        return stacks().setProcessorStatus(n, Boolean.TRUE);
    }

}
