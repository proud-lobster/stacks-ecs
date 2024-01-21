package com.proudlobster.stacks.ecp;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proudlobster.stacks.Managed;
import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.structure.Couple;
import com.proudlobster.stacks.structure.Dictionary;

/**
 * An entity associated with a Stacks instance.
 */
@FunctionalInterface
public interface ManagedEntity extends Managed, Entity {

    /**
     * @param e the entity to associated
     * @param s the stacks instance to associate
     * @return the managed entity associating them
     */
    public static ManagedEntity of(final Entity e, final Stacks s) {
        return () -> Couple.of(e, s);
    }

    /**
     * @return the unmanaged entity
     */
    default Entity entity() {
        return instance(Entity.class);
    }

    @Override
    default Dictionary<Object> delegate() {
        return entity().delegate();
    }

    default Optional<ManagedEntity> referenceEntity(final Component c) {
        return longValue(c).flatMap(i -> $(i).findAny());
    }

    default Stream<ManagedEntity> referenceEntities(final Component c) {
        return referenceValues(c).flatMap(i -> $(i));
    }

    default ManagedTransaction assignComponent(final Component c, final String s) {
        return $().assignComponent(identifier(), c, s);
    }

    default ManagedTransaction assignComponent(final Component c, final Long l) {
        return $().assignComponent(identifier(), c, l);
    }

    default ManagedTransaction assignComponent(final Component c) {
        return $().assignComponent(identifier(), c);
    }

    default ManagedTransaction removeComponent(final Component c) {
        return $().removeComponent(identifier(), c);
    }

    default ManagedTransaction alterValue(final Component c, final Long l) {
        return $().assignComponentLong(identifier(), c, () -> longValue(c).map(v -> v + l).orElse(l));
    }

    default ManagedTransaction addReference(final Component c, final Long l) {
        return $().assignComponentString(identifier(), c,
                () -> stringValue(c).map(v -> v + "|" + l).orElse(l.toString()));
    }

    default ManagedTransaction removeReference(final Component c, final Long l) {
        return $().assignComponentString(identifier(), c, () -> referenceValues(c).filter(v -> !v.equals(l))
                .map(v -> v.toString()).collect(Collectors.joining("|")));
    }

    default ManagedTransaction expire() {
        return assignComponent(Component.Core.EXPIRED);
    }
}
