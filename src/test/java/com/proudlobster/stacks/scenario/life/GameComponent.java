package com.proudlobster.stacks.scenario.life;

import java.util.Optional;

import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.structure.Couple;

public enum GameComponent implements Component {

    CREATURE(),
    ALIVE(),
    ENERGY(DataType.NUMBER),
    MESSAGE(DataType.STRING),
    ROOM(DataType.MULTIREF),
    FOOD(),
    EXIT(),
    DIRECTION(DataType.STRING),
    DESTINATION(DataType.REFERENCE),
    LOCATION(DataType.REFERENCE),
    PREVIOUS_LOCATIONS(DataType.MULTIREF);

    Couple<String> couple;

    GameComponent() {
        this(DataType.NONE);
    }

    GameComponent(final DataType type) {
        this.couple = Couple.of(name(), type.name());
    }

    @Override
    public Optional<String> get(final long i) {
        return couple.get(i);
    }
}
