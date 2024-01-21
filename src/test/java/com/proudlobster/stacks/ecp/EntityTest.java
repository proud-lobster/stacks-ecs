package com.proudlobster.stacks.ecp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class EntityTest implements TestContstants {

    @Test
    @DisplayName("Identity value correct")
    public void identity_valueCorrect() {
        assertEquals(ID_1, ID_ENTITY_1.identifier());
    }

    @Test
    @DisplayName("Identity fails for empty entity")
    public void identity_failsForEmpty() {
        assertThrows(Fallible.StacksException.class, EMPTY_ENTITY::identifier);
    }

    @Test
    @DisplayName("Entity is what it is")
    public void is_true() {
        assertTrue(ID_ENTITY_1.is(Component.Core.IDENTITY));
    }

    @Test
    @DisplayName("Entity is not what it is not")
    public void is_false() {
        assertFalse(ID_ENTITY_1.is(Component.Core.TRANSIENT));
    }

    @Test
    @DisplayName("Entity component name is what it is")
    public void is_nameTrue() {
        assertTrue(ID_ENTITY_1.is(Component.Core.IDENTITY.name()));
    }

    @Test
    @DisplayName("Entity component name is not what it is not")
    public void is_nameFalse() {
        assertFalse(ID_ENTITY_1.is(Component.Core.TRANSIENT.name()));
    }

    @Test
    @DisplayName("String value matches")
    public void stringValue_matches() {
        assertEquals(STRING_VALUE_1, STRING_ENTITY_1.stringValue(STRING_COMPONENT_1).get());
    }

    @Test
    @DisplayName("Long value matches")
    public void longValue_matches() {
        assertEquals(ID_1, ID_ENTITY_1.longValue(Component.Core.IDENTITY).get());
    }

    @Test
    @DisplayName("Single reference value matches")
    public void referenceValues_singleMatches() {
        assertEquals(ID_1, ID_ENTITY_1.referenceValues(Component.Core.IDENTITY).findAny().get());
    }

    @Test
    @DisplayName("Multi-reference values match")
    public void referenceValues_allMatch() {
        final List<Long> vs = MULTI_ENTITY_2.referenceValues(MULTIREF_COMPONENT_2).collect(Collectors.toList());
        assertEquals(ID_1, vs.get(0));
        assertEquals(ID_2, vs.get(1));
        assertEquals(ID_3, vs.get(2));
    }
}
