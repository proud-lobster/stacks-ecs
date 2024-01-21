package com.proudlobster.stacks.utility;

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
import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.ManagedEntity;

@Tag("Unit")
public class ManagedEntityTemplateTest implements TestContstants {

    private static final EntityTemplate TRANSIENT_TEMPL = EntityTemplate.fromProperties("test-transient");
    private static final EntityTemplate NO_ID_TEMPL = EntityTemplate.fromProperties("test-no-id");
    private static final EntityTemplate REF_TEMPL = EntityTemplate.fromProperties("test-ref");

    @BeforeEach
    public void initializeEntities() {
        STACKS_REF.set(Stacks.create());
    }

    @Test
    @DisplayName("Template creates transient entity with identity")
    public void create_identityTransientEntity() {
        assertFalse(STACKS_REF.get().$(Component.Core.TRANSIENT).findAny().isPresent());
        TRANSIENT_TEMPL.manage(STACKS_REF.get()).get().commit();
        assertTrue(STACKS_REF.get().$(Component.Core.TRANSIENT).findAny().isPresent());
    }

    @Test
    @DisplayName("Template with no identity gets named identity")
    public void create_identityFromArgs() {
        assertFalse(STACKS_REF.get().$(Component.Core.IDENTITY).findAny().isPresent());
        NO_ID_TEMPL.manage(STACKS_REF.get()).get().commit();
        assertTrue(STACKS_REF.get().$(Component.Core.IDENTITY).findAny().isPresent());
        assertTrue(STACKS_REF.get().$(Component.Core.IDENTITY).findAny().map(ManagedEntity::identifier).isPresent());
    }

    @Test
    @DisplayName("Template replaced reference to other ID")
    public void replace_ref() {
        REF_TEMPL.manage(STACKS_REF.get()).get().commit();
        final Optional<ManagedEntity> fooEntity = STACKS_REF.get().$(FLAG_COMPONENT_2).findAny();
        final Optional<ManagedEntity> refEntity = STACKS_REF.get().$(REF_COMPONENT_1).findAny();

        assertTrue(fooEntity.isPresent());
        assertTrue(refEntity.isPresent());
        assertEquals(fooEntity.map(ManagedEntity::identifier).get(),
                refEntity.flatMap(e -> e.longValue(REF_COMPONENT_1)).get());
    }
}
