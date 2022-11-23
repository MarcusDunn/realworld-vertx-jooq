package io.github.marcusdunn;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import java.sql.SQLException;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractDatabaseTest {
    public static final ConnectionFactory connectionFactory;
    private static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse(PostgreSQLContainer.IMAGE));
        postgres.start();
        connectionFactory = ConnectionFactories.get(PostgreSQLR2DBCDatabaseContainer.getOptions(postgres));
    }

    private static void reset() {
        try (final var connection = postgres.createConnection("?")) {
            try (final var jdbcConnection = new JdbcConnection(connection)) {
                try (final var liquibase = new Liquibase("dbchangelog.xml", new ClassLoaderResourceAccessor(), jdbcConnection)) {
                    liquibase.dropAll();
                    liquibase.update();
                } catch (LiquibaseException e) {
                    throw new RuntimeException(e);
                }
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        reset();
    }
}

