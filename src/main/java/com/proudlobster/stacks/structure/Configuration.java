package com.proudlobster.stacks.structure;

import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.utility.PropertyCache;

/**
 * Structure for configuration values, with a name, default value, and actual
 * value.
 */
@FunctionalInterface
public interface Configuration {

    /**
     * @param name  name of the configuration
     * @param value actual value of the configuration
     * @return configuration of the above with an empty default value
     */
    public static Configuration of(final String name, final String value) {
        return of(name, "", value);
    }

    /**
     * @param name  name of the configuration
     * @param def   default value of the configuration
     * @param value actual value of the configuration
     * @return configuration of the above
     */
    public static Configuration of(final String name, final String def, final String value) {
        return () -> Triple.of(name, def, value);
    }

    /**
     * @param p properties to load the configurations from
     * @return configurations loaded from those properties, with the property value
     *         as the default value
     */
    public static Stream<Configuration> of(final Properties p) {
        return p.entrySet().stream().map(e -> Configuration.of(e.getKey().toString(), e.getValue().toString(), null));
    }

    /**
     * @param n the name of a property file (without the extension) on the classpath
     *          to load configurations from
     * @return configurations loaded from those properties, with the property value
     *         as the default value
     */
    public static Stream<Configuration> ofProperties(final String n) {
        return of(PropertyCache.getProperties(n));
    }

    /**
     * @param d the name of a property file (without the extension) on the classpath
     *          to load the default values from
     * @param n the name of a property file (without the extension) on the classpath
     *          to load the actual values from
     * @return configurations loaded from those properties
     */
    public static Stream<Configuration> ofProperties(final String d, final String n) {
        return ofProperties(d).map(e -> e.overlay(PropertyCache.getProperties(n).getProperty(e.name())));
    }

    Fallible ERR_NO_NAME = Fallible.of("No configuration name defined.");

    int VALUE_POSITION = 2;

    /**
     * @return the tuple containing the internal values
     */
    Triple<String> delegate();

    /**
     * @return the default value of the configuration
     */
    default String defaultValue() {
        return delegate().second();
    }

    /**
     * @return the actual value of the configuration, or the default if none present
     */
    default String value() {
        return delegate().get(VALUE_POSITION).orElse(defaultValue());
    }

    /**
     * @return the name of the configuration
     */
    default String name() {
        return delegate().first();
    }

    /**
     * @return the last naming element of a name with dot notation
     */
    default String nameTail() {
        return Pattern.compile("\\.").splitAsStream(name()).reduce((a, b) -> b).orElseThrow(ERR_NO_NAME);
    }

    /**
     * @param value the value to overlay on this configuration
     * @return a configuration with the default value of the original and the new
     *         actual value
     */
    default Configuration overlay(final String value) {
        return Configuration.of(name(), defaultValue(), value);
    }

}
