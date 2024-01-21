package com.proudlobster.stacks.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class LibrarianTest implements TestContstants {

    // BEGIN LIBRARIES
    private static final Librarian SOME_LIBRARIAN = Librarian.create();

    @BeforeEach
    public void reset() {
        SOME_LIBRARIAN.map().clear();
    }

    @Test
    @DisplayName("Registering a dictionary makes that dictionary accessible")
    public void registerDictionary_nameAccessible() {
        SOME_LIBRARIAN.registerDictionary(STRING_VALUE_1);
        assertTrue(SOME_LIBRARIAN.accessLibrary().lookup(STRING_VALUE_1).isPresent());
    }

    @Test
    @DisplayName("Registering a dictionary by class makes a dictionary with that class name accessible")
    public void registerDictionary_classNameAccessible() {
        SOME_LIBRARIAN.registerDictionary(String.class);
        assertTrue(SOME_LIBRARIAN.accessLibrary().lookup(String.class.getName()).isPresent());
    }

    @Test
    @DisplayName("Registering an entry makes that entry available")
    public void registerEntry_entryAccessible() {
        SOME_LIBRARIAN.registerDictionary(STRING_VALUE_1);
        SOME_LIBRARIAN.registerEntry(STRING_VALUE_1, STRING_VALUE_2, LONG_VALUE_1);
        assertEquals(LONG_VALUE_1, SOME_LIBRARIAN.accessLibrary().lookup(STRING_VALUE_1, STRING_VALUE_2).get());
    }

    @Test
    @DisplayName("Registering an entry by class makes that entry available")
    public void registerEntry_classEntryAccessible() {
        SOME_LIBRARIAN.registerDictionary(LONG_VALUE_1.getClass());
        SOME_LIBRARIAN.registerEntry(LONG_VALUE_1.getClass(), STRING_VALUE_2, LONG_VALUE_1);
        assertEquals(LONG_VALUE_1,
                SOME_LIBRARIAN.accessLibrary().lookup(LONG_VALUE_1.getClass(), STRING_VALUE_2).get());
    }
}
