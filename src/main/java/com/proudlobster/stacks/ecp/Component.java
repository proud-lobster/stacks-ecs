package com.proudlobster.stacks.ecp;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.structure.Couple;

/**
 * Defines a characteristic that an entity can have.
 */
@FunctionalInterface
public interface Component extends Couple<String> {

    /**
     * The various values that a component may represent.
     */
    public static enum DataType {

        /**
         * Component does not contain any value.
         */
        NONE(Optional.empty()),

        /**
         * Component contains character value.
         */
        STRING(Optional.of(String.class)),

        /**
         * Component contains numeric value.
         */
        NUMBER(Optional.of(Long.class)),

        /**
         * Component references another entity.
         */
        REFERENCE(Optional.of(Long.class)),

        /**
         * Component references multiple entities.
         */
        MULTIREF(Optional.of(String.class));

        /**
         * The set of valid names for this, to simplify validation.
         */
        public static final Set<String> VALID_NAMES = Arrays.stream(values()).map(DataType::name)
                .collect(Collectors.toUnmodifiableSet());

        /**
         * The value type of the component, if present.
         */
        public Optional<Class<?>> valueType;

        /**
         * @param valueType The value type of the component, if present
         */
        DataType(final Optional<Class<?>> valueType) {
            this.valueType = valueType;
        }

    }

    /**
     * A set of essential components used to manage all entities.
     */
    public static enum Core implements Component {

        /**
         * The unique identifier for the entity. All entities must have this.
         */
        IDENTITY(DataType.REFERENCE),

        /**
         * Marks an entity that should not be held in persistent storage.
         */
        TRANSIENT(DataType.NONE),

        /**
         * Marks an entity that should no longer be used an can be cleaned up depending
         * on the storage requirements.
         */
        EXPIRED(DataType.NONE);

        Couple<String> couple;

        Core(final DataType type) {
            this.couple = Couple.of(name(), type.name());
        }

        @Override
        public Optional<String> get(final long i) {
            return couple.get(i);
        }
    }

    /**
     * @param n the component name
     * @param t the component type
     * @return a component representing the name and type
     */
    public static Component of(final String n, final DataType t) {
        return of(n, t.name());
    }

    /**
     * @param n the component name
     * @param t the component type
     * @return a component representing the name and type
     */
    public static Component of(final String n, final String t) {
        final Couple<String> c = Couple.of(n, t);
        return i -> c.get(i);
    }

    /**
     * @param n the component name
     * @return a flag component representing the name
     */
    public static Component flagOf(final String n) {
        return of(n, DataType.NONE);
    }

    Fallible ERR_NAME_NOT_FOUND = Fallible.of("Component name not found.");
    Fallible ERR_TYPE_NOT_FOUND = Fallible.of("Component data type not found.");
    Fallible ERR_COULD_NOT_RESOLVE = Fallible.of("Could not resolve component with name ''{0}''' and type ''{1}''.");

    /**
     * Position of name in tuple: {@value}
     */
    int NAME_POSITION = 0;

    /**
     * Position of type in tuple: {@value}
     */
    int TYPE_POSITION = 1;

    /**
     * @return the component name
     */
    default String name() {
        return get(NAME_POSITION).orElseThrow(ERR_NAME_NOT_FOUND.apply(this));
    }

    /**
     * @return the component type
     */
    default DataType type() {
        return get(TYPE_POSITION).filter(DataType.VALID_NAMES::contains).map(DataType::valueOf)
                .orElseThrow(ERR_TYPE_NOT_FOUND.apply(this));
    }

}
