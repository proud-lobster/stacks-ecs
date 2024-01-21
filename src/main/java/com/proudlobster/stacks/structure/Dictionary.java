package com.proudlobster.stacks.structure;

import java.util.Map;
import java.util.Optional;

import com.proudlobster.stacks.Fallible;

/**
 * Represents a repository of elements that can be looked up with a String key.
 */
@FunctionalInterface
public interface Dictionary<E> {

    Fallible ERR_REQUIRED_NOT_FOUND = Fallible.of("Required entry ''{0}'' not found.");

    /**
     * @param <E> the type of the dictionary element stored
     * @param m   the map of dictionary elements
     * @return the dictionary for that map of elements
     */
    public static <E> Dictionary<E> of(final Map<String, E> m) {
        return k -> Optional.ofNullable(m.get(k));
    }

    /**
     * @param k the key of the element to look up
     * @return the element referenced by the key
     */
    Optional<E> lookup(final String k);

    /**
     * @param k the key of the element to look up
     * @return the element referenced by the key
     */
    default E require(final String k) {
        return lookup(k).orElseThrow(ERR_REQUIRED_NOT_FOUND.apply(k));
    }

    /**
     * @param <F> the type of the element to look up
     * @param k   the key of the element to look up
     * @param c   the type reference of the element to look up
     * @return the element referenced by the given key and type
     */
    default <F extends E> Optional<F> lookup(final String k, final Class<F> c) {
        return lookup(k).filter(o -> c.isInstance(o)).map(o -> c.cast(o));
    }

}
