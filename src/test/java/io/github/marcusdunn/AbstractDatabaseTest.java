package io.github.marcusdunn;

import java.sql.SQLException;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractDatabaseTest extends AbstractIntegrationTest {
    public final DSLContext dsl = DSL.using(connectionFactory);

    private void reset() {
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

