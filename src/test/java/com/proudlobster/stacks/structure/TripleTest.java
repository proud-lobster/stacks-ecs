package com.proudlobster.stacks.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class TripleTest implements TestContstants {

    // BEGIN VALUES
    private static final String FIRST = "";
    private static final Long SECOND = 1L;
    private static final String THIRD = "FOO";

    // BEGIN COUPLES
    private static final Triple<Object> EMPTY_TRIPLE = i -> Optional.empty();
    private static final Triple<Object> SOME_TRIPLE = Triple.of(FIRST, SECOND, THIRD);

    @Test
    @DisplayName("First gets first element")
    public void first_firstElement() {
        assertEquals(FIRST, SOME_TRIPLE.first());
    }

    @Test
    @DisplayName("Second gets second element")
    public void second_secondElement() {
        assertEquals(SECOND, SOME_TRIPLE.second());
    }

    @Test
    @DisplayName("Third gets third element")
    public void third_thirdElement() {
        assertEquals(THIRD, SOME_TRIPLE.third());
    }

    @Test
    @DisplayName("Stream produces correct elements")
    public void stream_correctElements() {
        final List<Object> os = SOME_TRIPLE.stream().collect(Collectors.toList());
        assertEquals(FIRST, os.get(0));
        assertEquals(SECOND, os.get(1));
        assertEquals(THIRD, os.get(2));
    }

    @Test
    @DisplayName("Empty tuple throws error for first")
    public void first_emptyError() {
        assertThrows(Fallible.StacksException.class, EMPTY_TRIPLE::first);
    }

    @Test
    @DisplayName("Empty tuple throws error for second")
    public void second_emptyError() {
        assertThrows(Fallible.StacksException.class, EMPTY_TRIPLE::second);
    }

    @Test
    @DisplayName("Empty tuple throws error for third")
    public void third_emptyError() {
        assertThrows(Fallible.StacksException.class, EMPTY_TRIPLE::third);
    }
}
