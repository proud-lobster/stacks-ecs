package com.proudlobster.stacks.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.proudlobster.stacks.Fallible;

public interface PropertyCache {

    public static Properties getProperties(final String name) {
        return PROPERTY_CACHE.computeIfAbsent(name, n -> Fallible.attemptApply(a -> {
            final Properties p = new Properties();
            p.load(PropertyCache.class.getClassLoader().getResourceAsStream(a + ".properties"));
            return p;
        }, n));
    }

    Map<String, Properties> PROPERTY_CACHE = new HashMap<>();
}
