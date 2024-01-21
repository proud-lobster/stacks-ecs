package com.proudlobster.stacks.structure;

import java.util.Optional;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;

/**
 * A special case of Tuple containing one element.
 */
@FunctionalInterface
public interface Single<E> extends Tuple<E> {

    /**
     * @param <E> the type of the element
     * @param f   the first element
     * @return a single containing both elements
     */
    public static <E> Single<E> of(final E f) {
        return i -> Optional.ofNullable(i).filter(POSITION::equals).map(x -> f);
    }

    Fallible ERR_MISSING_ELEMENT = Fallible.of("Single missing element.");

    Long POSITION = 0L;
    int COUNT = 1;

    @Override
    Optional<E> get(final long i);

    /**
     * @return the element
     */
    default E first() {
        return get(POSITION).orElseThrow(ERR_MISSING_ELEMENT);
    }

    @Override
    default Stream<E> stream() {
        return Stream.of(first());
    }

    @Override
    default long count() {
        return COUNT;
    }
}
