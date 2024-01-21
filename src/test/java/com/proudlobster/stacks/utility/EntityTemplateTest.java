package com.proudlobster.stacks.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.TestContstants;
import com.proudlobster.stacks.ecp.Component;

@Tag("Unit")
public class EntityTemplateTest implements TestContstants {

    private static final String SOME_NAME = "foo";
    private static final String SOME_OTHER_NAME = "foo2";
    private static final String SOME_REF = "bar";
    private static final Map<String, Object> ID_ARG = Map.of(SOME_NAME, ID_1);
    private static final Map<String, Object> STR_ARG = Map.of(SOME_NAME, ID_1, SOME_REF, STRING_VALUE_1);
    private static final Map<String, Object> LONG_ARG = Map.of(SOME_NAME, ID_1, SOME_REF, LONG_VALUE_1);
    private static final Map<String, Object> REF_ARG = Map.of(SOME_NAME, ID_1, SOME_OTHER_NAME, ID_2);

    private static final EntityTemplate TRANSIENT_TEMPL = EntityTemplate.fromProperties("test-transient");
    private static final EntityTemplate NO_ID_TEMPL = EntityTemplate.fromProperties("test-no-id");
    private static final EntityTemplate ID_TEMPL = EntityTemplate.fromProperties("test-param-identity");
    private static final EntityTemplate STRING_TEMPL = EntityTemplate.fromProperties("test-string");
    private static final EntityTemplate LONG_TEMPL = EntityTemplate.fromProperties("test-long");
    private static final EntityTemplate REF_TEMPL = EntityTemplate.fromProperties("test-ref");

    @BeforeEach
    public void clearMemory() {
        MAP_WRITER_RECORDS.clear();
    }

    @Test
    @DisplayName("Template creates transient entity with identity")
    public void create_identityTransientEntity() {
        TRANSIENT_TEMPL.get(ID_ARG).commit(MAP_WRITER);
        assertTrue(MAP_WRITER_RECORDS.containsKey(ID_1));
        assertTrue(MAP_WRITER_RECORDS.get(ID_1).containsKey(Component.Core.TRANSIENT.name()));
    }

    @Test
    @DisplayName("Template with no identity gets named identity")
    public void create_noIdentityFromArgs() {
        NO_ID_TEMPL.get(ID_ARG).commit(MAP_WRITER);
        final Map<String, Object> m = MAP_WRITER_RECORDS.get(ID_1);
        assertNotNull(m);
        assertEquals(ID_1, m.get(Component.Core.IDENTITY.name()));
    }

    @Test
    @DisplayName("Template with identity gets that identity")
    public void create_identityFromArgs() {
        ID_TEMPL.get(ID_1).commit(MAP_WRITER);
        final Map<String, Object> m = MAP_WRITER_RECORDS.get(ID_1);
        assertNotNull(m);
        assertEquals(ID_1, m.get(Component.Core.IDENTITY.name()));
    }

    @Test
    @DisplayName("Template replaces a string value")
    public void replace_string() {
        STRING_TEMPL.get(STR_ARG).commit(MAP_WRITER);
        final Map<String, Object> m = MAP_WRITER_RECORDS.get(ID_1);
        assertNotNull(m);
        assertEquals(STRING_VALUE_1, m.get(COMPONENT_NAME_1));
    }

    @Test
    @DisplayName("Template replaces a long value")
    public void replace_long() {
        LONG_TEMPL.get(LONG_ARG).commit(MAP_WRITER);
        final Map<String, Object> m = MAP_WRITER_RECORDS.get(ID_1);
        assertNotNull(m);
        assertEquals(LONG_VALUE_1, m.get(COMPONENT_NAME_1));
    }

    @Test
    @DisplayName("Template replaced reference to other ID")
    public void replace_ref() {
        REF_TEMPL.get(REF_ARG).commit(MAP_WRITER);
        final Map<String, Object> m = MAP_WRITER_RECORDS.get(ID_2);
        assertNotNull(m);
        assertEquals(ID_1, m.get(COMPONENT_NAME_1));
    }
}
