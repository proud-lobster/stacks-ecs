package com.proudlobster.stacks.utility;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Used for converting strings to other forms.
 */
@FunctionalInterface
public interface StringAdapter {

    /**
     * Splitter used to retrieve multiref values from a string.
     */
    Pattern SPLITTER = Pattern.compile("\\|");

    /**
     * Pattern for identifying numeric strings.
     */
    Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * Identifies a blank (no non-whitespace characters) string
     */
    Predicate<String> BLANK = String::isBlank;

    /**
     * Identifies a non-blank (contains at least one non-whitespace character)
     * string
     */
    Predicate<String> NOT_BLANK = BLANK.negate();

    /**
     * @param s string to wrap in adapter
     * @return adapter for the string
     */
    static StringAdapter of(final String s) {
        return () -> s;
    }

    /**
     * @return the wrapped string
     */
    String string();

    /**
     * @return the adapter split in to multiple adapters using the splitter
     */
    default Stream<StringAdapter> split() {
        return Arrays.stream(SPLITTER.split(string())).filter(NOT_BLANK).map(StringAdapter::of);
    }

    /**
     * @return the adapter as a Long if possible
     */
    default Optional<Long> number() {
        return Optional.of(string()).filter(s -> NUMBER.matcher(s).matches()).map(Long::parseLong);
    }

    /**
     * @return the adapter split according to split() and with the contents
     *         converted according to number(). Non-numeric strings are ignored
     */
    default Stream<Long> splitToNumbers() {
        return split().map(StringAdapter::number).filter(Optional::isPresent).map(Optional::get);
    }
}
