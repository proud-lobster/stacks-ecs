package com.proudlobster.stacks.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.TestContstants;
import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.Entity;

@Tag("Unit")
public class EntityReaderTest implements TestContstants {

    @Test
    @DisplayName("Reading non-existing entity not present")
    void read_empty() {
        assertTrue(EMPTY_READER.read(ID_1).isEmpty());
    }

    @Test
    @DisplayName("Find entity by ID")
    void read_find() {
        assertEquals(ID_1, MAP_READER.read(ID_1).get().identifier());
    }

    @Test
    @DisplayName("No entities for components when empty")
    void read_componentsEmpty() {
        assertTrue(EMPTY_READER.read(FLAG_COMPONENT_1).findAny().isEmpty());
    }

    @Test
    @DisplayName("Finding on single component returns entity")
    void read_component() {
        assertEquals(ID_1, MAP_READER.read(FLAG_COMPONENT_1).map(Entity::identifier).findAny().get());
    }

    @Test
    @DisplayName("Finding on single component returns multiple entities")
    void read_componentsCount() {
        assertEquals(MAP_READER_CONTENTS.entrySet().size(), MAP_READER.read(Component.Core.IDENTITY).count());
    }

    @Test
    @DisplayName("Finding on multiple components returns matching entities")
    void read_components() {
        assertEquals(ID_3, MAP_READER.read(FLAG_COMPONENT_3, FLAG_COMPONENT_2).map(Entity::identifier).findAny().get());
    }

}
