package com.proudlobster.stacks.ecp;

import java.util.Arrays;
import java.util.function.Function;

import com.proudlobster.stacks.Managed;
import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.structure.Couple;
import com.proudlobster.stacks.structure.Tuple;

/**
 * Performs some game operation on managed Entities with a given Component.
 */
@FunctionalInterface
public interface ManagedProcessor extends Managed, Processor {

    /**
     * A function which performs some operation on a managed Entity, resulting in a
     * managed Transaction.
     */
    @FunctionalInterface
    public static interface ManagedProcessorFunction extends Function<ManagedEntity, ManagedTransaction> {
    }

    /**
     * @param c the Component this Processor handles
     * @param f the function operation performed
     * @param s the Stacks instance to manage this
     * @return the specified Processor
     */
    public static ManagedProcessor of(final Component c, final ManagedProcessorFunction f, final Stacks s) {
        return () -> Couple.of(Processor.of(c, e -> f.apply(ManagedEntity.of(e, s))), s);
    }

    /**
     * @param c  the Component this Processor handles
     * @param s  the Stacks instance to manage this
     * @param ps the subprocessors for this Processor to run
     * @return the specified Processor
     */
    public static ManagedProcessor of(final Component c, final Stacks s, final Processor... ps) {
        final ManagedProcessorFunction mpf = e -> Arrays.stream(ps).map(p -> p.process(e)).reduce(Transaction::compose)
                .map(t -> ManagedTransaction.of(t, s)).orElse(s.$());
        return ManagedProcessor.of(c, mpf, s);

    }

    /**
     * @return the Processor wrapped by this managed Processor
     */
    default Processor processor() {
        return instance(Processor.class);
    }

    @Override
    default Tuple<Object> delegate() {
        return processor().delegate();
    }

    /**
     * @return a Transaction resulting from performing the operation on all Entities
     *         with the matching Component
     */
    default ManagedTransaction process() {
        return $().$(component()).map(this::process).map(t -> ManagedTransaction.of(t, stacks()))
                .reduce(ManagedTransaction::compose).orElse($());
    }

}
