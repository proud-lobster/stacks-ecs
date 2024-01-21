package com.proudlobster.stacks.ecp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.proudlobster.stacks.ecp.Transaction.Lock;

@Tag("Unit")
public class TransactionTest implements TestContstants {

    @BeforeEach
    public void resetLock() {
        LOCK_INTR.set(Transaction.Lock.UNLOCKED);
    }

    @BeforeEach
    public void resetSequence() {
        T_LIST.clear();
    }

    @BeforeEach
    public void clearMemory() {
        MAP_WRITER_RECORDS.clear();
        MAP_WRITER_RECORDS.put(ID_1, new HashMap<>(Map.of(COMPONENT_NAME_4, Boolean.TRUE)));
    }

    // Lock tests
    @Test
    @DisplayName("New lock is unlocked")
    public void createLock_unlocked() {
        assertFalse(Lock.create().getLock().get());
    }

    @Test
    @DisplayName("Unlocked lock can be locked")
    public void lock_unlockedSuccessful() {
        assertTrue(LOCK.lock());
    }

    @Test
    @DisplayName("Locked lock can't be locked")
    public void lock_lockedFails() {
        LOCK_INTR.set(true);
        assertFalse(LOCK.lock());
    }

    @Test
    @DisplayName("Locked lock can be unlocked")
    public void unlock_lockedSuccess() {
        LOCK.unlock().commit(null);
        assertEquals(Transaction.Lock.UNLOCKED, LOCK_INTR.get());
    }

    @Test
    @DisplayName("Unlocked lock can be unlocked")
    public void unlock_unlockedSuccess() {
        LOCK_INTR.set(false);
        LOCK.unlock().commit(null);
        assertEquals(Transaction.Lock.UNLOCKED, LOCK_INTR.get());
    }

    @Test
    @DisplayName("Unlocked lock is unlocked")
    public void locked_unlockedFalse() {
        assertFalse(LOCK.locked());
    }

    @Test
    @DisplayName("Locked lock is locked")
    public void locked_lockedTrue() {
        LOCK.lock();
        assertTrue(LOCK.locked());
    }

    // Transaction tests
    @Test
    @DisplayName("Started transaction is locked")
    public void start_locked() {
        Transaction.start(LOCK);
        assertTrue(LOCK_INTR.get());
    }

    @Test
    @DisplayName("Committed transaction is unlocked")
    public void commit_unlocked() {
        LOCKED_TRANSACTION.commit(null);
        assertFalse(LOCK_INTR.get());
    }

    @Test
    @DisplayName("Free commit throws exception")
    public void commit_exception() {
        assertThrows(Fallible.StacksException.class, LIST_ADD_TRANSACTION_1::commit);
    }

    @Test
    @DisplayName("Composed transaction commits both")
    public void compose_commitsBoth() {
        LIST_ADD_TRANSACTION_1.compose(LIST_ADD_TRANSACTION_2).commit(null);
        assertEquals(STRING_VALUE_1, T_LIST.get(1));
        assertEquals(STRING_VALUE_2, T_LIST.get(0));
    }

    @Test
    @DisplayName("And then transaction commits both")
    public void andThen_commitsBoth() {
        LIST_ADD_TRANSACTION_1.andThen(LIST_ADD_TRANSACTION_2).accept(null);
        assertEquals(STRING_VALUE_1, T_LIST.get(0));
        assertEquals(STRING_VALUE_2, T_LIST.get(1));
    }

    @Test
    @DisplayName("Create entity creates correct values")
    public void createEntity_correctValues() {
        Transaction.start().createEntity(ID_1).commit(MAP_WRITER);
        assertEquals(ID_1, MAP_WRITER_RECORDS.get(ID_1).get(Component.Core.IDENTITY.name()));
    }

    @Test
    @DisplayName("Create transient entity creates correct values")
    public void createTransientEntity_correctValues() {
        Transaction.start().createTransientEntity(ID_1).commit(MAP_WRITER);
        assertEquals(ID_1, MAP_WRITER_RECORDS.get(ID_1).get(Component.Core.IDENTITY.name()));
        assertTrue(MAP_WRITER_RECORDS.get(ID_1).containsKey(Component.Core.TRANSIENT.name()));
    }

    @Test
    @DisplayName("Assign string component creates correct values")
    public void assignComponent_stringCorrectValues() {
        Transaction.start().assignComponent(ID_1, STRING_COMPONENT_1, STRING_VALUE_1).commit(MAP_WRITER);
        assertEquals(STRING_VALUE_1, MAP_WRITER_RECORDS.get(ID_1).get(COMPONENT_NAME_1));
    }

    @Test
    @DisplayName("Assign long component creates correct values")
    public void assignComponent_longCorrectValues() {
        Transaction.start().assignComponent(ID_1, NUMBER_COMPONENT_1, LONG_VALUE_1).commit(MAP_WRITER);
        assertEquals(LONG_VALUE_1, MAP_WRITER_RECORDS.get(ID_1).get(COMPONENT_NAME_1));
    }

    @Test
    @DisplayName("Assign flag component creates correct values")
    public void assignComponent_flagCorrectValues() {
        Transaction.start().assignComponent(ID_1, FLAG_COMPONENT_1).commit(MAP_WRITER);
        assertTrue(MAP_WRITER_RECORDS.get(ID_1).containsKey(FLAG_COMPONENT_1.name()));
    }

    @Test
    @DisplayName("Remove component creates correct values")
    public void removeComponent_correctValues() {
        Transaction.start().removeComponent(ID_1, FLAG_COMPONENT_4).commit(MAP_WRITER);
        assertFalse(MAP_WRITER_RECORDS.get(ID_1).containsKey(COMPONENT_NAME_4));
    }
}
