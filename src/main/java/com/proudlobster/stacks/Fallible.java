package com.proudlobster.stacks;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.proudlobster.stacks.structure.Tuple;
import com.proudlobster.stacks.utility.StringTemplate;
import com.proudlobster.stacks.utility.Template;

/**
 * A function which produces an exception supplier, for the purposes of
 * describing errors which could occur.
 */
@FunctionalInterface
public interface Fallible extends Supplier<Fallible.StacksException>,
        Function<Tuple<? extends Object>, Supplier<Fallible.StacksException>> {

    /**
     * Primary exception type used by this library.
     * 
     * This library recommends using RuntimeExceptions exclusively to avoid
     * exception tolerance.
     */
    static class StacksException extends RuntimeException {
        private static Throwable EMPTY = new Throwable("No further cause.");

        /**
         * @param s a tuple representing the source of the exception
         * @param t an exception message template string
         */
        public StacksException(final Tuple<? extends Object> s, final String t) {
            this(StringTemplate.of(t).bind(s), s.firstOf(Throwable.class));
        }

        /**
         * @param s a tuple representing the source of the exception
         * @param t an exception message template string
         * @param h the cause of the exception
         */
        public StacksException(final Tuple<? extends Object> s, final String t, final Throwable h) {
            this(StringTemplate.of(t).bind(s), h);
        }

        /**
         * @param t an exception message template
         */
        public StacksException(final Template<String> t) {
            this(t.get());
        }

        /**
         * @param t an exception message template
         * @param h the cause of the exception
         */
        public StacksException(final Template<String> t, final Throwable h) {
            this(t.get(), h);
        }

        /**
         * @param t an exception message template
         * @param h the cause of the exception
         */
        public StacksException(final Template<String> t, final Optional<Throwable> h) {
            this(t.get(), h.orElse(EMPTY));
        }

        /**
         * @param m an exception message
         */
        public StacksException(final String m) {
            super(m);
        }

        /**
         * @param m an exception message
         * @param t the cause of the exception
         */
        public StacksException(final String m, final Throwable t) {
            super(m, t);
        }

    }

    /**
     * A function which may throw an exception.
     */
    @FunctionalInterface
    static interface RiskyFunction<A, B> {
        Fallible FAILED = Fallible.of("Function attempt failed with parameter ''{0}''.");

        B apply(A a) throws Exception;
    }

    /**
     * A consumer which may throw an exception.
     */
    @FunctionalInterface
    static interface RiskyConsumer<O> {
        Fallible FAILED = Fallible.of("Consumer attempt failed with parameter ''{0}''.");

        void accept(O o) throws Exception;

        default Consumer<O> andThen(final Consumer<O> c) {
            return ((Consumer<O>) o -> Fallible.attemptAccept(this, o)).andThen(c);
        }
    }

    /**
     * A supplier which may throw an exception.
     */
    @FunctionalInterface
    static interface RiskySupplier<O> {
        Fallible FAILED = Fallible.of("Supplier attempt failed.");

        O get() throws Exception;
    }

    /**
     * A runnable which may throw an exception.
     */
    @FunctionalInterface
    static interface RiskyRunnable {
        Fallible FAILED = Fallible.of("Runnable attempt failed.");

        void run() throws Exception;
    }

    /**
     * @param <A> the input type of the function
     * @param <B> the output type of the function
     * @param f   the function to attempt
     * @param a   the function input to attempt
     * @return the function output
     */
    static <A, B> B attemptApply(final RiskyFunction<A, B> f, final A a) {
        try {
            return f.apply(a);
        } catch (final Exception e) {
            RiskyFunction.FAILED.throwIt(a, e);
            return null; // unreachable
        }
    }

    /**
     * @param <O> the input type of the consumer
     * @param c   the consumer to attempt
     * @param o   the consumer input to attempt
     */
    static <O> void attemptAccept(final RiskyConsumer<O> c, final O o) {
        try {
            c.accept(o);
        } catch (final Exception e) {
            RiskyConsumer.FAILED.throwIt(o, e);
        }
    }

    /**
     * @param <O> the output type of the supplier
     * @param s   the supplier to attempt
     * @return the supplier output
     */
    static <O> O attemptGet(final RiskySupplier<O> s) {
        try {
            return s.get();
        } catch (final Exception e) {
            RiskySupplier.FAILED.throwIt(e);
            return null; // unreachable
        }
    }

    /**
     * @param r the runnable to attempt
     */
    static void attemptRun(final RiskyRunnable r) {
        try {
            r.run();
        } catch (final Exception e) {
            RiskyRunnable.FAILED.throwIt(e);
        }
    }

    /**
     * @param m an exception message template string
     * @return a fallible instance for that template
     */
    static Fallible of(final String m) {
        return t -> () -> new StacksException(t, m);
    }

    /**
     * @return an exception described by this
     */
    @Override
    default StacksException get() {
        return apply().get();
    }

    /**
     * @param os elements of the tuple representing the source of the exception
     * @return an exception supplier for that tuple
     */
    default Supplier<StacksException> apply(final Object... os) {
        return apply(Tuple.of(os));
    }

    /**
     * @param os elements of the tuple representing the source of the exception
     * @throws StacksException the exception described by this, always
     */
    default void throwIt(final Object... os) throws StacksException {
        throw apply(os).get();
    }
}
