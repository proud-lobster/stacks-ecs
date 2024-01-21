package com.proudlobster.stacks.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.sql.DataSource;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.structure.Configuration;
import com.proudlobster.stacks.structure.Dictionary;
import com.proudlobster.stacks.utility.ObjectAdapter;

// TODO datasource configuration documentation
/**
 * Provider for JDBC connections to a configured datasource.
 */
@FunctionalInterface
public interface JdbcConnectionProvider extends Supplier<Connection> {

    /**
     * @param cl dictionary of database configuration
     * @return a new connection provider
     */
    public static JdbcConnectionProvider of(final Dictionary<Configuration> cl) {
        final DataSource ds = Pattern.compile(",").splitAsStream(cl.require(CONF_DS_PROP_LIST).value())
                .filter(s -> !s.isBlank())
                .map(s -> CONF_DS_PREFIX + s).map(cl::require)
                .map(c -> (UnaryOperator<ObjectAdapter<DataSource>>) d -> d.setProperty(c))
                .reduce((f, n) -> d -> f.andThen(n).apply(d)).orElse(UnaryOperator.identity())
                .apply(ObjectAdapter.of(DataSource.class, cl.require(CONF_DS_CLASS_NAME).value())).get();
        assertStorage(ds);
        return () -> Fallible.attemptGet(ds::getConnection);
    }

    private static void assertStorage(final DataSource ds) {
        Optional.of(Fallible.attemptGet(ds::getConnection))
                .filter(d -> !Fallible.attemptGet(() -> d.getMetaData().getTables(null, null, TABLE_NAME, null).next()))
                .ifPresent((CREATE_TABLE).andThen(CLOSE_CONNECTION));
    }

    private static PreparedStatement prepareStatement(final Connection c, final String q, final Object... params) {
        final PreparedStatement ps = Fallible.attemptGet(() -> c.prepareStatement(q));
        IntStream.range(0, params.length).forEach(i -> Fallible.attemptRun(() -> ps.setObject(i + 1, params[i])));
        return ps;
    }

    String CONF_DS_PREFIX = "stacks.entity.storage.jdbc.datasource.";
    String CONF_DS_CLASS_NAME = CONF_DS_PREFIX + "class";
    String CONF_DS_PROP_LIST = CONF_DS_PREFIX + "properties";
    String TABLE_NAME = "ENTITY_COMPONENT";
    String TABLE_DEFINITION = """
            CREATE TABLE ENTITY_COMPONENT (
                ID BIGINT NOT NULL,
                COMPONENT VARCHAR(50) NOT NULL,
                STR_VALUE CLOB,
                LONG_VALUE BIGINT,
                ACTIVE BOOLEAN,
                PRIMARY KEY (ID, COMPONENT)
            )
            """;
    Consumer<Connection> CLOSE_CONNECTION = c -> Fallible.attemptAccept(Connection::close, c);
    Consumer<Connection> CREATE_TABLE = c -> Fallible.attemptGet(() -> c.prepareStatement(TABLE_DEFINITION).execute());

    /**
     * @param <E> type of entity to be returned
     * @param q   the SQL query to execute
     * @param m   the mapping function for the result set
     * @param ps  the parameters for the query
     * @return the mapped results of the query
     */
    default <E> Stream<E> executeQuery(final String q, final Fallible.RiskyFunction<ResultSet, E> m,
            final Object... ps) {
        final List<E> es = new ArrayList<>();
        runInConnection(c -> {
            final ResultSet rs = prepareStatement(c, q, ps).executeQuery();
            while (rs.next()) {
                es.add(m.apply(rs));
            }
        });
        return es.stream();
    }

    /**
     * @param q      the SQL statement to execute
     * @param params the parameters for the statement
     */
    default void executeStatement(final String q, final Object... params) {
        runInConnection(c -> prepareStatement(c, q, params).execute());
    }

    /**
     * @param c a connection consumer
     */
    default void runInConnection(final Fallible.RiskyConsumer<Connection> c) {
        c.andThen(CLOSE_CONNECTION).accept(get());
    }
}
