package io.github.marcusdunn;

import dagger.Module;
import dagger.Provides;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import javax.inject.Singleton;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

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
    @Singleton
    static ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Provides
    @Singleton
    static PostgreSQLContainer<?> postgreSQLContainer() {
        return postgres;
    }
}
