package com.proudlobster.stacks.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class LibraryTest implements TestContstants {

    // BEGIN LIBRARY CONTENTS
    private static final Map<String, Object> INNER_MAP_1 = Map.of(KEY_2, STRING_VALUE_1);
    private static final Map<String, Map<String, Object>> SOME_MAP = Map.of(KEY_1, INNER_MAP_1,
            STRING_VALUE_1.getClass().getName(), INNER_MAP_1);
    private static final Map<String, Map<String, Object>> TYPE_MAP = Map.of(String.class.getName(), INNER_MAP_1);

    // BEGIN LIBRARIES
    private static final Library EMPTY_LIB = Library.of(new HashMap<>());
    private static final Library SOME_LIB = Library.of(SOME_MAP);
    private static final Library TYPE_LIB = Library.of(TYPE_MAP);

    @Test
    @DisplayName("Empty library gives no value")
    void lookup_noValue() {
        assertTrue(EMPTY_LIB.lookup(KEY_1, KEY_2).isEmpty());
    }

    @Test
    @DisplayName("Lookup finds some keyed value")
    void lookup_someValue() {
        assertEquals(STRING_VALUE_1, SOME_LIB.lookup(KEY_1, KEY_2).get());
    }

    @Test
    @DisplayName("Lookup with class finds some keyed value")
    void lookup_someValueWithClass() {
        assertEquals(STRING_VALUE_1, SOME_LIB.lookup(KEY_1, KEY_2, STRING_VALUE_1.getClass()).get());
    }

    @Test
    @DisplayName("Lookup with wrong class does not find value")
    void lookup_noValueWithClass() {
        assertTrue(SOME_LIB.lookup(KEY_1, KEY_2, Long.class).isEmpty());
    }

    @Test
    @DisplayName("Lookup by class finds value of that class")
    void lookup_byClass() {
        assertEquals(STRING_VALUE_1, SOME_LIB.lookup(STRING_VALUE_1.getClass(), KEY_2).get());
    }

    @Test
    @DisplayName("Lookup dictionary by class finds the dictionary for that class name")
    void lookup_dictionaryByClass() {
        assertEquals(STRING_VALUE_1, TYPE_LIB.lookup(String.class).lookup(KEY_2).get());
    }

    @Test
    @DisplayName("Lookup dictionary by class throws error when no dictionary exists for class")
    void look_dictionaryByClassFailed() {
        assertThrows(Fallible.StacksException.class, () -> TYPE_LIB.lookup(Long.class));
    }
}
