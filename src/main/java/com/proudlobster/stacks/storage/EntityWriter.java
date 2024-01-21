package com.proudlobster.stacks.storage;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.ecp.Component;

/**
 * Used to write entity data to storage of some kind.
 */
@FunctionalInterface
public interface EntityWriter extends Consumer<EntityWriter.Record> {

    /**
     * Data structure for writing.
     */
    @FunctionalInterface
    static interface Record extends EntityStorageDescriptor {

        /**
         * @param id the ID of the entity
         * @param c  the component to affect
         * @param s  the string value of the component
         * @param l  the long value of the component
         * @param a  if true then the data in the other fields should be written to the
         *           entity and if false then the named component should be removed from
         *           the entity
         * @return a record representing the above changes for writing
         */
        public static Record of(final Long id, final String c, final Optional<String> s, final Optional<Long> l,
                final boolean a) {
            return () -> EntityStorageDescriptor.of(Optional.of(id), Optional.of(c), s, l, a).stream();
        }

        Fallible ERR_NO_IDENTITY = Fallible.of("No entity identity associated with this record.");
        Fallible ERR_NO_COMPONENT = Fallible.of("No component associated with this record.");

        /**
         * @return the identifier for the record entity
         */
        default Long requiredIdentifier() {
            return firstOf(Long.class).orElseThrow(ERR_NO_IDENTITY);
        }

        /**
         * @return the component name
         */
        default String requiredComponent() {
            return firstOf(String.class).orElseThrow(ERR_NO_COMPONENT);
        }

    }

    Boolean DELETE_VAL = false;
    Boolean ACTIVE_VAL = true;

    /**
     * @param ews entity writers to compose
     * @return an entity writer composed of the others
     */
    public static EntityWriter of(final EntityWriter... ews) {
        return r -> Stream.of(ews).reduce((ew1, ew2) -> (EntityWriter) d -> ew1.andThen(ew2).accept(d))
                .ifPresent(w -> w.accept(r));
    }

    /**
     * @param r the record to write to storage
     */
    void write(final Record r);

    @Override
    default void accept(final Record r) {
        write(r);
    }

    /**
     * @param id the ID of the entity to write
     */
    default void writeEntity(final Long id) {
        writeAssignComponent(id, Component.Core.IDENTITY.name(), id);
    }

    /**
     * @param id the ID of the entity to write
     * @param c  the component name to write
     * @param v  the value to write
     */
    default void writeAssignComponent(final Long id, final String c, final String v) {
        write(Record.of(id, c, Optional.ofNullable(v), Optional.empty(), ACTIVE_VAL));
    }

    /**
     * @param id the ID of the entity to write
     * @param c  the component name to write
     * @param v  the value to write
     */
    default void writeAssignComponent(final Long id, final String c, final Long v) {
        write(Record.of(id, c, Optional.empty(), Optional.ofNullable(v), ACTIVE_VAL));
    }

    /**
     * @param id the ID of the entity to write
     * @param c  the flag component name to write
     */
    default void writeAssignComponent(final Long id, final String c) {
        write(Record.of(id, c, Optional.empty(), Optional.empty(), ACTIVE_VAL));
    }

    /**
     * @param id the entity to write as transient
     */
    default void writeTransientEntity(final long id) {
        ((Consumer<Long>) i -> writeAssignComponent(id, Component.Core.TRANSIENT.name())).andThen(this::writeEntity)
                .accept(id);
    }

    /**
     * @param id the entity to remove a component from
     * @param c  the component name to remove
     */
    default void writeRemoveComponent(final long id, final String c) {
        write(Record.of(id, c, Optional.empty(), Optional.empty(), DELETE_VAL));
    }

}
