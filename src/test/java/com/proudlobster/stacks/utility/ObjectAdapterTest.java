package com.proudlobster.stacks.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;
import com.proudlobster.stacks.structure.Configuration;

@Tag("Unit")
public class ObjectAdapterTest implements TestContstants {

    public static class Foo {
        private String bar;

        public String getBar() {
            return bar;
        }

        public void setBar(final String bar) {
            this.bar = bar;
        }
    }

    // BEGIN VALUES
    private static final String SOME_PROP = "bar";
    private static final String SOME_VAL = "FOO";
    private static final String INVALID_PROP = "foo";
    private static final Long INVALID_VAL = 1L;
    private static final Configuration SOME_CONF = Configuration.of(SOME_PROP, SOME_VAL);

    // BEGIN ADAPTERS
    private ObjectAdapter<Foo> SOME_ADAPTER;
    private ObjectAdapter<Object> OBJ_ADAPTER;

    @BeforeEach
    public void resetAdapters() {
        SOME_ADAPTER = ObjectAdapter.of(Foo.class);
        OBJ_ADAPTER = ObjectAdapter.of(Object.class, Foo.class.getName());
    }

    @Test
    @DisplayName("Setting a property changes the value")
    public void setProperty_works() {
        assertEquals(SOME_VAL, SOME_ADAPTER.setProperty(SOME_PROP, SOME_VAL).get().getBar());
    }

    @Test
    @DisplayName("Setting a property on a subtype changes the value")
    public void setProperty_subtype() {
        final Foo f = (Foo) OBJ_ADAPTER.setProperty(SOME_PROP, SOME_VAL).get();
        assertEquals(SOME_VAL, f.getBar());
    }

    @Test
    @DisplayName("Setting a property with a configuration works")
    public void setProperty_configuration() {
        assertEquals(SOME_VAL, SOME_ADAPTER.setProperty(SOME_CONF).get().getBar());
    }

    @Test
    @DisplayName("Setting an invalid property fails")
    public void setProperty_invalidPropertyFails() {
        assertThrows(Fallible.StacksException.class, () -> SOME_ADAPTER.setProperty(INVALID_PROP, SOME_VAL).get());
    }

    @Test
    @DisplayName("Setting a property to an invalid value fails")
    public void setProperty_invalidValueFails() {
        assertThrows(Fallible.StacksException.class, () -> SOME_ADAPTER.setProperty(SOME_PROP, INVALID_VAL).get());
    }
}
