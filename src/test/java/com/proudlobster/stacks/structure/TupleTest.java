package com.proudlobster.stacks.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.function.Predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.TestContstants;
import com.proudlobster.stacks.Fallible.StacksException;

@Tag("Unit")
public class TupleTest implements TestContstants {

    // BEGIN TUPLE CONTENTS
    private static final Object[] NON_EMPTY_CONTENTS = { STRING_VALUE_1, LONG_VALUE_1 };

    // BEGIN TUPLES
    private static final Tuple<Object> EMPTY = Tuple.of();
    private static final Tuple<Object> NON_EMPTY = Tuple.of(NON_EMPTY_CONTENTS);
    private static final Tuple<Tuple<Object>> TUPLE_OF_TUPLES = Tuple.of(NON_EMPTY);

    // BEGIN OTHER CONSTANTS
    private static final int EMPTY_SIZE = 0;
    private static final int BASE_INDEX = 0;
    private static final int HIGH_INDEX = 100;

    // BEGIN MISC
    private static final Predicate<Object> PRESENT_IN_NON_EMPTY = o -> Arrays.asList(NON_EMPTY_CONTENTS).contains(o);

    // stream()

    @Test
    @DisplayName("Non-empty tuple has non-empty stream")
    void stream_nonEmpty() {
        assertTrue(NON_EMPTY.stream().findAny().isPresent());
    }

    @Test
    @DisplayName("Stream contains elements")
    void stream_contentsMatch() {
        assertTrue(NON_EMPTY.stream().allMatch(PRESENT_IN_NON_EMPTY));
    }

    // get(i)

    @Test
    @DisplayName("Empty tuple returns empty optionals")
    void get_emptyAnyEmpty() {
        assertTrue(EMPTY.get(BASE_INDEX).isEmpty());
    }

    @Test
    @DisplayName("Non-empty tuple returns empty optional when missing")
    void get_nonEmptyEmptyWhenMissing() {
        assertTrue(NON_EMPTY.get(HIGH_INDEX).isEmpty());
    }

    @Test
    @DisplayName("Non-empty tuple returns present optional")
    void get_nonEmptyBasePresent() {
        assertTrue(NON_EMPTY.get(BASE_INDEX).isPresent());
    }

    // head()
    @Test
    @DisplayName("Head of empty is empty")
    void head_emptyEmpty() {
        assertTrue(EMPTY.head().isEmpty());
    }

    @Test
    @DisplayName("Head of non-empty is present")
    void head_nonEmptyPresent() {
        assertTrue(NON_EMPTY.head().isPresent());
    }

    @Test
    @DisplayName("Head of non-empty is first element")
    void head_equalsFirstElement() {
        assertEquals(STRING_VALUE_1, NON_EMPTY.head().orElse(null));
    }

    // tail()
    @Test
    @DisplayName("Tail of empty is empty")
    void tail_emptyEmpty() {
        assertTrue(EMPTY.tail().isEmpty());
    }

    @Test
    @DisplayName("Tail of non-empty is present")
    void tail_nonEmptyPresent() {
        assertTrue(NON_EMPTY.tail().isPresent());
    }

    @Test
    @DisplayName("Tail of non-empty is last element")
    void tail_equalsLastElement() {
        assertEquals(LONG_VALUE_1, NON_EMPTY.tail().orElse(null));
    }

    // morph(c)
    @Test
    @DisplayName("Tuple of type can be morphed to Tuple of another type")
    void morph_tuple() {
        assertTrue(NON_EMPTY.morph(String.class).get(BASE_INDEX).map(s -> s instanceof String).orElse(false));
    }

    @Test
    @DisplayName("Tuple of morphed type does not contain elements of other type")
    void morph_excludesOtherTypes() {
        assertTrue(NON_EMPTY.morph(String.class).stream().filter(c -> !(c instanceof String)).findAny().isEmpty());
    }

    @Test
    @DisplayName("Tuple of Tuples of type can be morphed")
    void morph_tupleOfTuples() {
        assertTrue(TUPLE_OF_TUPLES.morph(Tuple.class).get(BASE_INDEX).map(s -> s instanceof Tuple).orElse(false));
    }

    // firstOf(c)
    @Test
    @DisplayName("Tuple of Tuples can resolve base object")
    void firstOf_fullyResolves() {
        assertEquals(STRING_VALUE_1,
                ((Tuple<?>) TUPLE_OF_TUPLES.firstOf(Tuple.class).get()).firstOf(Object.class).get());
    }

    // count()
    @Test
    @DisplayName("Count of empty tuple is zero")
    void count_emptyZero() {
        assertEquals(EMPTY_SIZE, EMPTY.count());
    }

    @Test
    @DisplayName("Count of non-empty tuple is greater than zero")
    void count_nonEmptyGreaterThanZero() {
        assertTrue(EMPTY_SIZE < NON_EMPTY.count());
    }

    // requiredFirstOf
    @Test
    @DisplayName("Required first of element gets the element")
    void requiredFirstOf_fullyResolves() {
        assertEquals(LONG_VALUE_1, NON_EMPTY.requiredFirstOf(Long.class));
    }

    @Test
    @DisplayName("Required first of element fails for no element")
    void requiredFirstOf_fails() {
        assertThrows(StacksException.class, () -> EMPTY.requiredFirstOf(String.class));
    }
}
