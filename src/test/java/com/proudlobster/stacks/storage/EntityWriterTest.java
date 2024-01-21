package com.proudlobster.stacks.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;
import com.proudlobster.stacks.ecp.Component;

@Tag("Unit")
public class EntityWriterTest implements TestContstants {

    @BeforeEach
    public void clearMemory() {
        MAP_WRITER_RECORDS.clear();
        MAP_WRITER_RECORDS.put(ID_1, new HashMap<>(Map.of(COMPONENT_NAME_4, Boolean.TRUE)));
    }

    // Record tests
    @Test
    @DisplayName("Record identifier present")
    public void identifier_present() {
        assertEquals(ID_1, FULL_WRITER_RECORD.requiredIdentifier());
    }

    @Test
    @DisplayName("Record component present")
    public void component_present() {
        assertEquals(COMPONENT_NAME_1, FULL_WRITER_RECORD.requiredComponent());
    }

    @Test
    @DisplayName("Record string value present")
    public void stringValue_present() {
        assertEquals(STRING_VALUE_1, FULL_WRITER_RECORD.stringValue().get());
    }

    @Test
    @DisplayName("Record long value present")
    public void longValue_present() {
        assertEquals(LONG_VALUE_1, FULL_WRITER_RECORD.longValue().get());
    }

    @Test
    @DisplayName("Record active present")
    public void active_present() {
        assertEquals(Boolean.TRUE, FULL_WRITER_RECORD.active());
    }

    @Test
    @DisplayName("Empty record identifier throws error")
    public void identifier_missing() {
        assertThrows(Fallible.StacksException.class, EMPTY_WRITER_RECORD::requiredIdentifier);
    }

    @Test
    @DisplayName("Empty Record component throws error")
    public void component_missing() {
        assertThrows(Fallible.StacksException.class, EMPTY_WRITER_RECORD::requiredComponent);
    }

    @Test
    @DisplayName("Empty record string value empty")
    public void stringValue_missing() {
        assertTrue(EMPTY_RECORD.stringValue().isEmpty());
    }

    @Test
    @DisplayName("Empty record long value empty")
    public void longValue_missing() {
        assertTrue(EMPTY_RECORD.longValue().isEmpty());
    }

    @Test
    @DisplayName("Empty record active throws error")
    public void active_missing() {
        assertThrows(Fallible.StacksException.class, EMPTY_RECORD::active);
    }

    // Writer tests
    @Test
    @DisplayName("Write entity produces correct data")
    public void writeEntity_correctRecord() {
        MAP_WRITER.writeEntity(ID_1);
        assertEquals(ID_1, MAP_WRITER_RECORDS.get(ID_1).get(Component.Core.IDENTITY.name()));
    }

    @Test
    @DisplayName("Write string component produces correct data")
    public void writeAssignComponent_stringCorrectRecord() {
        MAP_WRITER.writeAssignComponent(ID_1, COMPONENT_NAME_1, STRING_VALUE_1);
        assertEquals(STRING_VALUE_1, MAP_WRITER_RECORDS.get(ID_1).get(COMPONENT_NAME_1));
    }

    @Test
    @DisplayName("Write long component produces correct data")
    public void writeAssignComponent_longCorrectRecord() {
        MAP_WRITER.writeAssignComponent(ID_1, COMPONENT_NAME_1, LONG_VALUE_1);
        assertEquals(LONG_VALUE_1, MAP_WRITER_RECORDS.get(ID_1).get(COMPONENT_NAME_1));
    }

    @Test
    @DisplayName("Write flag component produces correct data")
    public void writeAssignComponent_flagCorrectRecord() {
        MAP_WRITER.writeAssignComponent(ID_1, COMPONENT_NAME_1);
        assertEquals(EntityWriter.Record.DEFAULT_FLAG, MAP_WRITER_RECORDS.get(ID_1).get(COMPONENT_NAME_1));
    }

    @Test
    @DisplayName("Write transient entity produces correct data")
    public void writeTransientEntity_correctRecord() {
        MAP_WRITER.writeTransientEntity(ID_1);
        assertEquals(ID_1, MAP_WRITER_RECORDS.get(ID_1).get(Component.Core.IDENTITY.name()));
        assertEquals(EntityWriter.Record.DEFAULT_FLAG,
                MAP_WRITER_RECORDS.get(ID_1).get(Component.Core.TRANSIENT.name()));
    }

    @Test
    @DisplayName("Write remove component produces correct data")
    public void writeRemoveComponent_correctRecord() {
        MAP_WRITER.writeRemoveComponent(ID_1, COMPONENT_NAME_4);
        assertNull(MAP_WRITER_RECORDS.get(ID_1).get(COMPONENT_NAME_4));
    }
}
