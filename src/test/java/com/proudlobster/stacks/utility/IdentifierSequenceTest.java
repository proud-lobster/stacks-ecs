package com.proudlobster.stacks.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Unit")
public class IdentifierSequenceTest {

    private static final Long SEED = 0L;
    private static final Long SEED_PLUS = SEED + 1;
    private static final Long DEFAULT_BASELINE = System.currentTimeMillis() * 1000;

    private static final int DUPE_RANGE_START = 0;
    private static final int DUPE_RANGE_END = 100000;

    private static IdentifierSequence DEFAULT;
    private static IdentifierSequence SEEDED;

    @BeforeEach
    public void initialize() {
        SEEDED = IdentifierSequence.create(SEED);
        DEFAULT = IdentifierSequence.create();
    }

    @Test
    @DisplayName("Seeded sequence produces number higher than seed")
    public void create_seededIncrement() {
        assertEquals(SEED_PLUS, SEEDED.get());
    }

    @Test
    @DisplayName("Default sequence produces numbers higher than test start time")
    public void create_defaultBaseline() {
        assertTrue(DEFAULT.get() > DEFAULT_BASELINE);
    }

    @Test
    @DisplayName("Sequences produce no duplicates")
    public void create_noDupes() {
        assertFalse(IntStream.range(DUPE_RANGE_START, DUPE_RANGE_END).mapToObj(i -> DEFAULT.get())
                .collect(Collectors.toMap(Function.identity(), v -> 1L, Long::sum)).values().stream().filter(c -> c > 1)
                .findAny().isPresent());
    }
}
