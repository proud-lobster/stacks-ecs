package com.proudlobster.stacks.utility;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import com.proudlobster.stacks.structure.Tuple;

/**
 * Template implementation for strings.
 * 
 * Replaces named arguments such as {foo} or ordered arguments such as {1}.
 */
@FunctionalInterface
public interface StringTemplate extends Template<String> {

    Pattern PARAM_PATTERN = Pattern.compile("\\{([^\\}]*)}");

    private static String replacer(final Map<String, Object> m, final String s) {
        final AtomicInteger i = new AtomicInteger(0);
        final StringBuilder sb = new StringBuilder();
        PARAM_PATTERN.matcher(s).results().forEach(r -> {
            sb.append(s.substring(i.get(), r.start())).append(Optional.ofNullable(m.get(r.group(1))).orElse(r.group()));
            i.set(r.end());
        });
        return sb.append(s.subSequence(i.get(), s.length())).toString();
    }

    /**
     * @param s a string template
     * @return a template of that string
     */
    public static StringTemplate of(final String s) {
        return m -> replacer(m, s);
    }

    /**
     * @param t the arguments to fill in to the template
     * @return a template bound to those arguments for later resolution
     */
    default StringTemplate bind(final Tuple<? extends Object> t) {
        return as -> Optional.of(as).map(m -> m.values()).filter(x -> x.size() == 0).map(x -> this.get(t))
                .orElseThrow(ERR_ALREADY_BOUND);
    }

    /**
     * @param t the arguments to fill in to the template
     * @return a template bound to those arguments for later resolution
     */
    default StringTemplate bind(final Object... rs) {
        return as -> Optional.of(as).map(m -> m.values()).filter(x -> x.size() == 0).map(x -> this.get(rs))
                .orElseThrow(ERR_ALREADY_BOUND);
    }
}
