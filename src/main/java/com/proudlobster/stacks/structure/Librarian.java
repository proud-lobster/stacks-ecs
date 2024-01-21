package com.proudlobster.stacks.structure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.proudlobster.stacks.Fallible;

/**
 * A builder for Libraries.
 */
@FunctionalInterface
public interface Librarian {

    /**
     * @return a new librarian, with an internal map for managing the library
     */
    public static Librarian create() {
        final HashMap<String, Map<String, Object>> l = new HashMap<>();
        return () -> l;
    }

    Fallible ERR_NO_SUCH_DICTIONARY = Fallible.of("No dictionary with name ''{0}''.");

    /**
     * @return accessor to the internal library
     */
    Map<String, Map<String, Object>> map();

    /**
     * @param n the name to create a new dictionary for
     */
    default void registerDictionary(final String n) {
        map().computeIfAbsent(n, x -> new HashMap<>());
    }

    /**
     * @param c the type to create a new dictionary for
     */
    default void registerDictionary(final Class<?> c) {
        registerDictionary(c.getName());
    }

    /**
     * @param d the library-level key to register the entry for
     * @param n the dictionary-level key to register the entry for
     * @param e the entry to register
     */
    default void registerEntry(final String d, final String n, final Object e) {
        Optional.ofNullable(map().get(d)).orElseThrow(ERR_NO_SUCH_DICTIONARY.apply(d)).put(n, e);
    }

    /**
     * @param d the type to register the entry for
     * @param n the dictionary-level key to register the entry for
     * @param e the entry to register
     */
    default void registerEntry(final Class<?> d, final String n, final Object e) {
        registerEntry(d.getName(), n, e);
    }

    /**
     * @return the library managed by this librarian
     */
    default Library accessLibrary() {
        return Library.of(map());
    }

}
