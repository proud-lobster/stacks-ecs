package com.proudlobster.stacks.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.ecp.ManagedEntity;
import com.proudlobster.stacks.ecp.ManagedTransaction;
import com.proudlobster.stacks.ecp.ManagedProcessor.ManagedProcessorFunction;
import com.proudlobster.stacks.scenario.life.GameComponent;
import com.proudlobster.stacks.scenario.life.RoomBuilder;

public class LifeScenarioTest {

    private static final Comparator<ManagedEntity> RANDOM_COMPARATOR = (a, b) -> a == b ? 0
            : (ThreadLocalRandom.current().nextBoolean() ? -1 : 1);

    private Stacks $;

    final ManagedProcessorFunction ROOM_PROCESSOR = e -> {
        LinkedList<ManagedEntity> creatures = e.referenceEntities(GameComponent.ROOM)
                .filter(n -> n.is(GameComponent.CREATURE)).collect(Collectors.toCollection(LinkedList::new));
        LinkedList<ManagedEntity> food = e.referenceEntities(GameComponent.ROOM).filter(n -> n.is(GameComponent.FOOD))
                .collect(Collectors.toCollection(LinkedList::new));

        ManagedTransaction t = e.$();
        while (creatures.size() > 0 && food.size() > 0) {
            ManagedEntity eater = creatures.pop();
            t = t.compose(eater.alterValue(GameComponent.ENERGY, 25L));
            t = t.compose(food.pop().expire());
            t = t.createEntitiesFromTemplate("test-message", eater.identifier() + " has eaten some food.");
        }

        return t;
    };

    final ManagedProcessorFunction ENERGY_PROCESSOR = e -> {
        if (e.is(GameComponent.ALIVE) && e.longValue(GameComponent.ENERGY).filter(l -> l <= 0).isPresent()) {
            return e.removeComponent(GameComponent.ALIVE).createEntitiesFromTemplate("test-message",
                    e.identifier() + " has died from lack of energy!");
        } else {
            return e.alterValue(GameComponent.ENERGY, -5L);
        }
    };

    final ManagedProcessorFunction MOVEMENT_PROCESSOR = e -> {
        if (e.is(GameComponent.ALIVE)) {

            final ManagedEntity currentRoom = e.referenceEntity(GameComponent.LOCATION).get();

            if (currentRoom.referenceEntities(GameComponent.ROOM).anyMatch(n -> n.is(GameComponent.FOOD))) {
                return $.$();
            } else {
                final Predicate<Long> isPrevious = e.referenceValues(GameComponent.PREVIOUS_LOCATIONS)
                        .map(l -> (Predicate<Long>) (p -> l.equals(p))).reduce((p1, p2) -> p1.or(p2))
                        .orElse(l -> false);
                final Comparator<ManagedEntity> priorityComparator = (a, b) -> {
                    int r = RANDOM_COMPARATOR.compare(a, b);
                    if (isPrevious.test(a.identifier())) {
                        r += 10;
                    }
                    if (isPrevious.test(b.identifier())) {
                        r -= 10;
                    }
                    return r;
                };
                final ManagedEntity randomExit = currentRoom.referenceEntities(GameComponent.ROOM)
                        .filter(n -> n.is(GameComponent.EXIT)).min(priorityComparator).get();
                final ManagedEntity randomRoom = randomExit.referenceEntity(GameComponent.DESTINATION).get();

                return currentRoom.removeReference(GameComponent.ROOM, e.identifier())
                        .andThen(e.addReference(GameComponent.PREVIOUS_LOCATIONS, currentRoom.identifier()))
                        .andThen(randomRoom.addReference(GameComponent.ROOM, e.identifier()))
                        .andThen(e.assignComponent(GameComponent.LOCATION, randomRoom.identifier()))
                        .andThen(e.alterValue(GameComponent.ENERGY, -1L));
            }
        } else {
            return $.$();
        }
    };

    final ManagedProcessorFunction MESSAGE_RECEIVER = e -> {
        e.stringValue(GameComponent.MESSAGE).ifPresent(System.out::println);
        return e.expire();
    };

    @BeforeEach
    public void reset() {
        $ = Stacks.create();
    }

    @Test
    @DisplayName("Single creature lives and dies")
    public void singleCreatureLivesAndDies() {
        $.$().createEntitiesFromTemplate("test-creature", 50).commit();
        assertEquals(1, $.$(GameComponent.CREATURE).count());
        assertEquals(1, $.$(GameComponent.ALIVE).count());
        assertEquals(50, $.$(GameComponent.CREATURE).findFirst().flatMap(e -> e.longValue(GameComponent.ENERGY)).get());

        $.$("energy-processor", GameComponent.ENERGY, ENERGY_PROCESSOR);
        $.$("message-receiver", GameComponent.MESSAGE, MESSAGE_RECEIVER);
        Supplier<Boolean> stillAlive = () -> $.$(GameComponent.ALIVE).count() > 0;
        int i = 0;
        while (stillAlive.get()) {
            $.runProcessors();
            i++;
        }

        assertEquals(11, i);
    }

