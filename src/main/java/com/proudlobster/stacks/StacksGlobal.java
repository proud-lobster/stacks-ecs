package com.proudlobster.stacks;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.ManagedEntity;
import com.proudlobster.stacks.ecp.ManagedTransaction;

public interface StacksGlobal {

    String ERR_NO_STACKS = "Stacks instance could not be loaded.";
    Future<Stacks> SINGLETON = CompletableFuture.completedFuture(Stacks.create());

    static Stacks stacks() {
        try {
            return SINGLETON.get();
        } catch (CancellationException | ExecutionException | InterruptedException e) {
            throw new Fallible.StacksException(ERR_NO_STACKS, e);
        }
    }

    /**
     * @return a new managed transaction
     */
    static ManagedTransaction $() {
        return stacks().startTransaction();
    }

    /**
     * @param c  at least one component to find entities matching
     * @param cs additional components to match
     * @return the stream of managed entities matching those components
     */
    static Stream<ManagedEntity> $(final Component c, final Component... cs) {
        return $(Stream.of(cs, new Component[] { c }).flatMap(Stream::of).toArray(Component[]::new));
    }

    /**
     * @param cs components to find entities matching
     * @return the stream of managed entities matching those components
     */
    static Stream<ManagedEntity> $(final Component[] cs) {
        return stacks().$(cs);
    }

    /**
     * @param e  at least one entity ID to find the entities for
     * @param es additional entity IDs to find
     * @return the stream of managed entities for those IDs
     */
    static Stream<ManagedEntity> $(final Long e, final Long... es) {
        return $(Stream.of(es, new Long[] { e }).flatMap(Stream::of).toArray(Long[]::new));
    }

    /**
     * @param cs entity IDs to find the entities for
     * @return the stream of managed entities for those IDs
     */
    static Stream<ManagedEntity> $(final Long[] cs) {
        return stacks().$(cs);
    }

    /**
     * @param <T> the type of the library element to find
     * @param c   the type reference of the element to find
     * @param s   the index for the element to find
     * @return the element matching the type and index in the library
     */
    static <T> Optional<T> $(final Class<T> c, final String s) {
        return stacks().$(c, s);
    }

    /**
     * @return next available entity ID
     */
    static Long nextId() {
        return stacks().nextId();
    }
}
