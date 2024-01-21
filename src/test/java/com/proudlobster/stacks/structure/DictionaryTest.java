package com.proudlobster.stacks.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class DictionaryTest implements TestContstants {

    // BEGIN DICTIONARY CONTENTS
    private static final String BAD_KEY = "bar";
    private static final Map<String, String> STRING_MAP = Map.of(KEY_1, STRING_VALUE_1);
    private static final Map<String, Object> MIXED_MAP = Map.of(KEY_1, STRING_VALUE_1, KEY_2, LONG_VALUE_1);

    // BEGIN DICTIONARIES
    private static final Dictionary<String> STRING_DICT = Dictionary.of(STRING_MAP);
    private static final Dictionary<Object> MIXED_DICT = Dictionary.of(MIXED_MAP);

    @Test
    @DisplayName("Lookup finds some keyed value")
    void lookup_findKeyedValue() {
        assertEquals(STRING_VALUE_1, STRING_DICT.lookup(KEY_1).get());
    }

    @Test
    @DisplayName("Lookup does not find value for bad key")
    void lookup_emptyBadKey() {
        assertTrue(STRING_DICT.lookup(BAD_KEY).isEmpty());
    }

    @Test
    @DisplayName("Lookup finds some keyed value with matching type")
    void lookup_findKeyedTypedValue() {
        assertEquals(LONG_VALUE_1, MIXED_DICT.lookup(KEY_2, Long.class).get());
    }

    @Test
    @DisplayName("Lookup does not find keyed value with unmatching type")
    void lookup_emptyKeyedBadType() {
        assertTrue(MIXED_DICT.lookup(KEY_2, String.class).isEmpty());
    }

    @Test
    @DisplayName("Lookup does not find value for bad key with type")
    void lookup_emptyBadKeyType() {
        assertTrue(STRING_DICT.lookup(BAD_KEY, String.class).isEmpty());
    }

    @Test
    @DisplayName("Require returns a present value")
    void require_findKeyedValue() {
        assertEquals(STRING_VALUE_1, STRING_DICT.require(KEY_1));
    }

    @Test
    @DisplayName("Require throws error for bad key")
    void require_badKeyError() {
        assertThrows(Fallible.StacksException.class, () -> STRING_DICT.require(BAD_KEY));
    }
}
