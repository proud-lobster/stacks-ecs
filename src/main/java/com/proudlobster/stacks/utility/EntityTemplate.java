package com.proudlobster.stacks.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.Stacks;
import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.ecp.Transaction;
import com.proudlobster.stacks.structure.Triple;
import com.proudlobster.stacks.structure.Tuple;

/**
 * Template implementation for entities, producting a transaction that will
 * commit one or more entities.
 * 
 * The template is comprised of named segments, with each segment representing a
 * possible component for the entity to be built. Each segment must correspond
 * to a named Long argument providing the ID for the segment's entity.
 * 
 * A template can define any number of possible entities, with segments being
 * grouped by a name that associates them with the same entity.
 * 
 * Templates can be provided as properties files on the classpath under the
 * "templates" directory. These properties must be defined as...
 * 
 * {@code segmentName.COMPONENT_NAME=Component value}
 */
@FunctionalInterface
public interface EntityTemplate extends Template<Transaction> {

    /**
     * A segment of the template, representing one component to be assigned to the
     * entity.
     */
    @FunctionalInterface
    public static interface Segment {

        /**
         * @param n the name used to group segments belonging to the same entity
         * @param c the name of the component defined by the segment
         * @param v the value to be assigned to the component
         * @return a segment bound to the given parameters
         */
        public static Segment of(final String n, final String c, final String v) {
            return () -> Triple.of(n, c, v);
        }

        /**
         * @return the tuple containing the bound parameters
         */
        Triple<String> delegate();

        /**
         * @return the name given to group this segment with others for the same entity
         */
        default String name() {
            return delegate().first();
        }

        /**
         * @return the component for this segment
         */
        default String component() {
            return delegate().second().toUpperCase();
        }

        /**
         * @return the string value of the component for this segment, which may contain
         *         string template argument variables
         */
        default String value() {
            return delegate().third();
        }

        /**
         * @return true if the segment defines the TRANSIENT component
         */
        default boolean isTransient() {
            return component().equals(Component.Core.TRANSIENT.name());
        }

        /**
         * @param args named template arguments
         * @return a segment with all template arguments resolved, ready for entity
         *         construction
         */
        default ResolvedSegment resolve(final Map<String, Object> args) {
            return ResolvedSegment.resolve(this, args);
        }

    }

    /**
     * A segment where all of the argument variables have been resolved and is ready
     * for entity construction.
     */
    @FunctionalInterface
    public static interface ResolvedSegment extends Segment {

        Fallible ERR_ID_NOT_PROVIDED = Fallible.of("No identity argument found for this segment name.");
        Fallible ERR_NO_ID = Fallible.of("No identity found for this template segment.");
        Fallible ERR_NO_VALUE = Fallible.of("No resolved value found for this template segment.");

        /**
         * @param s    the segment to resolve
         * @param args the template args to resolve against the segment
         * @return a segment with all template arguments resolved, ready for entity
         *         construction
         */
        public static ResolvedSegment resolve(final Segment s, final Map<String, Object> args) {
            final String rv = StringTemplate.of(s.value()).get(args);
            final Long id = Optional.ofNullable(args.get(s.name())).filter(v -> v instanceof Long).map(Long.class::cast)
                    .orElseThrow(ERR_ID_NOT_PROVIDED);
            if (rv.isEmpty()) {
                return () -> Tuple.of(s.name(), Component.flagOf(s.component()), s.value(), id);
            } else if (rv.matches("-?\\d+")) {
                return () -> Tuple.of(s.name(), Component.of(s.component(), Component.DataType.NUMBER),
                        Long.parseLong(rv), id);
            } else {
                return () -> Tuple.of(s.name(), Component.of(s.component(), Component.DataType.STRING), rv, id);
            }
        }

        /**
         * @return the tuple containing the bound parameters
         */
        Tuple<Object> resolvedDelegate();

