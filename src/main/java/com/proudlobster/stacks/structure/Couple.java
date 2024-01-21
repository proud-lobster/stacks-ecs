package com.proudlobster.stacks.structure;

import java.util.Optional;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;

/**
 * A special case of Tuple containing two elements.
 */
@FunctionalInterface
public interface Couple<E> extends Tuple<E> {

    /**
     * @param <E> the type of the elements
     * @param f   the first element
     * @param s   the second element
     * @return a couple containing both elements
     */
    static <E> Couple<E> of(final E f, final E s) {
        return i -> Single.of(f).get(i).map(Optional::ofNullable).orElse(Single.of(s).get(i - SECOND_POSITION));
    }

    Fallible ERR_MISSING_FIRST_ELEMENT = Fallible.of("Couple missing first element.");
    Fallible ERR_MISSING_SECOND_ELEMENT = Fallible.of("Couple missing second element.");

    int FIRST_POSITION = 0;
    int SECOND_POSITION = 1;
    int COUNT = 2;

    @Override
    Optional<E> get(final long i);

    /**
     * @return the first element of the couple
     */
    default E first() {
        return get(FIRST_POSITION).orElseThrow(ERR_MISSING_FIRST_ELEMENT);
    }

    /**
     * @return the second element of the couple
     */
    default E second() {
        return get(SECOND_POSITION).orElseThrow(ERR_MISSING_SECOND_ELEMENT);
    }

    @Override
    default Stream<E> stream() {
        return Stream.of(first(), second());
    }

    @Override
    default long count() {
        return COUNT;
    }
}
