package com.proudlobster.stacks.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class EntityStorageDescriptorTest implements TestContstants {

    @Test
    @DisplayName("Descriptor identifier present")
    public void identifier_present() {
        assertEquals(ID_1, FULL_RECORD.identifier().get());
    }

    @Test
    @DisplayName("Descriptor component present")
    public void component_present() {
        assertEquals(COMPONENT_NAME_1, FULL_RECORD.component().get());
    }

    @Test
    @DisplayName("Descriptor string value present")
    public void stringValue_present() {
        assertEquals(STRING_VALUE_1, FULL_RECORD.stringValue().get());
    }

    @Test
    @DisplayName("Descriptor long value present")
    public void longValue_present() {
        assertEquals(LONG_VALUE_1, FULL_RECORD.longValue().get());
    }

    @Test
    @DisplayName("Descriptor with only long value returns long value")
    public void value_isLong() {
        assertEquals(LONG_VALUE_1, NUMBER_RECORD.value());
    }

    @Test
    @DisplayName("Descriptor active present")
    public void active_present() {
        assertEquals(Boolean.TRUE, FULL_RECORD.active());
    }

    @Test
    @DisplayName("Empty descriptor identifier is empty")
    public void identifier_missing() {
        assertTrue(EMPTY_RECORD.identifier().isEmpty());
    }

    @Test
    @DisplayName("Empty Descriptor component is empty")
    public void component_missing() {
        assertTrue(EMPTY_RECORD.component().isEmpty());
    }

    @Test
    @DisplayName("Empty descriptor string value empty")
    public void stringValue_missing() {
        assertTrue(EMPTY_RECORD.stringValue().isEmpty());
    }

    @Test
    @DisplayName("Empty descriptor long value empty")
    public void longValue_missing() {
        assertTrue(EMPTY_RECORD.longValue().isEmpty());
    }

    @Test
    @DisplayName("Empty descriptor active throws error")
    public void active_missing() {
        assertThrows(Fallible.StacksException.class, EMPTY_RECORD::active);
    }
}