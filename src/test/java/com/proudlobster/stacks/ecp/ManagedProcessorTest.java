package com.proudlobster.stacks.ecp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class ManagedProcessorTest implements TestContstants {

    @BeforeEach
    public void initializeEntities() {
        STACKS_REF.set(Stacks.create());
        STACKS_REF.get().$().createEntity(ID_1).assignComponent(ID_1, FLAG_COMPONENT_1).commit();
    }

    @Test
    @DisplayName("Processing with matching component causes change")
    public void process_matchChange() {
        MANAGED_PROCESSOR_1.get().process().commit();
        assertTrue(STACKS_REF.get().$(ID_2).findAny().isPresent());
    }

    @Test
    @DisplayName("Processing without matching component does not cause change")
    public void process_noMatchNoChange() {
        MANAGED_PROCESSOR_2.get().process().commit();
        assertFalse(STACKS_REF.get().$(ID_2).findAny().isPresent());
    }

    @Test
    @DisplayName("A subprocessor which matches components for itself and its parent does something")
    public void process_subprocessorSuccess() {
        STACKS_REF.get().$().createEntity(ID_3).assignComponent(ID_3, FLAG_COMPONENT_1)
                .assignComponent(ID_3, FLAG_COMPONENT_2).commit();
        MANAGED_PARENT_PROCESSOR_1.get().process().commit();
        assertTrue(STACKS_REF.get().$(ID_2).findAny().isPresent());
    }

    @Test
    @DisplayName("A subprocessor which does not match components for itself and its parent does nothing")
    public void process_subprocessorNothing() {
        MANAGED_PARENT_PROCESSOR_1.get().process().commit();
        assertFalse(STACKS_REF.get().$(ID_2).findAny().isPresent());
    }
}
