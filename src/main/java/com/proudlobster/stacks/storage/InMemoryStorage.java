package com.proudlobster.stacks.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.Entity;
import com.proudlobster.stacks.structure.Dictionary;

/**
 * Entity reader and writer for keeping entities in memory.
 * 
 * Uses a pair of maps to retain state.
 */
@FunctionalInterface
public interface InMemoryStorage extends EntityReader, EntityWriter {

    /**
     * @return new in-memory storage
     */
    public static InMemoryStorage of() {
        final Map<Long, Map<String, Object>> em = new HashMap<>();
        final Map<String, Set<Long>> cm = new HashMap<>();
        return (r, w) -> w ? writeInternal(r, em, cm) : readInternal(r, em, cm);
    }

    private static Stream<Entity> readInternal(final EntityStorageDescriptor r, final Map<Long, Map<String, Object>> em,
            final Map<String, Set<Long>> cm) {
        return r.identifier().map(i -> Stream.of(i))
                .orElseGet(() -> r.component().map(c -> cm.get(c)).filter(Objects::nonNull).map(s -> s.stream())
                        .orElse(Stream.empty()))
                .map(i -> em.get(i)).filter(Objects::nonNull).map(m -> (Entity) () -> Dictionary.of(m));
    }

    private static Stream<Entity> writeInternal(final EntityStorageDescriptor r,
            final Map<Long, Map<String, Object>> em, final Map<String, Set<Long>> cm) {
        Optional.of(r).filter(d -> d.active()).ifPresentOrElse(d -> {
            em.computeIfAbsent(r.identifier().get(), x -> new ConcurrentHashMap<>()).put(r.component().get(),
                    r.value());
            cm.computeIfAbsent(r.component().get(), x -> ConcurrentHashMap.newKeySet()).add(r.identifier().get());
        }, () -> {
            em.get(r.identifier().get()).remove(r.component().get());
            cm.get(r.component().get()).remove(r.identifier().get());
        });

        // TODO add test for expiration
        r.component().filter(c -> Component.Core.EXPIRED.name().equals(c))
                .ifPresent(c -> expireInternal(r.identifier().get(), em, cm));
        return Stream.of();
    }

    private static void expireInternal(final Long id, final Map<Long, Map<String, Object>> em,
            final Map<String, Set<Long>> cm) {
        em.get(id).keySet().stream().map(c -> cm.get(c)).forEach(s -> s.remove(id));
        em.remove(id);
    }

    /**
     * @param r descriptor of the entity data to manage
     * @param w write the data if true, read otherwise
     * @return a stream of matching entities if w is false, empty otherwise
     */
    Stream<Entity> handle(final EntityStorageDescriptor r, final boolean w);

    @Override
    default Stream<Entity> read(EntityReader.Record r) {
        return handle(r, false);
    }

    @Override
    default void write(EntityWriter.Record r) {
        handle(r, true);
    }
}
