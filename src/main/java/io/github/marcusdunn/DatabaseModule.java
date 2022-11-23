package io.github.marcusdunn;

import dagger.Module;
import dagger.Provides;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import javax.inject.Singleton;
import org.apache.commons.configuration2.Configuration;

import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

@Module
public class DatabaseModule {
    @Provides
    @Singleton
    static ConnectionFactory connectionFactory(Configuration configuration) {
        return ConnectionFactories.get(ConnectionFactoryOptions
                .parse(configuration.getString("R2DBC_URL", "r2dbc:postgresql://localhost:5432/realworld"))
                .mutate()
                .option(USER, configuration.getString("DATABASE_USER", "realworld"))
                .option(PASSWORD, configuration.getString("DATABASE_PASSWORD", "realworld"))
                .build()
        );
    }
}
