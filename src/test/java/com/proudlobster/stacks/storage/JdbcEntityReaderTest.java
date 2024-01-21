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
import java.util.concurrent.atomic.AtomicReference;
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
import com.proudlobster.stacks.ecp.Entity;
import com.proudlobster.stacks.structure.Configuration;
import com.proudlobster.stacks.structure.Dictionary;

@Tag("Unit")
public class JdbcEntityReaderTest implements TestContstants {

    // BEGIN CONFIGURATION
    private static final String TEST_INSERT = "INSERT INTO ENTITY_COMPONENT (ID, COMPONENT, STR_VALUE, LONG_VALUE, ACTIVE) VALUES (?,?,?,?,?)";
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
    private static final AtomicReference<JdbcConnectionProvider> PROVIDER = new AtomicReference<>();

    // BEGIN WRITERS
    private static final JdbcEntityReader READER = JdbcEntityReader.of(() -> PROVIDER.get().get());

    @BeforeEach
    public void deleteDatabase() throws IOException {
        Optional.of(Paths.get(DB_NAME.value())).filter(Files::exists).map(p -> Fallible.attemptApply(Files::walk, p))
                .orElseGet(Stream::empty).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        PROVIDER.set(JdbcConnectionProvider.of(DICT));
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
    @DisplayName("Empty database reads no records by ID")
    public void read_noRecordsById() {
        assertTrue(READER.read(EntityReader.Record.of(ID_1)).findAny().isEmpty());
    }

    @Test
    @DisplayName("Empty database reads no records by component")
    public void read_noRecordsByComponent() {
        assertTrue(READER.read(EntityReader.Record.of(Component.Core.IDENTITY.name())).findAny().isEmpty());
    }

    @Test
    @DisplayName("Empty database does not have ID")
    public void read_idEmpty() {
        assertTrue(READER.read(ID_1).isEmpty());
    }

    @Test
    @DisplayName("Database with ID reads ID")
    public void read_idPresent() {
        PROVIDER.get().executeStatement(TEST_INSERT, ID_1, Component.Core.IDENTITY.name(), null, ID_1, Boolean.TRUE);
        assertEquals(ID_1, READER.read(ID_1).map(Entity::identifier).get());
    }

    @Test
    @DisplayName("Database with component reads component")
    public void readIdsForComponent_componentPresent() {
        PROVIDER.get().executeStatement(TEST_INSERT, ID_1, Component.Core.IDENTITY.name(), null, ID_1, Boolean.TRUE);
        assertEquals(ID_1, READER.readIdsForComponent(Component.Core.IDENTITY.name()).findAny().get());
    }
}
