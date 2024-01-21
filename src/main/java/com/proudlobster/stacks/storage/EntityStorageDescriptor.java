package com.proudlobster.stacks.storage;

import java.util.Optional;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.structure.Tuple;

/**
 * A structure used to describe entity data to be read or written
 */
public interface EntityStorageDescriptor extends Tuple<Object> {

    /**
     * @param id the ID of the entity
     * @param c  the component of the entity described
     * @param s  the string value of the component described
     * @param l  the long value of the component described
     * @param a  true if the entity is active, false otherwise
     * @return a record representing the described values
     */
    public static EntityStorageDescriptor of(final Optional<Long> id, final Optional<String> c,
            final Optional<String> s, final Optional<Long> l, final boolean a) {
        return () -> Stream.of(id.orElse(null), c.orElse(null), s.orElse(null), l.orElse(null), a);
    }

    Boolean DEFAULT_FLAG = true;

    Fallible ERR_NO_ACTIVE = Fallible.of("No active flag associated with this record.");

    /**
     * @return the identifier for the record entity
     */
    default Optional<Long> identifier() {
        return firstOf(Long.class);
    }

    /**
     * @return the component name
     */
    default Optional<String> component() {
        return firstOf(String.class);
    }

    /**
     * @return the string value
     */
    default Optional<String> stringValue() {
        return morph(String.class).get(1);
    }

    /**
     * @return the long value
     */
    default Optional<Long> longValue() {
        return morph(Long.class).get(1);
    }

    /**
     * @return the derived value described based on what is present
     */
    default Object value() {
        return stringValue().map(Object.class::cast)
                .orElse(longValue().map(Object.class::cast).orElse(DEFAULT_FLAG));
    }

    /**
     * @return the active value (if false then delete the entity component)
     */
    default Boolean active() {
        return firstOf(Boolean.class).orElseThrow(ERR_NO_ACTIVE);
    }
}
