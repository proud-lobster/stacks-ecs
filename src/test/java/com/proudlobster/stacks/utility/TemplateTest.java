package com.proudlobster.stacks.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.TestContstants;

@Tag("Unit")
public class TemplateTest implements TestContstants {

    // BEGIN VALUES
    private static final Object SOME_VALUE = 1;
    private static final String SOME_ARG = "FOO";

    // BEGIN TEMPLATES
    private static final Template<Object> SOME_TEMPLATE = a -> Optional.ofNullable(a).map(m -> m.values())
            .filter(x -> x.size() > 0).map(x -> x.stream().findFirst().get()).orElse(SOME_VALUE);

    @Test
    @DisplayName("Template produces correct value")
    public void produce_correctValue() {
        assertEquals(SOME_VALUE, SOME_TEMPLATE.get());
    }

    @Test
    @DisplayName("Template produces correct value with arguments")
    public void produce_correctValueWithArgs() {
        assertEquals(SOME_ARG, SOME_TEMPLATE.get(SOME_ARG));
    }

    @Test
    @DisplayName("Template binds and produces correct value")
    public void bind_correctBindValue() {
        assertEquals(SOME_VALUE, SOME_TEMPLATE.bind().get());
    }

    @Test
    @DisplayName("Template binds to produce correct value with arguments")
    public void bind_correctBindValueWithArgs() {
        assertEquals(SOME_ARG, SOME_TEMPLATE.bind(SOME_ARG).get());
    }

}
