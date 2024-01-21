package com.proudlobster.stacks.utility;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A supplier for obtaining new unique identifiers.
 */
@FunctionalInterface
public interface IdentifierSequence extends Supplier<Long> {

    /**
     * @param s seed value
     * @return a new sequence starting from the seed
     */
    public static IdentifierSequence create(final Long s) {
        final AtomicLong c = new AtomicLong(s);
        return () -> c.incrementAndGet();
    }

    /**
     * @return a new sequence seeded with system time
     */
    public static IdentifierSequence create() {
        return create(System.currentTimeMillis() * 1000);
    }
}
