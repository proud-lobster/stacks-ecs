package com.proudlobster.stacks.ecp;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import com.proudlobster.stacks.structure.Couple;
import com.proudlobster.stacks.structure.Tuple;

/**
 * Performs some game operation on Entities with a given Component.
 */
@FunctionalInterface
public interface Processor {

    /**
     * A function which performs some operation on an Entity, resulting in a
     * Transaction.
     */
    @FunctionalInterface
    public static interface ProcessorFunction extends Function<Entity, Transaction> {

    }

    /**
     * @param c the Component this Processor handles
     * @param f the function operation performed
     * @return the specified Processor
     */
    public static Processor of(final Component c, final ProcessorFunction f) {
        return () -> Couple.of(c, f);
    }

    /**
     * @param c  the Component this Processor handles
     * @param ps the subprocessors for this Processor to run
     * @return the specified Processor
     */
    public static Processor of(final Component c, final Processor... ps) {
        final ProcessorFunction pf = e -> Arrays.stream(ps).map(p -> p.process(e)).reduce(Transaction::compose)
                .orElse(Transaction.start());
        return Processor.of(c, pf);
    }

    /**
     * @return internal elements of the Processor
     */
    Tuple<Object> delegate();

    /**
     * @return the Component this Processor handles
     */
    default Component component() {
        return delegate().requiredFirstOf(Component.class);
    }

    /**
     * @return the function operation performed
     */
    default ProcessorFunction function() {
        return delegate().requiredFirstOf(ProcessorFunction.class);
    }

    /**
     * @param e an Entity to process
     * @return a Transaction resulting from the operation
     */
    default Transaction process(final Entity e) {
        return Optional.of(e).filter(n -> n.is(component())).map(this.function()).orElse(Transaction.start());
    }

}