        /**
         * @return a tuple emulating the unbound segment
         */
        default Triple<String> delegate() {
            return Triple.of(resolvedDelegate().get(0).toString(),
                    resolvedDelegate().requiredFirstOf(Component.class).name(), resolvedDelegate().get(2).toString());
        }

        /**
         * @return the value of the segment with all variables resolved
         */
        default Object resolvedValue() {
            return resolvedDelegate().get(2).orElseThrow(ERR_NO_VALUE);
        }

        /**
         * @return the component represented by the segment
         */
        default Component resolvedComponent() {
            return resolvedDelegate().requiredFirstOf(Component.class);
        }

        /**
         * @return the identity component value of the segment
         */
        default Long resolvedIdentity() {
            return resolvedDelegate().morph(Long.class).tail().orElseThrow(ERR_NO_ID);
        }

        /**
         * @return a transaction for assigning the component represented by this segment
         */
        default Transaction toTransaction() {
            switch (resolvedComponent().type()) {
                case NONE:
                    return Transaction.start().assignComponent(resolvedIdentity(), resolvedComponent());
                case NUMBER:
                    return Transaction.start().assignComponent(resolvedIdentity(), resolvedComponent(),
                            Long.class.cast(resolvedValue()));
                default:
                    return Transaction.start().assignComponent(resolvedIdentity(), resolvedComponent(),
                            resolvedValue().toString());
            }
        }
    }

    /**
     * @param n a template file name (without .properties extension) on the
     *          classpath in the "templates" directory
     * @return a template generated from the file
     */
    public static EntityTemplate fromProperties(final String n) {
        return () -> PropertyCache.getProperties("templates/" + n).entrySet().stream()
                .map(e -> Optional.of(e.getKey()).map(Object::toString).map(s -> s.split("\\."))
                        .map(a -> Segment.of(a[0], a[1], e.getValue().toString())).orElseThrow(ERR_MISSING_PROPERTY));
    }

    Fallible ERR_MISSING_PROPERTY = Fallible.of("There was an error resolving a property.");
    Fallible ERR_NO_STACKS = Fallible.of("Entity templates require a Stacks instance argument.");
    Fallible ERR_NO_SEGMENTS_WITH_NAME = Fallible.of("Found no segments for this name.");

    /**
     * @return the segments defined by this template
     */
    Stream<Segment> segments();

    @Override
    default Transaction get(final Map<String, Object> args) {
        final Map<String, Object> augArgs = new HashMap<>();
        augArgs.putAll(args);
        augArgs.putAll(segments().filter(s -> s.component().equals(Component.Core.IDENTITY.name()))
                .collect(Collectors.toMap(s -> s.name(), s -> Long.parseLong(StringTemplate.of(s.value()).get(args)))));
        final Transaction creates = segments().collect(Collectors.groupingBy(Segment::name)).entrySet()
                .stream().map(e -> {
                    final Long id = e.getValue().stream().map(s -> s.resolve(augArgs))
                            .map(ResolvedSegment::resolvedIdentity).findFirst().orElseThrow(ERR_NO_SEGMENTS_WITH_NAME);
                    if (e.getValue().stream().anyMatch(Segment::isTransient)) {
                        return Transaction.start().createTransientEntity(id);
                    } else {
                        return Transaction.start().createEntity(id);
                    }
                }).reduce(Transaction::compose).orElseGet(Transaction::start);
        final Transaction others = segments().filter(s -> !s.isTransient()).map(e -> e.resolve(augArgs))
                .map(e -> e.toTransaction()).reduce(Transaction::compose).orElseGet(Transaction::start);
        return others.compose(creates);
    }

    /**
     * @param s a Stacks instance
     * @return a managed instance of this template, able to generate IDs and produce
     *         managed transactions.
     */
    default ManagedEntityTemplate manage(final Stacks s) {
        return ManagedEntityTemplate.of(this, s);
    }

}
