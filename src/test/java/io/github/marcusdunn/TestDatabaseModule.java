package io.github.marcusdunn;

import dagger.Module;
import dagger.Provides;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.SQLException;

@Module
public class TestDatabaseModule {
    public static final ConnectionFactory connectionFactory;
    private static final PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse(PostgreSQLContainer.IMAGE));
        postgres.start();
        connectionFactory = ConnectionFactories.get(PostgreSQLR2DBCDatabaseContainer.getOptions(postgres));
    }

    @Provides
    static ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Provides
    static PostgreSQLContainer<?> postgreSQLContainer() {
        return postgres;
    }

    @Provides
    static Liquibase liquibase() {
        try {
            final var connection = postgres.createConnection("?");
            final var jdbcConnection = new JdbcConnection(connection);
            return new Liquibase("dbchangelog.xml", new ClassLoaderResourceAccessor(), jdbcConnection);
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }
}
