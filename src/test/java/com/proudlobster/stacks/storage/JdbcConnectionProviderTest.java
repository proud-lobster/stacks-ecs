package com.proudlobster.stacks.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.proudlobster.stacks.Fallible;
import com.proudlobster.stacks.TestContstants;
import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.structure.Configuration;
import com.proudlobster.stacks.structure.Dictionary;

@Tag("Unit")
public class JdbcConnectionProviderTest implements TestContstants {

    // BEGIN CONFIGURATION
    private static final String TEST_QUERY = "SELECT * FROM ENTITY_COMPONENT";
    private static final String TEST_INSERT = "INSERT INTO ENTITY_COMPONENT (ID, COMPONENT, STR_VALUE, LONG_VALUE, ACTIVE) VALUES (?,?,?,?,?)";
    private static final String COLUMN_NAME = "ID";
    private static final int VALID_TIMEOUT = 1;
    private static final Configuration PROP_LIST = Configuration.of(JdbcConnectionProvider.CONF_DS_PROP_LIST,
            "databaseName,createDatabase");
    private static final Configuration DB_CLASS = Configuration.of(JdbcConnectionProvider.CONF_DS_CLASS_NAME,
            "org.apache.derby.jdbc.EmbeddedDataSource");
    private static final Configuration DB_NAME = Configuration
            .of(JdbcConnectionProvider.CONF_DS_PREFIX + "databaseName", "STACKS_DB");
    private static final Configuration CREATE_DB = Configuration
            .of(JdbcConnectionProvider.CONF_DS_PREFIX + "createDatabase", "create");
    private static final Map<String, Configuration> CONF = Stream.of(PROP_LIST, DB_CLASS, DB_NAME, CREATE_DB)
            .collect(Collectors.toMap(Configuration::name, Function.identity()));
    private static final Dictionary<Configuration> DICT = Dictionary.of(CONF);

    // BEGIN PROVIDERS
    private static JdbcConnectionProvider PROVIDER;

    @BeforeEach
    public void deleteDatabase() throws IOException {
        Optional.of(Paths.get(DB_NAME.value())).filter(Files::exists).map(p -> Fallible.attemptApply(Files::walk, p))
                .orElseGet(Stream::empty).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        PROVIDER = JdbcConnectionProvider.of(DICT);
    }

    @AfterEach
    public void shutDownDatabase() throws SQLException {
        try {
            DriverManager.getConnection("jdbc:derby:" + DB_NAME.value() + ";shutdown=true");
        } catch (SQLNonTransientConnectionException e) {
            if (!e.getSQLState().equals("08006")) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    @DisplayName("Connection is valid")
    public void get_isValid() throws SQLException {
        assertTrue(PROVIDER.get().isValid(VALID_TIMEOUT));
    }

    @Test
    @DisplayName("Empty database returns empty stream")
    public void executeQuery_emptyStream() {
        assertTrue(PROVIDER.executeQuery(TEST_QUERY, rs -> rs.getLong(COLUMN_NAME)).findAny().isEmpty());
    }

    @Test
    @DisplayName("Statement inserts data")
    public void executeStatement_dataStream() {
        PROVIDER.executeStatement(TEST_INSERT, ID_1, Component.Core.IDENTITY.name(), null, ID_1, Boolean.TRUE);
        assertEquals(ID_1, PROVIDER.executeQuery(TEST_QUERY, rs -> rs.getLong(COLUMN_NAME)).findFirst().get());
    }

    @Test
    @DisplayName("Connection consumer works")
    public void runInConnection_works() {
        final AtomicBoolean b = new AtomicBoolean(false);
        PROVIDER.runInConnection(c -> b.set(c.isValid(VALID_TIMEOUT)));
        assertTrue(b.get());
    }

}
