package com.proudlobster.stacks.utility;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proudlobster.stacks.Managed;
import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.ecp.ManagedTransaction;
import com.proudlobster.stacks.structure.Couple;
import com.proudlobster.stacks.utility.EntityTemplate.Segment;

/**
 * Managed instance of an entity template.
 */
@FunctionalInterface
public interface ManagedEntityTemplate extends Managed, Template<ManagedTransaction> {

    /**
     * @param t template to manage
     * @param s Stacks instance
     * @return entity template managed by the given Stacks instance
     */
    public static ManagedEntityTemplate of(final EntityTemplate t, final Stacks s) {
        return () -> Couple.of(t, s);
    }

    /**
     * @return segments from the delegate template
     */
    default Stream<Segment> segments() {
        return instance(EntityTemplate.class).segments();
    }

    @Override
    default ManagedTransaction get(final Map<String, Object> args) {
        final Map<String, Long> ids = segments().collect(Collectors.groupingBy(Segment::name)).keySet().stream()
                .collect(Collectors.toMap(Function.identity(), n -> stacks().nextId()));
        final Map<String, Object> merged = Stream.concat(ids.entrySet().stream(), args.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return ManagedTransaction.of(instance(EntityTemplate.class).get(merged), stacks());
    }
}
