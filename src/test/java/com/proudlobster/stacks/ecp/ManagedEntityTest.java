package com.proudlobster.stacks.ecp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class ManagedEntityTest implements TestContstants {

    @BeforeEach
    public void initializeEntities() {
        STACKS_REF.set(Stacks.create());
        STACKS_REF.get().$().createEntity(ID_1).commit();
    }

    @Test
    @DisplayName("Managed entity is the same as entity")
    public void entity_matches() {
        assertEquals(MANAGED_ENTITY_1.get().identifier(), MANAGED_ENTITY_1.get().entity().identifier());
    }

    @Test
    @DisplayName("Managed entity exists")
    public void entity_exists() {
        assertEquals(MANAGED_ENTITY_1.get().identifier(),
                STACKS_REF.get().$(ID_1).findFirst().map(Entity::identifier).orElse(INVALID_ID));
    }
}
