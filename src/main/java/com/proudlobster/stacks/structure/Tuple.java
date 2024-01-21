package com.proudlobster.stacks.structure;

import java.util.Optional;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;

/**
 * A representation of a sequence of data elements.
 */
@FunctionalInterface
public interface Tuple<E> {

    Fallible ERR_NONE_OF_TYPE = Fallible.of("Could not find element of type {0}.");

    /**
     * @param <E> common type of the elements
     * @param es  elements to wrap in a tuple
     * @return a tuple wrapping the provided elements
     */
    @SafeVarargs
    public static <E> Tuple<E> of(final E... es) {
        return () -> Stream.of(es);
    }

    /**
     * @return a stream of all elements in the tuple
     */
    Stream<E> stream();

    /**
     * @param i the position of the element to retrieve
     * @return that element if it exists
     */
    default Optional<E> get(final long i) {
        return Optional.of(i).filter(x -> x >= 0).flatMap(x -> stream().skip(i).findFirst());
    }

    /**
     * @return the first element in the tuple if it exists
     */
    default Optional<E> head() {
        return stream().findFirst();
    }

    /**
     * @return the last element in the tuple if it exists
     */
    default Optional<E> tail() {
        return get(count() - 1);
    }

    /**
     * @param <N> generic type of the output tuple
     * @param c   target type of output tuple elements
     * @return a filtered view of this tuple restricted to elements which match the
     *         target type
     */
    default <N> Tuple<N> morph(final Class<N> c) {
        return () -> stream().filter(c::isInstance).map(c::cast);
    }

    /**
     * @param <N> generic type of the output element
     * @param c   target type of the output element
     * @return the first element in the tuple which matches the target type if it
     *         exists
     */
    default <N> Optional<N> firstOf(final Class<N> c) {
        return morph(c).head();
    }

    /**
     * @param <N> generic type of the output element
     * @param c   target type of the output element
     * @return the first element in the tuple which matches the target type or
     *         throws an error if one does not exist
     */
    default <N> N requiredFirstOf(final Class<N> c) {
        return firstOf(c).orElseThrow(ERR_NONE_OF_TYPE.apply(c.getName()));
    }

    /**
     * @return the number of elements in the tuple
     */
    default long count() {
        return stream().count();
    }

}
