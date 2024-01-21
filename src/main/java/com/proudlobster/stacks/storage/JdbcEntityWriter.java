package com.proudlobster.stacks.storage;

import java.util.Optional;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.structure.Configuration;
import com.proudlobster.stacks.structure.Dictionary;
import com.proudlobster.stacks.utility.ObjectAdapter;

/**
 * An entity writer for a JDBC data source.
 */
@FunctionalInterface
public interface JdbcEntityWriter extends EntityWriter {

    public static JdbcEntityWriter of(final Dictionary<Configuration> cl) {
        return of(JdbcConnectionProvider.of(cl));
    }

    public static JdbcEntityWriter of(final JdbcConnectionProvider p) {
        return () -> p;
    }

    String INSERT = "INSERT INTO ENTITY_COMPONENT (ID, COMPONENT, STR_VALUE, LONG_VALUE, ACTIVE) VALUES (?,?,?,?,?)";
    String UPDATE_STR = "UPDATE ENTITY_COMPONENT SET STR_VALUE = ? WHERE ID = ? AND COMPONENT = ?";
    String UPDATE_LONG = "UPDATE ENTITY_COMPONENT SET LONG_VALUE = ? WHERE ID = ? AND COMPONENT = ?";
    String DELETE = "UPDATE ENTITY_COMPONENT SET ACTIVE = false WHERE ID = ? AND COMPONENT = ?";
    String EXPIRE = "UPDATE ENTITY_COMPONENT SET ACTIVE = false WHERE ID = ?";
    Fallible ERR_NO_VALUE = Fallible.of("Record has no value to update.");

    /**
     * @return the JDBC connection provider referenced by this writer
     */
    JdbcConnectionProvider provider();

    /**
     * @return a reader for this writer
     */
    default JdbcEntityReader reader() {
        return () -> provider();
    }

    /**
     * @param r the record to write
     */
    default void write(final Record r) {
        Optional.of(r).filter(Record::active).ifPresentOrElse(this::upsert, () -> delete(r));
    }

    /**
     * @param r the record to update if it exists, or insert if it does not
     */
    default void upsert(final Record r) {
        Optional.of(r).map(Record::requiredIdentifier).map(reader()::read).filter(Optional::isPresent)
                .map(Optional::get).filter(e -> e.is(r.requiredComponent()))
                .ifPresentOrElse(e -> update(r), () -> insert(r));

        r.component().filter(c -> Component.Core.EXPIRED.name().equals(c))
                .ifPresent(c -> expire(r.requiredIdentifier()));
    }

    // TODO add test and doc for expiration
    default void expire(final Long id) {
        provider().executeStatement(EXPIRE, id);
    }

    /**
     * @param r the record to insert
     */
    default void insert(final Record r) {
        provider().executeStatement(INSERT, r.requiredIdentifier(), r.requiredComponent(), r.stringValue().orElse(null),
                r.longValue().orElse(null), true);
    }

    /**
     * @param r the record to update
     */
    default void update(final Record r) {
        provider().executeStatement(
                ObjectAdapter.ofFirst(r.stringValue().map(s -> UPDATE_STR), r.longValue().map(s -> UPDATE_LONG)).get(),
                r.value(), r.requiredIdentifier(), r.requiredComponent());
    }

    /**
     * @param r the record to delete
     */
    default void delete(final Record r) {
        provider().executeStatement(DELETE, r.requiredIdentifier(), r.requiredComponent());
    };
}
