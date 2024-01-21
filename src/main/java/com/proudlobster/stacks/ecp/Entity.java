package com.proudlobster.stacks.ecp;

import java.util.Optional;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.structure.Dictionary;
import com.proudlobster.stacks.utility.StringAdapter;

/**
 * The core game data structure; all game objects should be entities.
 * 
 * An entity is comprised of any number of components and component values, and
 * nothing else. Every characteristic of an entity is repsentened in components,
 * and the entity's construction is essentially a dictionary of components.
 */
@FunctionalInterface
public interface Entity extends Comparable<Entity> {

    /**
     * An entity identifier reserved as unusable.
     */
    Long INVALID_ID = -1L;

    Fallible ERR_NO_IDENTITY = Fallible.of("No identity found for entity.");

    /**
     * The dictionary containing the entity components.
     */
    Dictionary<Object> delegate();

    /**
     * @return the identity value for the entity
     */
    default Long identifier() {
        return longValue(Component.Core.IDENTITY).orElseThrow(ERR_NO_IDENTITY);
    }

    /**
     * @param c a component
     * @return true if the entity is associated with this component
     */
    default boolean is(final Component c) {
        return is(c.name());
    }

    /**
     * @param c a component name
     * @return true if the entity is associated with this component
     */
    default boolean is(final String c) {
        return delegate().lookup(c).isPresent();
    }

    /**
     * @param <T> the value type of the component
     * @param c   a component
     * @param l   the object value type of the component
     * @return the value of that component for this entity
     */
    default <T> Optional<T> value(final Component c, final Class<T> l) {
        return c.type().valueType.filter(l::equals).flatMap(t -> delegate().lookup(c.name(), l));
    }

    /**
     * @param c a component
     * @return the string value of that component
     */
    default Optional<String> stringValue(final Component c) {
        return value(c, String.class);
    }

    /**
     * @param c a component
     * @return the long value of that component
     */
    default Optional<Long> longValue(final Component c) {
        return value(c, Long.class);
    }

    /**
     * @param c a component
     * @return a stream of long values for that component
     */
    default Stream<Long> referenceValues(final Component c) {
        return stringValue(c).map(StringAdapter::of).map(StringAdapter::splitToNumbers)
                .orElse(longValue(c).map(Stream::of).orElse(Stream.empty()));
    }

    @Override
    default int compareTo(final Entity e) {
        return identifier().compareTo(e.identifier());
    }

}
