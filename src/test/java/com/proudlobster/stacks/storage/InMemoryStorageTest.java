package com.proudlobster.stacks.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.TestContstants;
import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.Entity;

@Tag("Unit")
public class InMemoryStorageTest implements TestContstants {

    private static final Long COMPS_COUNT = 3L;

    @BeforeEach
    public void resetStorage() {
        INNER_STORAGE.set(InMemoryStorage.of());
    }

    @Test
    @DisplayName("Reading non-existing entity not present")
    void read_empty() {
        assertTrue(STORAGE.read(ID_1).isEmpty());
    }

    @Test
    @DisplayName("Find entity by ID")
    void read_find() {
        STORAGE.writeEntity(ID_1);
        assertEquals(ID_1, STORAGE.read(ID_1).get().identifier());
    }

    @Test
    @DisplayName("No entities for components when empty")
    void read_componentsEmpty() {
        assertTrue(STORAGE.read(STRING_COMPONENT_1).findAny().isEmpty());
    }

    @Test
    @DisplayName("Finding on single component returns entity")
    void read_component() {
        STORAGE.writeEntity(ID_1);
        STORAGE.writeAssignComponent(ID_1, COMPONENT_NAME_1);
        assertEquals(ID_1, STORAGE.read(STRING_COMPONENT_1).map(Entity::identifier).findAny().get());
    }

    @Test
    @DisplayName("Finding on single component returns multiple entities")
    void read_componentsCount() {
        STORAGE.writeEntity(ID_1);
        STORAGE.writeEntity(ID_2);
        STORAGE.writeEntity(ID_3);
        assertEquals(COMPS_COUNT, STORAGE.read(Component.Core.IDENTITY).count());
    }

    @Test
    @DisplayName("Finding on multiple components returns matching entities")
    void read_components() {
        STORAGE.writeEntity(ID_3);
        STORAGE.writeAssignComponent(ID_3, COMPONENT_NAME_2);
        STORAGE.writeAssignComponent(ID_3, COMPONENT_NAME_3);
        assertEquals(ID_3, STORAGE.read(FLAG_COMPONENT_3, NUMBER_COMPONENT_2).map(Entity::identifier).findAny().get());
    }

    @Test
    @DisplayName("Write entity produces correct data")
    public void writeEntity_correctRecord() {
        STORAGE.writeEntity(ID_1);
        assertEquals(ID_1, STORAGE.read(ID_1).map(Entity::identifier).get());
    }

    @Test
    @DisplayName("Write string component produces correct data")
    public void writeAssignComponent_stringCorrectRecord() {
        STORAGE.writeEntity(ID_1);
        STORAGE.writeAssignComponent(ID_1, COMPONENT_NAME_1, STRING_VALUE_1);
        assertEquals(STRING_VALUE_1, STORAGE.read(ID_1).map(e -> e.stringValue(STRING_COMPONENT_1).get()).get());
    }

    @Test
    @DisplayName("Write long component produces correct data")
    public void writeAssignComponent_longCorrectRecord() {
        STORAGE.writeAssignComponent(ID_1, COMPONENT_NAME_2, LONG_VALUE_1);
        assertEquals(LONG_VALUE_1, STORAGE.read(ID_1).map(e -> e.longValue(NUMBER_COMPONENT_2).get()).get());
    }

    @Test
    @DisplayName("Write flag component produces correct data")
    public void writeAssignComponent_flagCorrectRecord() {
        STORAGE.writeAssignComponent(ID_1, COMPONENT_NAME_3);
        assertTrue(STORAGE.read(ID_1).map(e -> e.is(FLAG_COMPONENT_3)).get());
    }

    @Test
    @DisplayName("Write transient entity produces correct data")
    public void writeTransientEntity_correctRecord() {
        STORAGE.writeTransientEntity(ID_1);
        assertEquals(ID_1, STORAGE.read(ID_1).map(Entity::identifier).get());
        assertTrue(STORAGE.read(ID_1).map(e -> e.is(Component.Core.TRANSIENT)).get());
    }

    @Test
    @DisplayName("Write remove component produces correct data")
    public void writeRemoveComponent_correctRecord() {
        STORAGE.writeEntity(ID_1);
        STORAGE.writeAssignComponent(ID_1, COMPONENT_NAME_2, LONG_VALUE_1);
        STORAGE.writeRemoveComponent(ID_1, COMPONENT_NAME_2);
        assertFalse(STORAGE.read(ID_1).map(e -> e.is(NUMBER_COMPONENT_2)).get());
    }
}
