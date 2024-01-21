package com.proudlobster.stacks.structure;

import java.util.Map;
import java.util.Optional;

import com.proudlobster.stacks.Fallible;

/*
 * A dictionary of dictionaries.
 */
@FunctionalInterface
public interface Library extends Dictionary<Dictionary<Object>> {

    Fallible ERR_NO_TYPE_DICTIONARY = Fallible.of("There is no dictionary of type ''{0}''.");

    /**
     * @param m the map used to back the library
     * @return a library wrapping the given map
     */
    public static Library of(final Map<String, Map<String, Object>> m) {
        return dn -> Optional.ofNullable(m.get(dn)).map(a -> Dictionary.of(a));
    }

    /**
     * @param k the highest level key to reference
     * @param l the key to reference in the inner dictionary
     * @return the element referenced by both keys
     */
    default Optional<Object> lookup(final String k, final String l) {
        return lookup(k).flatMap(o -> o.lookup(l));
    }

    /**
     * @param <E> the type of the element to look up
     * @param k   the highest level key to reference
     * @param l   the key to reference in the inner dictionary
     * @param c   the type reference of the element to look up
     * @return the element referenced by both keys and type
     */
    default <E extends Object> Optional<E> lookup(final String k, final String l, final Class<E> c) {
        return lookup(k).flatMap(i -> i.lookup(l, c)).filter(o -> c.isInstance(o)).map(o -> c.cast(o));
    }

    /**
     * @param <E> the type of the element to look up
     * @param c   the type reference of the element to look up wherein the name of
     *            that type is also used as the first key
     * @param l   the key to reference in the inner dictionary
     * @return the element referenced by both keys and type
     */
    default <E extends Object> Optional<E> lookup(final Class<E> c, final String l) {
        return lookup(c.getName(), l, c);
    }

    /**
     * @param <E> the type of the elements contained in the dictionary to look up
     * @param c   the type reference of the elements contained in the dictionary to
     *            look up
     * @return the dictionary containing elements of the given type
     */
    default <E extends Object> Dictionary<E> lookup(final Class<E> c) {
        return lookup(c.getName()).map(d -> (Dictionary<E>) (k -> d.lookup(k, c)))
                .orElseThrow(ERR_NO_TYPE_DICTIONARY.apply(c.getName()));
    }

}
