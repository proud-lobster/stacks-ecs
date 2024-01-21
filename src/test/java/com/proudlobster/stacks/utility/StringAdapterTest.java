package com.proudlobster.stacks.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class StringAdapterTest implements TestContstants {

    // BEGIN STRING DATA
    private static final String SPLITTABLE_STRING = "foo|5|bar|1001";
    private static final String NUMERIC_STRING = "999";
    private static final String NON_NUMERIC_STRING = "a5";

    // BEGIN ADAPTERS
    private static final StringAdapter SPLITTABLE_ADAPTER = StringAdapter.of(SPLITTABLE_STRING);
    private static final StringAdapter NUMERIC_ADAPTER = StringAdapter.of(NUMERIC_STRING);
    private static final StringAdapter NON_NUMERIC_ADAPTER = StringAdapter.of(NON_NUMERIC_STRING);

    // BEGIN TEST RESULTS
    private static final String STRING_1 = "foo";
    private static final String STRING_2 = "5";
    private static final String STRING_3 = "bar";
    private static final String STRING_4 = "1001";
    private static final Long NUMERIC_NUMBER = 999L;
    private static final Long NUMBER_1 = 5L;
    private static final Long NUMBER_2 = 1001L;
    private static final Long NUMERIC_COUNT = 2L;

    @Test
    @DisplayName("Split returns correct adapters")
    public void split_correctAdapters() {
        final List<String> parts = SPLITTABLE_ADAPTER.split().map(StringAdapter::string).collect(Collectors.toList());
        assertEquals(STRING_1, parts.get(0));
        assertEquals(STRING_2, parts.get(1));
        assertEquals(STRING_3, parts.get(2));
        assertEquals(STRING_4, parts.get(3));
    }

    @Test
    @DisplayName("Number converts numeric string correctly")
    public void number_convertsCorrectly() {
        assertEquals(NUMERIC_NUMBER, NUMERIC_ADAPTER.number().get());
    }

    @Test
    @DisplayName("Number does not convert non-numeric string")
    public void number_doesNotConvert() {
        assertTrue(NON_NUMERIC_ADAPTER.number().isEmpty());
    }

    @Test
    @DisplayName("Split to numbers returns correct numbers")
    public void splitToNumbers_correctNumbers() {
        final List<Long> parts = SPLITTABLE_ADAPTER.splitToNumbers().collect(Collectors.toList());
        assertEquals(NUMBER_1, parts.get(0));
        assertEquals(NUMBER_2, parts.get(1));
    }

    @Test
    @DisplayName("Split to numbers ignores non-numeric strings")
    public void splitToNumbers_ignoreStrings() {
        assertEquals(NUMERIC_COUNT, SPLITTABLE_ADAPTER.splitToNumbers().count());
    }
}
