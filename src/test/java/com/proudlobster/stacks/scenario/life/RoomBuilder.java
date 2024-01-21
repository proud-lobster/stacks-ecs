package com.proudlobster.stacks.scenario.life;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.ecp.ManagedEntity;
import com.proudlobster.stacks.ecp.ManagedTransaction;

public interface RoomBuilder {

    public static RoomBuilder of(final Stacks s) {
        return () -> s;
    }

    Stacks stacks();

    default ManagedTransaction $() {
        return stacks().$();
    }

    default Long nextId() {
        return stacks().nextId();
    }

    default ManagedEntity $(final Long id) {
        return stacks().$(id).findFirst().get();
    }

    default ManagedTransaction buildRoomGrid(int x, int y, int foodCount) {

        ManagedTransaction createRooms = $();
        long[][] rooms = new long[x][y];
        for (int ix = 0; ix < x; ix++) {
            for (int iy = 0; iy < y; iy++) {
                rooms[ix][iy] = nextId();
                createRooms = createRooms.createEntity(rooms[ix][iy]).assignComponent(rooms[ix][iy], GameComponent.ROOM,
                        "");
            }
        }

        ManagedTransaction linkRooms = $();
        for (int ix = 0; ix < x; ix++) {
            for (int iy = 0; iy < y; iy++) {
                final Long room = rooms[ix][iy];
                final List<Long> exitsToAdd = new ArrayList<>();
                if (iy - 1 >= 0) {
                    long id = nextId();
                    linkRooms = linkRooms.createEntitiesFromTemplate("test-exit", id, "north", rooms[ix][iy - 1]);
                    exitsToAdd.add(id);
                }
                if (ix + 1 < x) {
                    long id = nextId();
                    linkRooms = linkRooms.createEntitiesFromTemplate("test-exit", id, "east", rooms[ix + 1][iy]);
                    exitsToAdd.add(id);
                }
                if (iy + 1 < y) {
                    long id = nextId();
                    linkRooms = linkRooms.createEntitiesFromTemplate("test-exit", id, "south", rooms[ix][iy + 1]);
                    exitsToAdd.add(id);
                }
                if (ix - 1 >= 0) {
                    long id = nextId();
                    linkRooms = linkRooms.createEntitiesFromTemplate("test-exit", id, "west", rooms[ix - 1][iy]);
                    exitsToAdd.add(id);
                }

                final Supplier<String> exitsSupplier = () -> Stream
                        .concat($(room).referenceValues(GameComponent.ROOM), exitsToAdd.stream()).map(o -> o.toString())
                        .collect(Collectors.joining("|"));

                linkRooms = linkRooms.assignComponentString(room, GameComponent.ROOM, exitsSupplier);
            }
        }

        ManagedTransaction dropFood = $();
        final Random r = new Random(System.currentTimeMillis());
        for (int i = 0; i < foodCount; i++) {
            final long room = rooms[r.nextInt(x)][r.nextInt(y)];
            final long foodId = nextId();
            dropFood = dropFood.createEntity(foodId).assignComponent(foodId, GameComponent.FOOD);
            final Supplier<String> foodSupplier = () -> Stream
                    .concat($(room).referenceValues(GameComponent.ROOM), Stream.of(foodId)).map(o -> o.toString())
                    .collect(Collectors.joining("|"));
            dropFood = dropFood.assignComponentString(room, GameComponent.ROOM, foodSupplier);
        }

        return createRooms.andThen(linkRooms).andThen(dropFood);
    }
}
