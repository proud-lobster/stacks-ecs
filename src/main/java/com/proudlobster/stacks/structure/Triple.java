package com.proudlobster.stacks.structure;

import java.util.Optional;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;

/**
 * A special case of Tuple containing three elements.
 */
@FunctionalInterface
public interface Triple<E> extends Tuple<E> {

    public static <E> Triple<E> of(final E f, final E s, final E t) {
        return i -> Single.of(f).get(i).map(Optional::ofNullable).orElse(Single.of(s).get(i - SECOND_POSITION))
                .map(Optional::ofNullable).orElse(Single.of(t).get(i - THIRD_POSITION));
    }

    Fallible ERR_MISSING_FIRST_ELEMENT = Fallible.of("Couple missing first element.");
    Fallible ERR_MISSING_SECOND_ELEMENT = Fallible.of("Couple missing second element.");
    Fallible ERR_MISSING_THIRD_ELEMENT = Fallible.of("Couple missing third element.");

    int FIRST_POSITION = 0;
    int SECOND_POSITION = 1;
    int THIRD_POSITION = 2;
    int COUNT = 3;

    @Override
    Optional<E> get(final long i);

    /**
     * @return the first element of the triple
     */
    default E first() {
        return get(FIRST_POSITION).orElseThrow(ERR_MISSING_FIRST_ELEMENT);
    }

    /**
     * @return the second element of the triple
     */
    default E second() {
        return get(SECOND_POSITION).orElseThrow(ERR_MISSING_SECOND_ELEMENT);
    }

    /**
     * @return the third element of the triple
     */
    default E third() {
        return get(THIRD_POSITION).orElseThrow(ERR_MISSING_THIRD_ELEMENT);
    }

    @Override
    default Stream<E> stream() {
        return Stream.of(first(), second(), third());
    }

    @Override
    default long count() {
        return COUNT;
    }
}