    @Test
    @DisplayName("Single creature lives, eats, and dies")
    public void singleCreatureLivesEatsAndDies() {
        $.$().createEntitiesFromTemplate("test-room-1", 50).commit();
        assertEquals(1, $.$(GameComponent.ROOM).findFirst().map(r -> r.referenceEntities(GameComponent.ROOM))
                .orElseGet(Stream::empty).filter(e -> e.is(GameComponent.CREATURE)).count());
        assertEquals(1, $.$(GameComponent.ROOM).findFirst().map(r -> r.referenceEntities(GameComponent.ROOM))
                .orElseGet(Stream::empty).filter(e -> e.is(GameComponent.FOOD)).count());

        $.$("room-processor", GameComponent.ROOM, ROOM_PROCESSOR);
        $.$("energy-processor", GameComponent.ENERGY, ENERGY_PROCESSOR);
        $.$("message-receiver", GameComponent.MESSAGE, MESSAGE_RECEIVER);
        Supplier<Boolean> stillAlive = () -> $.$(GameComponent.ALIVE).count() > 0;
        int i = 0;
        while (stillAlive.get()) {
            $.runProcessors();
            i++;
        }

        assertEquals(16, i);
    }

    @Test
    @DisplayName("Single creature lives, moves, eats, and dies")
    public void singleCreatureLivesMovesEatsDies() {
        $.$().createEntitiesFromTemplate("test-creature", 50).commit();
        RoomBuilder.of($).buildRoomGrid(4, 4, 100).commit();

        ManagedEntity randomRoom = $.$(GameComponent.ROOM).min(RANDOM_COMPARATOR).get();
        ManagedEntity creature = $.$(GameComponent.CREATURE).findFirst().get();
        randomRoom.addReference(GameComponent.ROOM, creature.identifier()).commit();
        creature.assignComponent(GameComponent.LOCATION, randomRoom.identifier()).commit();

        $.$("room-processor", GameComponent.ROOM, ROOM_PROCESSOR);
        $.$("energy-processor", GameComponent.ENERGY, ENERGY_PROCESSOR);
        $.$("movement-processor", GameComponent.LOCATION, MOVEMENT_PROCESSOR);
        $.$("message-receiver", GameComponent.MESSAGE, MESSAGE_RECEIVER);
        Supplier<Boolean> stillAlive = () -> $.$(GameComponent.ALIVE).count() > 0;
        while (stillAlive.get()) {
            $.runProcessors();
        }

        assertEquals(0, $.$(GameComponent.FOOD).count());
    }

    @Test
    @DisplayName("Ten creatures live, move, eat, and die")
    public void tenCreaturesLiveMoveEatDie() {
        RoomBuilder.of($).buildRoomGrid(4, 4, 100).commit();

        for (int i = 0; i < 10; i++) {
            $.$().createEntitiesFromTemplate("test-creature", 50).commit();
        }

        $.$(GameComponent.CREATURE).forEach(e -> {
            ManagedEntity randomRoom = $.$(GameComponent.ROOM).min(RANDOM_COMPARATOR).get();
            randomRoom.addReference(GameComponent.ROOM, e.identifier()).commit();
            e.assignComponent(GameComponent.LOCATION, randomRoom.identifier()).commit();
        });

        $.$("room-processor", GameComponent.ROOM, ROOM_PROCESSOR);
        $.$("energy-processor", GameComponent.ENERGY, ENERGY_PROCESSOR);
        $.$("movement-processor", GameComponent.LOCATION, MOVEMENT_PROCESSOR);
        $.$("message-receiver", GameComponent.MESSAGE, MESSAGE_RECEIVER);
        Supplier<Boolean> stillAlive = () -> $.$(GameComponent.ALIVE).count() > 0;
        while (stillAlive.get()) {
            $.runProcessors();
        }

        assertEquals(0, $.$(GameComponent.FOOD).count());
    }

    @Test
    @DisplayName("One-hundred creatures live, move, eat, and die")
    public void oneHundredCreaturesLiveMoveEatDie() {
        RoomBuilder.of($).buildRoomGrid(4, 4, 100).commit();

        for (int i = 0; i < 100; i++) {
            $.$().createEntitiesFromTemplate("test-creature", 50).commit();
        }

        $.$(GameComponent.CREATURE).forEach(e -> {
            ManagedEntity randomRoom = $.$(GameComponent.ROOM).min(RANDOM_COMPARATOR).get();
            randomRoom.addReference(GameComponent.ROOM, e.identifier()).commit();
            e.assignComponent(GameComponent.LOCATION, randomRoom.identifier()).commit();
        });

        $.$("room-processor", GameComponent.ROOM, ROOM_PROCESSOR);
        $.$("energy-processor", GameComponent.ENERGY, ENERGY_PROCESSOR);
        $.$("movement-processor", GameComponent.LOCATION, MOVEMENT_PROCESSOR);
        $.$("message-receiver", GameComponent.MESSAGE, MESSAGE_RECEIVER);
        Supplier<Boolean> stillAlive = () -> $.$(GameComponent.ALIVE).count() > 0;
        while (stillAlive.get()) {
            $.runProcessors();
        }

        assertEquals(0, $.$(GameComponent.FOOD).count());
    }

    @Test
    @DisplayName("Message expires")
    public void messageExpires() {
        $.$().createEntitiesFromTemplate("test-message", "Foo").commit();
        assertEquals(1, $.$(GameComponent.MESSAGE).count());

        $.$("message-receiver", GameComponent.MESSAGE, MESSAGE_RECEIVER);
        $.runProcessors();
        assertEquals(0, $.$(GameComponent.MESSAGE).count());

    }

}
