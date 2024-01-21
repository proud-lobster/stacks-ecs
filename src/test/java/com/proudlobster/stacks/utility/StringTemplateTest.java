package com.proudlobster.stacks.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class StringTemplateTest implements TestContstants {

    // BEGIN VALUES
    private static final String SOME_STRING = "Hello, {0}!";
    private static final String SOME_NAMED_STRING = "Hello, {name1}!  This is {name2}.";
    private static final String SOME_ARG = "World";
    private static final String SOME_NAMED_ARG = "Foo";
    private static final String SOME_NAME_1 = "name1";
    private static final String SOME_NAME_2 = "name2";
    private static final Map<String, Object> SOME_NAMED_ARGS = Map.of(SOME_NAME_1, SOME_ARG, SOME_NAME_2,
            SOME_NAMED_ARG);

    // BEGIN TEMPLATES
    private static final Template<String> STRING_TEMPLATE = StringTemplate.of(SOME_STRING);
    private static final Template<String> STRING_NAMED_TEMPLATE = StringTemplate.of(SOME_NAMED_STRING);

    // BEGIN OUTPUTS
    private static final String SOME_STRING_OUT = "Hello, World!";
    private static final String SOME_NAMED_STRING_OUT = "Hello, World!  This is Foo.";

    @Test
    @DisplayName("String template produces correct value")
    public void get_correctStringValue() {
        assertEquals(SOME_STRING, STRING_TEMPLATE.get());
    }

    @Test
    @DisplayName("String template produces correct value with arguments")
    public void get_correctStringValueWithArgs() {
        assertEquals(SOME_STRING_OUT, STRING_TEMPLATE.get(SOME_ARG));
    }

    @Test
    @DisplayName("String template binds and produces correct value")
    public void bind_correctStringValue() {
        assertEquals(SOME_STRING, STRING_TEMPLATE.bind().get());
    }

    @Test
    @DisplayName("String template binds to produce correct value with arguments")
    public void bind_correctStringValueWithArgs() {
        assertEquals(SOME_STRING_OUT, STRING_TEMPLATE.bind(SOME_ARG).get());
    }

    @Test
    @DisplayName("String template produces correct value with named arguments")
    public void get_correctStringValueWithNamedArgs() {
        assertEquals(SOME_NAMED_STRING_OUT, STRING_NAMED_TEMPLATE.get(SOME_NAMED_ARGS));
    }
}
