package com.proudlobster.stacks.ecp;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.proudlobster.stacks.Managed;
import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.storage.EntityWriter;
import com.proudlobster.stacks.structure.Couple;
import com.proudlobster.stacks.utility.EntityTemplate;

/**
 * A transaction associated with a Stacks instance.
 */
@FunctionalInterface
public interface ManagedTransaction extends Managed, Transaction {

    /**
     * @param t the transaction to be associated
     * @param s the Stacks instance to be associated
     * @return the managed transaction associating them
     */
    public static ManagedTransaction of(final Transaction t, final Stacks s) {
        return () -> Couple.of(t, s);
    }

    /**
     * @return the unmanaged transaction
     */
    default Transaction transaction() {
        return instance(Transaction.class);
    }

    /**
     * @param t a new transaction to manage
     * @return the managed transaction associating t with the stacks instance
     */
    default ManagedTransaction manage(final Transaction t) {
        return ManagedTransaction.of(t, stacks());
    }

    /**
     * @return a transaction composing entity creation using the next entity ID
     *         known by the Stacks instance
     */
    default ManagedTransaction createEntity() {
        return manage(createEntity(stacks().nextId()));
    }

    /**
     * @return a transaction composing transient entity creation using the next
     *         entity ID known by the Stacks instance
     */
    default ManagedTransaction createTransientEntity() {
        return manage(createTransientEntity(stacks().nextId()));
    }

    /**
     * @param cs the flag components - no values - of the entity to be created
     * @return a transaction composing entity creation using the next entity ID
     *         known by the Stacks instance, with the given components
     */
    default ManagedTransaction createEntity(final Component... cs) {
        return Optional.of(stacks().nextId())
                .map(l -> Arrays.stream(cs)
                        .map(c -> (Function<Transaction, Transaction>) (t -> t.assignComponent(l, c)))
                        .reduce(t -> t.createEntity(l), (r, a) -> r.andThen(a)))
                .map(t -> t.apply($())).map(this::manage).get();
    }

    /**
     * @param cs the flag components - no values - of the entity to be created
     * @return a transaction composing transient entity creation using the next
     *         entity ID known by the Stacks instance, with the given components
     */
    default ManagedTransaction createTransientEntity(final Component... cs) {
        return Optional.of(stacks().nextId())
                .map(l -> Arrays.stream(cs)
                        .map(c -> (Function<Transaction, Transaction>) (t -> t.assignComponent(l, c)))
                        .reduce(t -> t.createTransientEntity(l), (r, a) -> r.andThen(a)))
                .map(t -> t.apply($())).map(this::manage).get();
    }

    /**
     * @param t    name of file (no extension) in templates directory to use for
     *             template
     * @param args arguments for the template
     * @return managed template to create entities
     */
    default ManagedTransaction createEntitiesFromTemplate(final String t, final Object... args) {
        return createEntitiesFromTemplate(EntityTemplate.fromProperties(t), args);
    }

    @Override
    default void commit(final EntityWriter w) {
        transaction().commit(w);
    }

    @Override
    default void commit() {
        stacks().commitTransaction(transaction());
    }

    @Override
    default ManagedTransaction compose(final Transaction t) {
        return manage(transaction().compose(t));
    }

    @Override
    default ManagedTransaction andThen(Transaction t) {
        return manage(transaction().andThen(t));
    }

    @Override
    default ManagedTransaction createEntity(final Long i) {
        return manage(transaction().createEntity(i));
    }

    @Override
    default ManagedTransaction createTransientEntity(final Long i) {
        return manage(transaction().createTransientEntity(i));
    }

    @Override
    default ManagedTransaction createEntitiesFromTemplate(final EntityTemplate t, final Object... args) {
        return this.compose(t.manage(stacks()).get(args));
    }

    @Override
    default ManagedTransaction assignComponent(final Long i, final Component c, final String s) {
        return manage(transaction().assignComponent(i, c, s));
    }

    @Override
    default ManagedTransaction assignComponentString(Long i, Component c, Supplier<String> s) {
        return manage(transaction().assignComponentString(i, c, s));
    }

    @Override
    default ManagedTransaction assignComponent(final Long i, final Component c, final Long l) {
        return manage(transaction().assignComponent(i, c, l));
    }

    @Override
    default ManagedTransaction assignComponentLong(Long i, Component c, Supplier<Long> l) {
        return manage(transaction().assignComponentLong(i, c, l));
    }

    @Override
    default ManagedTransaction assignComponent(final Long i, final Component c) {
        return manage(transaction().assignComponent(i, c));
    }

    @Override
    default ManagedTransaction removeComponent(final Long i, final Component c) {
        return manage(transaction().removeComponent(i, c));
    }

}
