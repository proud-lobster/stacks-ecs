package com.proudlobster.stacks.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class ConfigurationTest implements TestContstants {

    // BEGIN VALUES
    private static final String SOME_NAME_TAIL = "name";
    private static final String SOME_NAME = "some." + SOME_NAME_TAIL;
    private static final String SOME_VALUE = "FOO";
    private static final String SOME_DEFAULT = "BAR";
    private static final String SOME_OVERLAY = "FOOBAR";

    // BEGIN CONFIGURATIONS
    private static final Configuration EMPTY_CONFIG = () -> (Triple<String>) i -> Optional.empty();
    private static final Configuration DEFAULT_CONFIG = Configuration.of(SOME_NAME, SOME_DEFAULT, null);
    private static final Configuration FULL_CONFIG = Configuration.of(SOME_NAME, SOME_DEFAULT, SOME_VALUE);
    private static final Supplier<Stream<Configuration>> DEFAULTS = () -> Configuration.ofProperties("default");

    @Test
    @DisplayName("Configuration has default value")
    public void defaultValue_get() {
        assertEquals(SOME_DEFAULT, FULL_CONFIG.defaultValue());
    }

    @Test
    @DisplayName("Empty configuration has no default value")
    public void defaultValue_fail() {
        assertThrows(Fallible.StacksException.class, () -> EMPTY_CONFIG.defaultValue());
    }

    @Test
    @DisplayName("Configuration has actual value")
    public void value_get() {
        assertEquals(SOME_VALUE, FULL_CONFIG.value());
    }

    @Test
    @DisplayName("Configuration with no actual value gets default value")
    public void value_getDefault() {
        assertEquals(SOME_DEFAULT, DEFAULT_CONFIG.value());
    }

    @Test
    @DisplayName("Configuration has name")
    public void name_get() {
        assertEquals(SOME_NAME, FULL_CONFIG.name());
    }

    @Test
    @DisplayName("Empty configuration has no name")
    public void name_fail() {
        assertThrows(Fallible.StacksException.class, () -> EMPTY_CONFIG.name());
    }

    @Test
    @DisplayName("Name tail gets name tail")
    public void nameTail_get() {
        assertEquals(SOME_NAME_TAIL, FULL_CONFIG.nameTail());
    }

    @Test
    @DisplayName("Can load defaults")
    public void ofDefaults_works() {
        assertTrue(DEFAULTS.get().findAny().isPresent());
    }

    @Test
    @DisplayName("Overlay replaces value")
    public void overlay_replaces() {
        assertEquals(SOME_OVERLAY, FULL_CONFIG.overlay(SOME_OVERLAY).value());
    }
}
