package com.proudlobster.stacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.Entity;
import com.proudlobster.stacks.ecp.ManagedEntity;
import com.proudlobster.stacks.ecp.ManagedProcessor;
import com.proudlobster.stacks.ecp.Processor;
import com.proudlobster.stacks.ecp.Transaction;
import com.proudlobster.stacks.ecp.ManagedProcessor.ManagedProcessorFunction;
import com.proudlobster.stacks.ecp.Processor.ProcessorFunction;
import com.proudlobster.stacks.storage.EntityReader;
import com.proudlobster.stacks.storage.EntityStorageDescriptor;
import com.proudlobster.stacks.storage.EntityWriter;
import com.proudlobster.stacks.storage.InMemoryStorage;
import com.proudlobster.stacks.structure.Dictionary;

public interface TestContstants {

        static Map<String, Object> mockEntityMap(final Object... os) {
                final Map<String, Object> cm = new HashMap<>();
                for (int i = 0; i < os.length; i++) {
                        if (os[i] instanceof Component) {
                                final Component c = (Component) os[i];
                                if (c.type() == Component.DataType.NONE) {
                                        cm.put(c.name(), true);
                                } else {
                                        i++;
                                        cm.put(c.name(), os[i]);
                                }

                        }
                }
                return cm;
        }

        static Entity mockEntity(final Object... os) {
                final Map<String, Object> cm = mockEntityMap(os);
                return () -> k -> Optional.ofNullable(cm.get(k));
        }

        String KEY_1 = "fookey";
        String KEY_2 = "barkey";
        String STRING_VALUE_1 = "bar";
        String STRING_VALUE_2 = "bar2";
        String MULTIREF_STRING_2 = "1|2|3";
        Long LONG_VALUE_1 = 1000L;
        Long LONG_VALUE_2 = 1001L;
        Long INVALID_ID = -1L;
        Long ID_1 = 1L;
        Long ID_2 = 2L;
        Long ID_3 = 3L;

        String COMPONENT_NAME_1 = "FOO_COMP1";
        String COMPONENT_NAME_2 = "FOO_COMP2";
        String COMPONENT_NAME_3 = "FOO_COMP3";
        String COMPONENT_NAME_4 = "FOO_COMP4";

        String INVALID_COMPONENT_TYPE = "INVALID";

        Component EMPTY_COMPONENT = i -> Optional.empty();
        Component INVALID_COMPONENT = Component.of(COMPONENT_NAME_1, INVALID_COMPONENT_TYPE);
        Component FLAG_COMPONENT_1 = Component.flagOf(COMPONENT_NAME_1);
        Component STRING_COMPONENT_1 = Component.of(COMPONENT_NAME_1, Component.DataType.STRING);
        Component NUMBER_COMPONENT_1 = Component.of(COMPONENT_NAME_1, Component.DataType.NUMBER);
        Component REF_COMPONENT_1 = Component.of(COMPONENT_NAME_1, Component.DataType.REFERENCE);
        Component MULTIREF_COMPONENT_1 = Component.of(COMPONENT_NAME_1, Component.DataType.MULTIREF);
        Component FLAG_COMPONENT_2 = Component.flagOf(COMPONENT_NAME_2);
        Component STRING_COMPONENT_2 = Component.of(COMPONENT_NAME_2, Component.DataType.STRING);
        Component NUMBER_COMPONENT_2 = Component.of(COMPONENT_NAME_2, Component.DataType.NUMBER);
        Component REF_COMPONENT_2 = Component.of(COMPONENT_NAME_2, Component.DataType.REFERENCE);
        Component MULTIREF_COMPONENT_2 = Component.of(COMPONENT_NAME_2, Component.DataType.MULTIREF);
        Component FLAG_COMPONENT_3 = Component.flagOf(COMPONENT_NAME_3);
        Component STRING_COMPONENT_3 = Component.of(COMPONENT_NAME_3, Component.DataType.STRING);
        Component NUMBER_COMPONENT_3 = Component.of(COMPONENT_NAME_3, Component.DataType.NUMBER);
        Component REF_COMPONENT_3 = Component.of(COMPONENT_NAME_3, Component.DataType.REFERENCE);
        Component MULTIREF_COMPONENT_3 = Component.of(COMPONENT_NAME_3, Component.DataType.MULTIREF);
        Component FLAG_COMPONENT_4 = Component.flagOf(COMPONENT_NAME_4);
        Component STRING_COMPONENT_4 = Component.of(COMPONENT_NAME_4, Component.DataType.STRING);
        Component NUMBER_COMPONENT_4 = Component.of(COMPONENT_NAME_4, Component.DataType.NUMBER);
        Component REF_COMPONENT_4 = Component.of(COMPONENT_NAME_4, Component.DataType.REFERENCE);
        Component MULTIREF_COMPONENT_4 = Component.of(COMPONENT_NAME_4, Component.DataType.MULTIREF);

        Entity EMPTY_ENTITY = mockEntity();
        Entity ID_ENTITY_1 = mockEntity(Component.Core.IDENTITY, ID_1);
        Entity FLAG_ENTITY_1 = mockEntity(FLAG_COMPONENT_1);
        Entity STRING_ENTITY_1 = mockEntity(STRING_COMPONENT_1, STRING_VALUE_1);
        Entity FLAG_ENTITY_2 = mockEntity(FLAG_COMPONENT_2);
        Entity MULTI_ENTITY_2 = mockEntity(MULTIREF_COMPONENT_2, MULTIREF_STRING_2);
        Entity ENTITY_FLAG_1_FLAG_2 = mockEntity(FLAG_COMPONENT_1, FLAG_COMPONENT_2);

