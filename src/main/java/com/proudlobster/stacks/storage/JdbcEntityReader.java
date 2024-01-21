package com.proudlobster.stacks.storage;

import java.sql.ResultSet;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.ecp.Entity;
import com.proudlobster.stacks.structure.Configuration;
import com.proudlobster.stacks.structure.Couple;
import com.proudlobster.stacks.structure.Dictionary;

/**
 * An entity reader for a JDBC data source.
 */
@FunctionalInterface
public interface JdbcEntityReader extends EntityReader {

    public static JdbcEntityReader of(final Dictionary<Configuration> cl) {
        return of(JdbcConnectionProvider.of(cl));
    }

    public static JdbcEntityReader of(final JdbcConnectionProvider p) {
        return () -> p;
    }

    Fallible ERR_BADLY_FORMED_ENTITY = Fallible.of("Badly formed data for entity ''{0}''.");
    String QUERY_ENTITY = "SELECT ID, COMPONENT, STR_VALUE, LONG_VALUE FROM ENTITY_COMPONENT WHERE ACTIVE = TRUE AND ID = ?";
    String QUERY_IDS = "SELECT ID FROM ENTITY_COMPONENT WHERE ACTIVE = TRUE AND COMPONENT = ?";
    Fallible.RiskyFunction<ResultSet, Couple<Object>> RESULTS_TO_ENTITY = rs -> Couple.of(rs.getString("COMPONENT"),
            Stream.of(rs.getString("STR_VALUE"), rs.getLong("LONG_VALUE"), true).filter(Objects::nonNull).findFirst()
                    .orElse(true));

    /**
     * @return the JDBC connection provider referenced by this reader
     */
    JdbcConnectionProvider provider();

    /**
     * @param r a record describing the entities to read
     * @return the entities described by the record
     */
    default Stream<Entity> read(final EntityReader.Record r) {
        return r.identifier().map(Stream::of).orElse(r.component().map(this::readIdsForComponent).orElse(Stream.of()))
                .map(this::read).filter(Optional::isPresent).map(Optional::get);
    }

    /**
     * @param o the component name to read entity IDs for
     * @return the IDs of entities with that component
     */
    default Stream<Long> readIdsForComponent(final String o) {
        return provider().executeQuery(QUERY_IDS, rs -> rs.getLong("ID"), o);
    }

    @Override
    default Optional<Entity> read(final Long id) {
        return Optional.of(provider().executeQuery(QUERY_ENTITY, RESULTS_TO_ENTITY, id).collect(Collectors
                .toMap(c -> c.firstOf(String.class).orElseThrow(ERR_BADLY_FORMED_ENTITY.apply(id)), Couple::second)))
                .filter(a -> a.size() > 0).map(a -> (Entity) () -> Dictionary.of(a));
    }

}
