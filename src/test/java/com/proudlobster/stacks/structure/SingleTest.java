package com.proudlobster.stacks.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class SingleTest implements TestContstants {

    // BEGIN VALUES
    private static final String FIRST = "";

    // BEGIN SINGLES
    private static final Single<Object> EMPTY_SINGLE = i -> Optional.empty();
    private static final Single<Object> SOME_SINGLE = Single.of(FIRST);

    @Test
    @DisplayName("First gets first element")
    public void first_firstElement() {
        assertEquals(FIRST, SOME_SINGLE.first());
    }

    @Test
    @DisplayName("Stream produces correct element")
    public void stream_correctElements() {
        assertEquals(FIRST, SOME_SINGLE.stream().findFirst().get());
    }

    @Test
    @DisplayName("Empty tuple throws error for first")
    public void first_emptyError() {
        assertThrows(Fallible.StacksException.class, EMPTY_SINGLE::first);
    }

}
