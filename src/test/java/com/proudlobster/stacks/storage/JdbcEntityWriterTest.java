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

import com.proudlobster.stacks.TestContstants;
import com.proudlobster.stacks.ecp.Component;
import com.proudlobster.stacks.structure.Configuration;
import com.proudlobster.stacks.structure.Dictionary;

@Tag("Unit")
public class JdbcEntityWriterTest implements TestContstants {

        // BEGIN TEST DATA
        private static final EntityWriter.Record SOME_REC_1 = EntityWriter.Record.of(ID_1,
                        Component.Core.IDENTITY.name(), Optional.empty(), Optional.empty(), Boolean.TRUE);
        private static final EntityWriter.Record SOME_REC_2 = EntityWriter.Record.of(ID_1, COMPONENT_NAME_1,
                        Optional.of(STRING_VALUE_1), Optional.empty(), Boolean.TRUE);
        private static final EntityWriter.Record SOME_REC_3 = EntityWriter.Record.of(ID_1, COMPONENT_NAME_1,
                        Optional.of(STRING_VALUE_2), Optional.empty(), Boolean.TRUE);
        private static final EntityWriter.Record DEL_REC = EntityWriter.Record.of(ID_1, Component.Core.IDENTITY.name(),
                        Optional.empty(), Optional.empty(), Boolean.FALSE);

        // BEGIN CONFIGURATION
        private static final String TEST_QUERY = "SELECT * FROM ENTITY_COMPONENT WHERE ACTIVE = true";
        private static final String ID_COL_NAME = "ID";
        private static final String STR_VAL_COL_NAME = "STR_VALUE";
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
        private static final JdbcEntityWriter WRITER = JdbcEntityWriter.of(() -> PROVIDER.get().get());

        @BeforeEach
        public void deleteDatabase() throws IOException {
                Files.walk(Paths.get(DB_NAME.value())).sorted(Comparator.reverseOrder()).map(Path::toFile)
                                .forEach(File::delete);
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
        @DisplayName("Write record works")
        public void write_record() {
                WRITER.write(SOME_REC_1);
                assertEquals(ID_1, PROVIDER.get().executeQuery(TEST_QUERY, rs -> rs.getLong(ID_COL_NAME)).findFirst()
                                .get());
        }

        @Test
        @DisplayName("Upsert record works")
        public void upsert_record() {
                WRITER.write(SOME_REC_2);
                WRITER.write(SOME_REC_3);
                assertEquals(SOME_REC_3.value(),
                                PROVIDER.get().executeQuery(TEST_QUERY, rs -> rs.getString(STR_VAL_COL_NAME))
                                                .findFirst().get());
        }

        @Test
        @DisplayName("Delete record works")
        public void delete_record() {
                WRITER.write(SOME_REC_1);
                WRITER.write(DEL_REC);
                assertTrue(PROVIDER.get().executeQuery(TEST_QUERY, rs -> rs.getLong(ID_COL_NAME)).findAny().isEmpty());
        }
}
