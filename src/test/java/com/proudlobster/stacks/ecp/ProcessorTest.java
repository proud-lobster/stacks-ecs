package com.proudlobster.stacks.ecp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class ProcessorTest implements TestContstants {

    @BeforeEach
    public void resetWrite() {
        MAP_WRITER_RECORDS.clear();
    }

    @Test
    @DisplayName("Component for processor exists")
    public void component_exists() {
        assertEquals(FLAG_COMPONENT_1, PROCESSOR_1.component());
    }

    @Test
    @DisplayName("Transaction does nothing if nothing to do")
    public void process_noOp() {
        PROCESSOR_1.process(FLAG_ENTITY_1).commit(MAP_WRITER);
        assertTrue(MAP_WRITER_RECORDS.isEmpty());
    }

    @Test
    @DisplayName("Processor does nothing if entity does not have its component")
    public void process_notHavingComponent() {
        PROCESSOR_2.process(FLAG_ENTITY_1).commit(MAP_WRITER);
        assertTrue(MAP_WRITER_RECORDS.isEmpty());
    }

    @Test
    @DisplayName("Processor does something if entity does have its component")
    public void process_havingComponent() {
        PROCESSOR_2.process(FLAG_ENTITY_2).commit(MAP_WRITER);
        assertFalse(MAP_WRITER_RECORDS.isEmpty());
    }

    @Test
    @DisplayName("A subprocessor which matches components for itself and its parent does something")
    public void process_subprocessorSuccess() {
        PARENT_PROCESSOR_1.process(ENTITY_FLAG_1_FLAG_2).commit(MAP_WRITER);
        assertFalse(MAP_WRITER_RECORDS.isEmpty());
    }

    @Test
    @DisplayName("A subprocessor which does not match components for itself and its parent does nothing")
    public void process_subprocessorNothing() {
        PARENT_PROCESSOR_1.process(FLAG_ENTITY_2).commit(MAP_WRITER);
        assertTrue(MAP_WRITER_RECORDS.isEmpty());
    }
}
