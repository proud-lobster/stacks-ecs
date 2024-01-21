package com.proudlobster.stacks.utility;

import java.beans.PropertyDescriptor;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.structure.Configuration;

/**
 * Adapter class for working with objects in a functional manner.
 */
public interface ObjectAdapter<T> extends Supplier<T> {

    /**
     * @param <P> the type of object being wrapped
     * @param cp  the class of object being wrapped
     * @param cn  the class name of the object being wrapped, castable to P
     * @return an adapter wrapping a new instance of object with type named by cn
     */
    public static <P> ObjectAdapter<P> of(final Class<P> cp, final String cn) {
        return () -> Fallible.attemptGet(() -> cp.cast(of(Class.forName(cn)).get()));
    }

    /**
     * @param <P> the type of object being wrapped
     * @param cp  the class of object being wrapped
     * @return an adapter wrapping a new instance of object with type P
     */
    public static <P> ObjectAdapter<P> of(final Class<P> cp) {
        return () -> Fallible.attemptGet(() -> cp.getDeclaredConstructor().newInstance());
    }

    /**
     * @param <P> the type of object being wrapped
     * @param p   the object being wrapped
     * @return an adapter wrapping that object
     */
    public static <P> ObjectAdapter<P> of(final P p) {
        return () -> p;
    }

    /**
     * @param <P> the type of object being wrapped
     * @param d   the default object to wrap if no others are present
     * @param ps  the objects to attempt to wrap, using the first that is present
     * @return an adapter wrapping the first present object of ps or d if none of ps
     *         are present
     */
    @SafeVarargs
    public static <P> ObjectAdapter<P> ofFirstDefault(final P d, final Optional<P>... ps) {
        return () -> Stream.of(ps).filter(Optional::isPresent).map(Optional::get).findFirst().orElse(d);
    }

    /**
     * @param <P> the type of object being wrapped
     * @param ps  the objects to attempt to wrap, using the first that is present
     * @return an adapter wrapping the first present object of ps
     */
    @SafeVarargs
    public static <P> ObjectAdapter<P> ofFirst(final Optional<P>... ps) {
        return () -> Stream.of(ps).filter(Optional::isPresent).map(Optional::get).findFirst()
                .orElseThrow(ERR_NO_SUITABLE_VALUE);
    }

    Fallible ERR_NO_SUITABLE_VALUE = Fallible.of("Could not find at least one suitable value.");

    /**
     * @param c a configuration to set an object property with
     * @return a wrapper around the original adapter, with the property applied.
     *         Note this is a late-resolved method; the wrapped object is not
     *         modified until it is unwrapped.
     */
    default ObjectAdapter<T> setProperty(final Configuration c) {
        return setProperty(c.nameTail(), c.value());
    }

    /**
     * @param p the property name to set
     * @param v the value to set the property to
     * @return a wrapper around the original adapter, with the property applied.
     *         Note this is a late-resolved method; the wrapped object is not
     *         modified until it is unwrapped.
     */
    default ObjectAdapter<T> setProperty(final String p, final Object v) {
        return setProperty(p, v, get().getClass());
    }

    /**
     * @param p the property name to set
     * @param v the property value to set
     * @param t the type of the object to set the property on
     * @return a wrapper around the original adapter, with the property applied.
     *         Note this is a late-resolved method; the wrapped object is not
     *         modified until it is unwrapped.
     */
    default ObjectAdapter<T> setProperty(final String p, final Object v, final Class<?> t) {
        return () -> Fallible.attemptApply(o -> {
            new PropertyDescriptor(p, t).getWriteMethod().invoke(o, v);
            return o;
        }, get());
    }

}
