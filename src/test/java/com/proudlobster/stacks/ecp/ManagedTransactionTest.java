package com.proudlobster.stacks.ecp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class ManagedTransactionTest implements TestContstants {

    @BeforeEach
    public void initializeEntities() {
        STACKS_REF.set(Stacks.create());
        STACKS_REF.get().$().createEntity(ID_1).assignComponent(ID_1, FLAG_COMPONENT_4).commit();
    }

    @Test
    @DisplayName("Creating a new entity creates an entity with the next entity ID")
    public void createEntity_usesNextId() {
        final Long nextId = STACKS_REF.get().nextId() + 1;
        assertFalse(STACKS_REF.get().$(nextId).findAny().isPresent());
        STACKS_REF.get().$().createEntity().commit();
        assertTrue(STACKS_REF.get().$(nextId).findAny().isPresent());
    }

    @Test
    @DisplayName("Creating a new transient entity creates a transient entity with the next entity ID")
    public void createTransientEntity_usesNextId() {
        final Long nextId = STACKS_REF.get().nextId() + 1;
        assertFalse(STACKS_REF.get().$(nextId).findAny().isPresent());
        STACKS_REF.get().$().createTransientEntity().commit();
        assertTrue(STACKS_REF.get().$(nextId).filter(e -> e.is(Component.Core.TRANSIENT)).findAny().isPresent());
    }

    @Test
    @DisplayName("Creating a new entity with components creates an entity with those components")
    public void createEntity_hasComponents() {
        final Long nextId = STACKS_REF.get().nextId() + 1;
        assertFalse(STACKS_REF.get().$(nextId).findAny().isPresent());
        STACKS_REF.get().$().createEntity(FLAG_COMPONENT_1).commit();
        assertTrue(STACKS_REF.get().$(nextId).filter(e -> e.is(FLAG_COMPONENT_1)).findAny().isPresent());
    }

    @Test
    @DisplayName("Creating a new transient entity with components creates a transient entity with those components")
    public void createTransientEntity_hasComponents() {
        final Long nextId = STACKS_REF.get().nextId() + 1;
        assertFalse(STACKS_REF.get().$(nextId).findAny().isPresent());
        STACKS_REF.get().$().createTransientEntity(FLAG_COMPONENT_1).commit();
        assertTrue(STACKS_REF.get().$(nextId).filter(e -> e.is(Component.Core.TRANSIENT))
                .filter(e -> e.is(FLAG_COMPONENT_1)).findAny().isPresent());
    }

    @Test
    @DisplayName("Assigning a flag component gives that component")
    public void assignComponent_givesFlagComponent() {
        STACKS_REF.get().$().assignComponent(ID_1, FLAG_COMPONENT_1).commit();
        assertEquals(ID_1, STACKS_REF.get().$(FLAG_COMPONENT_1).findAny().map(e -> e.identifier()).get());
    }

    @Test
    @DisplayName("Assigning a string component gives that component")
    public void assignComponent_givesStringComponent() {
        STACKS_REF.get().$().assignComponent(ID_1, STRING_COMPONENT_2, STRING_VALUE_1).commit();
        assertEquals(STRING_VALUE_1, STACKS_REF.get().$(STRING_COMPONENT_2).findAny()
                .map(e -> e.stringValue(STRING_COMPONENT_2)).filter(Optional::isPresent).map(Optional::get).get());
    }

    @Test
    @DisplayName("Assigning a long component gives that component")
    public void assignComponent_givesLongComponent() {
        STACKS_REF.get().$().assignComponent(ID_1, NUMBER_COMPONENT_3, LONG_VALUE_1).commit();
        assertEquals(LONG_VALUE_1, STACKS_REF.get().$(NUMBER_COMPONENT_3).findAny()
                .map(e -> e.longValue(NUMBER_COMPONENT_3)).filter(Optional::isPresent).map(Optional::get).get());
    }

    @Test
    @DisplayName("Removing a component removes it")
    public void removeComponent_removesComponent() {
        assertTrue(STACKS_REF.get().$(ID_1).filter(e -> e.is(FLAG_COMPONENT_4)).findAny().isPresent());
        STACKS_REF.get().$().removeComponent(ID_1, FLAG_COMPONENT_4).commit();
        assertFalse(STACKS_REF.get().$(ID_1).filter(e -> e.is(FLAG_COMPONENT_4)).findAny().isPresent());
    }

}