        AtomicReference<InMemoryStorage> INNER_STORAGE = new AtomicReference<>();
        InMemoryStorage STORAGE = (r, w) -> INNER_STORAGE.get().handle(r, w);

        EntityStorageDescriptor EMPTY_RECORD = () -> Stream.of();
        EntityStorageDescriptor FULL_RECORD = EntityStorageDescriptor.of(Optional.of(ID_1),
                        Optional.of(COMPONENT_NAME_1),
                        Optional.of(STRING_VALUE_1), Optional.of(LONG_VALUE_1), Boolean.TRUE);
        EntityStorageDescriptor NUMBER_RECORD = EntityStorageDescriptor.of(Optional.of(ID_1), Optional.empty(),
                        Optional.empty(), Optional.of(LONG_VALUE_1), Boolean.TRUE);

        EntityWriter.Record FULL_WRITER_RECORD = () -> FULL_RECORD.stream();
        EntityWriter.Record EMPTY_WRITER_RECORD = () -> EMPTY_RECORD.stream();

        EntityReader EMPTY_READER = s -> Stream.empty();
        Map<Long, Map<String, Object>> MAP_READER_CONTENTS = Map.of(
                        ID_1, mockEntityMap(Component.Core.IDENTITY, ID_1, FLAG_COMPONENT_1),
                        ID_2, mockEntityMap(Component.Core.IDENTITY, ID_2, FLAG_COMPONENT_2, Component.Core.TRANSIENT),
                        ID_3, mockEntityMap(Component.Core.IDENTITY, ID_3, FLAG_COMPONENT_2, FLAG_COMPONENT_3));
        EntityReader MAP_READER = r -> MAP_READER_CONTENTS.entrySet().stream()
                        .filter(e -> r.identifier().map(e.getKey()::equals).orElse(true))
                        .map(Map.Entry::getValue).filter(m -> r.component().map(m::containsKey).orElse(true))
                        .map(Dictionary::of).map(d -> (Entity) () -> d);

        Map<Long, Map<String, Object>> MAP_WRITER_RECORDS = new HashMap<>();
        EntityWriter DELETE_MAP_WRITER = r -> Optional.of(r).filter(x -> !x.active())
                        .ifPresent(x -> MAP_WRITER_RECORDS.get(r.requiredIdentifier()).remove(r.requiredComponent()));
        EntityWriter ACTIVE_MAP_WRITER = r -> Optional.of(r).filter(EntityWriter.Record::active)
                        .ifPresent(x -> MAP_WRITER_RECORDS.computeIfAbsent(r.requiredIdentifier(), i -> new HashMap<>())
                                        .put(r.requiredComponent(), r.value()));
        EntityWriter MAP_WRITER = r -> ACTIVE_MAP_WRITER.andThen(DELETE_MAP_WRITER).accept(r);

        AtomicBoolean LOCK_INTR = new AtomicBoolean();
        Transaction.Lock LOCK = () -> LOCK_INTR;
        Transaction LOCKED_TRANSACTION = Transaction.start(LOCK);
        List<String> T_LIST = new ArrayList<>();
        Transaction LIST_ADD_TRANSACTION_1 = w -> T_LIST.add(STRING_VALUE_1);
        Transaction LIST_ADD_TRANSACTION_2 = w -> T_LIST.add(STRING_VALUE_2);

        ProcessorFunction NO_OP = e -> Transaction.start();
        ProcessorFunction ASSIGN_COMP_1_TO_ID_2 = e -> Transaction.start().assignComponent(ID_1, FLAG_COMPONENT_1);
        Processor PROCESSOR_1 = Processor.of(FLAG_COMPONENT_1, NO_OP);
        Processor PROCESSOR_2 = Processor.of(FLAG_COMPONENT_2, ASSIGN_COMP_1_TO_ID_2);
        Processor PARENT_PROCESSOR_1 = Processor.of(FLAG_COMPONENT_1, PROCESSOR_2);

        AtomicReference<Stacks> STACKS_REF = new AtomicReference<>(Stacks.create());

        Supplier<ManagedEntity> MANAGED_ENTITY_1 = () -> STACKS_REF.get().$(ID_1).findAny().get();

        ManagedProcessorFunction CREATE_ENTITY_2 = e -> e.$().createEntity(ID_2);
        Supplier<ManagedProcessor> MANAGED_PROCESSOR_1 = () -> ManagedProcessor.of(FLAG_COMPONENT_1, CREATE_ENTITY_2,
                        STACKS_REF.get());
        Supplier<ManagedProcessor> MANAGED_PROCESSOR_2 = () -> ManagedProcessor.of(FLAG_COMPONENT_2, CREATE_ENTITY_2,
                        STACKS_REF.get());
        Supplier<ManagedProcessor> MANAGED_PARENT_PROCESSOR_1 = () -> ManagedProcessor.of(FLAG_COMPONENT_1,
                        STACKS_REF.get(), MANAGED_PROCESSOR_2.get());
}
