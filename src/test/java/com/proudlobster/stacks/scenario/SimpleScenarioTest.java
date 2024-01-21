package com.proudlobster.stacks.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.ManagedTransaction;
import com.proudlobster.stacks.ecp.ManagedProcessor.ManagedProcessorFunction;

@Tag("Scenario")
public class SimpleScenarioTest {

    private static final Component HAT = Component.flagOf("HAT");
    private static final ManagedProcessorFunction HAT_ASSIGNER = e -> e.assignComponent(HAT);

    private Stacks $;

    @BeforeEach
    public void reset() {
        $ = Stacks.create();
    }

    @Test
    @DisplayName("Commit One Thousand Entities")
    public void commitOneThousandEntities() {
        ManagedTransaction t = $.$();
        for (int i = 0; i < 1000; i++) {
            t = t.createEntity();
        }
        t.commit();
        assertEquals(1000, $.$(Component.Core.IDENTITY).count());
    }

    @Test
    @DisplayName("Commit Two Thousand Entities")
    public void commitTwoThousandEntities() {
        ManagedTransaction t = $.$();
        for (int i = 0; i < 2000; i++) {
            t = t.createEntity();
        }
        t.commit();
        assertEquals(2000, $.$(Component.Core.IDENTITY).count());
    }

    @Test
    @DisplayName("Commit One Thousand Transient Entities")
    public void commitOneThousandTransientEntities() {
        ManagedTransaction t = $.$();
        for (int i = 0; i < 1000; i++) {
            t = t.createTransientEntity();
        }
        t.commit();
        assertEquals(1000, $.$(Component.Core.IDENTITY).count());
    }

    @Test
    @DisplayName("Give one thing a hat")
    public void commitAssignHat() {
        $.$().createEntity().commit();
        $.$("hat-assigner", Component.Core.IDENTITY, HAT_ASSIGNER);
        $.runProcessors();
        assertEquals($.$(Component.Core.IDENTITY).count(), $.$(HAT).count());
    }

    @Test
    @DisplayName("Assign all five-hundred things hats")
    public void commitAssignFiveHundredHats() {
        IntStream.range(0, 500).mapToObj(x -> $.$().createEntity()).reduce(ManagedTransaction::compose)
                .ifPresent(ManagedTransaction::commit);
        assertEquals(500, $.$(Component.Core.IDENTITY).count());
        $.$("hat-assigner", Component.Core.IDENTITY, HAT_ASSIGNER);
        $.runProcessors();
        assertEquals($.$(Component.Core.IDENTITY).count(), $.$(HAT).count());
    }

    @Test
    @DisplayName("Create Simple World")
    public void createSimpleWorld() {
        $.$().createEntitiesFromTemplate("test-world").commit();
        assertEquals(1, $.$(Component.flagOf("WORLD")).count());
        assertEquals(2, $.$(Component.flagOf("WORLD"))
                .flatMap(e -> e.referenceValues(Component.of("CONTAINER", Component.DataType.MULTIREF))).count());
    }
}
