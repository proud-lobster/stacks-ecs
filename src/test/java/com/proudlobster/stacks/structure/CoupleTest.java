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
public class CoupleTest implements TestContstants {

    // BEGIN VALUES
    private static final String FIRST = "";
    private static final Long SECOND = 1L;

    // BEGIN COUPLES
    private static final Couple<Object> EMPTY_COUPLE = i -> Optional.empty();
    private static final Couple<Object> SOME_COUPLE = Couple.of(FIRST, SECOND);

    @Test
    @DisplayName("First gets first element")
    public void first_firstElement() {
        assertEquals(FIRST, SOME_COUPLE.first());
    }

    @Test
    @DisplayName("Second gets second element")
    public void second_secondElement() {
        assertEquals(SECOND, SOME_COUPLE.second());
    }

    @Test
    @DisplayName("Stream produces correct elements")
    public void stream_correctElements() {
        final List<Object> os = SOME_COUPLE.stream().collect(Collectors.toList());
        assertEquals(FIRST, os.get(0));
        assertEquals(SECOND, os.get(1));
    }

    @Test
    @DisplayName("Empty tuple throws error for first")
    public void first_emptyError() {
        assertThrows(Fallible.StacksException.class, EMPTY_COUPLE::first);
    }

    @Test
    @DisplayName("Empty tuple throws error for second")
    public void second_emptyError() {
        assertThrows(Fallible.StacksException.class, EMPTY_COUPLE::second);
    }
}
