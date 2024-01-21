package com.proudlobster.stacks.storage;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.Entity;

/**
 * Used to read entity data from storage.
 */
@FunctionalInterface
public interface EntityReader extends Function<Stream<EntityReader.Record>, Stream<Entity>> {

    /**
     * Data structure for a read request.
     */
    public static interface Record extends EntityStorageDescriptor {

        /**
         * @param id the ID of the entity
         * @param c  the component of the entity described
         * @param s  the string value of the component described
         * @param l  the long value of the component described
         * @param a  true if the entity is active, false otherwise
         * @return a record representing the described values
         */
        public static Record of(final Optional<Long> id, final Optional<String> c, final Optional<String> s,
                final Optional<Long> l) {
            return () -> EntityStorageDescriptor.of(id, c, s, l, true).stream();
        }

        /**
         * @param id the ID of the entity
         * @return a record describing that ID
         */
        public static Record of(final Long id) {
            return of(Optional.of(id), Optional.empty(), Optional.empty(), Optional.empty());
        }

        /**
         * @param c the component of the entity described
         * @return a record representing entities with that component
         */
        public static Record of(final String c) {
            return of(Optional.empty(), Optional.of(c), Optional.empty(), Optional.empty());
        }
    }

    /**
     * @param ers entity readers to compose
     * @return an entity reader composed of the others
     */
    public static EntityReader of(final EntityReader... ers) {
        return r -> Stream.of(ers).flatMap(er -> er.read(r))
                .collect(Collectors.toMap(Entity::identifier, Function.identity(), (e, t) -> e)).values().stream();
    }

    /**
     * @param r record to match
     * @return entities matching the identifier and/or component name of the record
     */
    Stream<Entity> read(final EntityReader.Record r);

    /**
     * @param r records to match
     * @return entities matching the identifiers and/or component names of the
     *         records
     */
    default Stream<Entity> read(final Stream<EntityReader.Record> rs) {
        return rs.map(this::read).flatMap(Function.identity());
    }

    /**
     * @param r record to match
     * @return entities matching the identifiers and/or component names of the
     *         records
     */
    default Stream<Entity> apply(final Stream<EntityReader.Record> rs) {
        return read(rs);
    }

    /**
     * @param id the ID of the entity to find
     * @return that entity
     */
    default Optional<Entity> read(final Long id) {
        return apply(Stream.of(Record.of(id))).findAny();
    }

    /**
     * @param cs components of the entities to find
     * @return the entities with those components
     */
    default Stream<Entity> read(final Component... cs) {
        return apply(Arrays.stream(cs).map(Component::name).map(Record::of))
                .filter(e -> Arrays.stream(cs).allMatch(e::is));
    }
}
