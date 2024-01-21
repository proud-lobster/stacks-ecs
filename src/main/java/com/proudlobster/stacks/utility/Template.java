package com.proudlobster.stacks.utility;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.structure.Tuple;

/**
 * A mechanism for binding arguments to a template to produce a new structure.
 */
@FunctionalInterface
public interface Template<T> extends Supplier<T> {

    Fallible ERR_ALREADY_BOUND = Fallible.of("Attempting to bind new arguments to a template already bound.");

    /**
     * @param as the named arguments to fill in to the template
     * @return the template completed as a new structure with the given arguments
     */
    T get(final Map<String, Object> as);

    /**
     * @param as the arguments to fill in to the template, named by index
     * @return the template completed as a new structure with the given arguments
     */
    default T get(final Object... as) {
        return get(IntStream.range(0, as.length).mapToObj(i -> i)
                .collect(Collectors.toMap(i -> i.toString(), i -> as[i])));
    }

    /**
     * @return the template completed as a new structure
     */
    default T get() {
        return get(new Object[0]);
    }

    /**
     * @param t the arguments to fill in to the template, named by index
     * @return the template completed as a new structure with the given arguments
     */
    default T get(final Tuple<? extends Object> t) {
        return get(t.stream().toArray());
    }

    /**
     * @param t the arguments to fill in to the template, named by index
     * @return a template bound to those arguments for later resolution
     */
    default Template<T> bind(final Tuple<? extends Object> t) {
        return as -> Optional.of(as).map(m -> m.values()).filter(x -> x.size() == 0).map(x -> this.get(t))
                .orElseThrow(ERR_ALREADY_BOUND);
    }

    /**
     * @param t the arguments to fill in to the template, named by index
     * @return a template bound to those arguments for later resolution
     */
    default Template<T> bind(final Object... rs) {
        return as -> Optional.of(as).map(m -> m.values()).filter(x -> x.size() == 0).map(x -> this.get(rs))
                .orElseThrow(ERR_ALREADY_BOUND);
    }

}
