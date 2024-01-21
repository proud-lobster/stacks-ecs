package com.proudlobster.stacks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Unit")
public class FallibleTest implements TestContstants {

    // BEGIN MESSAGES
    private static final String MESSAGE_TEMPLATE_STR = "Hello, {0}!";
    private static final String MESSAGE_TEMPLATE_COMPLETE = "Hello, World!";
    private static final String MESSAGE_PART_STR = "World";
    private static final String MESSAGE_OUTPUT = "Hello, World!";

    // BEGIN FALLIBLES
    private static final Fallible MESSAGE_FALLIBLE = Fallible.of(MESSAGE_TEMPLATE_STR);
    private static final Fallible MESSAGE_COMPLETE = Fallible.of(MESSAGE_TEMPLATE_COMPLETE);

    @Test
    @DisplayName("Apply produces correct message")
    void apply_correctMessage() {
        assertEquals(MESSAGE_OUTPUT, MESSAGE_FALLIBLE.apply(MESSAGE_PART_STR).get().getMessage());
    }

    @Test
    @DisplayName("Apply produces correct type")
    void apply_correctType() {
        assertEquals(Fallible.StacksException.class, MESSAGE_FALLIBLE.apply(MESSAGE_PART_STR).get().getClass());
    }

    @Test
    @DisplayName("Apply successful without full arguments")
    void apply_successWithoutArgs() {
        assertEquals(Fallible.StacksException.class, MESSAGE_FALLIBLE.apply().get().getClass());
    }

    @Test
    @DisplayName("Get produces correct type")
    void get_correctType() {
        assertEquals(Fallible.StacksException.class, MESSAGE_COMPLETE.get().getClass());
    }

    @Test
    @DisplayName("Thrown exception is correct type")
    void throwIt_correctType() {
        assertThrows(Fallible.StacksException.class, () -> MESSAGE_FALLIBLE.throwIt());
    }

}
