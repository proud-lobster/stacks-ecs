package com.proudlobster.stacks.ecp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class ComponentTest implements TestContstants {

    @Test
    @DisplayName("Empty component throws exception for name")
    public void name_emptyThrowsException() {
        assertThrows(Fallible.StacksException.class, EMPTY_COMPONENT::name);
    }

    @Test
    @DisplayName("Empty component throws exception for type")
    public void type_emptyThrowsException() {
        assertThrows(Fallible.StacksException.class, EMPTY_COMPONENT::type);
    }

    @Test
    @DisplayName("Component with invalid type does not throw exception for name")
    public void type_invalidTypeNameNoException() {
        assertDoesNotThrow(INVALID_COMPONENT::name);
    }

    @Test
    @DisplayName("Component with invalid type throws exception for type")
    public void type_invalidTypeTypeThrowsException() {
        assertThrows(Fallible.StacksException.class, INVALID_COMPONENT::type);
    }

    @Test
    @DisplayName("None-type component does not have a value type")
    public void type_noneValueTypeEmpty() {
        assertTrue(FLAG_COMPONENT_1.type().valueType.isEmpty());
    }

    @Test
    @DisplayName("String-type component has a String value type")
    public void type_stringValueTypeString() {
        assertEquals(String.class, STRING_COMPONENT_1.type().valueType.get());
    }

    @Test
    @DisplayName("Number-type component has a Long value type")
    public void type_numberValueTypeLong() {
        assertEquals(Long.class, NUMBER_COMPONENT_1.type().valueType.get());
    }

    @Test
    @DisplayName("Reference-type component has a Long value type")
    public void type_referenceValueTypeLong() {
        assertEquals(Long.class, REF_COMPONENT_1.type().valueType.get());
    }

    @Test
    @DisplayName("Multiref-type component has a String value type")
    public void type_multirefValueTypeString() {
        assertEquals(String.class, MULTIREF_COMPONENT_1.type().valueType.get());
    }
}
