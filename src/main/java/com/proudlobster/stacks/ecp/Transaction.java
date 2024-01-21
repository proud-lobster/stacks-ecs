package com.proudlobster.stacks.ecp;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.storage.EntityWriter;
import com.proudlobster.stacks.utility.EntityTemplate;

/**
 * Represents a change in game state to be committed. This can be a composite of
 * multiple changes.
 */
@FunctionalInterface
public interface Transaction extends Consumer<EntityWriter> {

    /**
     * A state used to control the availability of staging transactions.
     */
    @FunctionalInterface
    public static interface Lock {

        /**
         * @return a new lock
         */
        public static Lock create() {
            return AtomicBoolean::new;
        }

        boolean LOCKED = true;
        boolean UNLOCKED = false;

        /**
         * @return the atomic boolean used to keep the lock state
         */
        AtomicBoolean getLock();

        /**
         * Locks the lock.
         * 
         * @return true if it was locked, false otherwise
         */
        default boolean lock() {
            return getLock().compareAndSet(UNLOCKED, LOCKED);
        }

        /**
         * @return a transaction that will unlock the lock
         */
        default Transaction unlock() {
            return w -> getLock().set(UNLOCKED);
        }

        /**
         * @return true if the lock is locked, false if unlocked
         */
        default boolean locked() {
            return getLock().get();
        }
    }

    Fallible ERR_CANNOT_COMMIT = Fallible.of("This is not a managed transaction. It cannot be committed freely.");
    Fallible ERR_CANNOT_LOCK = Fallible.of("You cannot start a new transaction chain until the last one completes.");
    Fallible ERR_NOT_STRING = Fallible.of("''{0}'' is not a string component.");
    Fallible ERR_NOT_LONG = Fallible.of("''{0}'' is not a long component.");
    Fallible ERR_NOT_FLAG = Fallible.of("''{0}'' is not a flag component.");

    /**
     * @param l the lock to contorl the transaction
     * @return a transaction that unlocks the lock, used to compose with further
     *         transactions
     */
    public static Transaction start(final Lock l) {
        return Optional.of(l).filter(Lock::lock).map(Lock::unlock).orElseThrow(ERR_CANNOT_LOCK);
    }

    /**
     * @return a transaction that unlocks a unique internal lock, used to compose
     *         with further transactions
     */
    public static Transaction start() {
        return start(Lock.create());
    }

    /**
     * @param w the entity writer to apply the transaction to
     */
    void commit(final EntityWriter w);

    /**
     * Commit the transaction with a prior assigned writer.
     */
    default void commit() {
        throw ERR_CANNOT_COMMIT.get();
    }

    /**
     * @param t the transaction to execute before the other one
     * @return a transaction which applies the parameter transaction and then the
     *         previous transaction
     */
    default Transaction compose(final Transaction t) {
        return t.andThen(this)::accept;
    }

    /**
     * @param t the transaction to execute after this one
     * @return a transaction which applies the previous transaction and then the
     *         parameter transaction
     */
    default Transaction andThen(final Transaction t) {
        return Consumer.super.andThen(t)::accept;
    }

    /**
     * @param i the ID of the entity to create
     * @return a transaction composing the entity creation
     */
    default Transaction createEntity(final Long i) {
        return ((Transaction) (w -> w.writeEntity(i))).andThen(this)::accept;
    }

    /**
     * @param i the ID of the entity to create
     * @return a transaction composing the transient entity creation
     */
    default Transaction createTransientEntity(final Long i) {
        return this.compose(w -> w.writeTransientEntity(i));
    }

    /**
     * @param t    the template to create the entities from
     * @param args template arguments
     * @return a transaction to create the associated entities
     */
    default Transaction createEntitiesFromTemplate(final EntityTemplate t, Object... args) {
        return this.compose(t.get(args));
    }

    /**
     * @param i the ID of the entity to update
     * @param c the component to assign to the entity
     * @param s the string value to assign
     * @return a transaction composing the component assignment
     */
    default Transaction assignComponent(final Long i, final Component c, final String s) {
        return Optional.of(c.type().valueType).filter(Optional::isPresent).filter(t -> t.get().equals(String.class))
                .map(t -> this.compose(w -> w.writeAssignComponent(i, c.name(), s)))
                .orElseThrow(ERR_NOT_STRING.apply(c));
    }

    /**
     * @param i the ID of the entity to update
     * @param c the component to assign to the entity
     * @param s the string value to assign
     * @return a transaction composing the component assignment
     */
    default Transaction assignComponentString(final Long i, final Component c, final Supplier<String> s) {
        final Transaction doit = w -> w.writeAssignComponent(i, c.name(), s.get());
        return this.compose(Optional.of(c.type().valueType).filter(Optional::isPresent)
                .filter(t -> t.get().equals(String.class)).map(t -> doit).orElseThrow(ERR_NOT_STRING.apply(c)));
    }

    /**
     * @param i the ID of the entity to update
     * @param c the component to assign to the entity
     * @param l the long value to assign
     * @return a transaction composing the component assignment
     */
    default Transaction assignComponent(final Long i, final Component c, final Long l) {
        return assignComponentLong(i, c, () -> l);
    }

    /**
     * @param i the ID of the entity to update
     * @param c the component to assign to the entity
     * @param l the long value to assign
     * @return a transaction composing the component assignment
     */
    default Transaction assignComponentLong(final Long i, final Component c, final Supplier<Long> l) {
        final Transaction doit = w -> w.writeAssignComponent(i, c.name(), l.get());
        return this.compose(Optional.of(c.type().valueType).filter(Optional::isPresent)
                .filter(t -> t.get().equals(Long.class)).map(t -> doit).orElseThrow(ERR_NOT_LONG.apply(c)));
    }

    /**
     * @param i the ID of the entity to update
     * @param c the component to assign to the entity
     * @return a transaction composing the component assignment
     */
    default Transaction assignComponent(final Long i, final Component c) {
        return Optional.of(c.type().valueType).filter(Optional::isEmpty)
                .map(t -> this.compose(w -> w.writeAssignComponent(i, c.name()))).orElseThrow(ERR_NOT_FLAG.apply(c));
    }

    /**
     * @param i the ID of the entity to update
     * @param c the component to remove from the entity
     * @return a transaction composing the component removal
     */
    default Transaction removeComponent(final Long i, final Component c) {
        return this.compose(w -> w.writeRemoveComponent(i, c.name()));
    }

    @Override
    default void accept(EntityWriter w) {
        commit(w);
    }

}
