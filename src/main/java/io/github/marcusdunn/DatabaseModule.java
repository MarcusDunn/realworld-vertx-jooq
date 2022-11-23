package io.github.marcusdunn;

import dagger.Module;
import dagger.Provides;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

@Module
public class DatabaseModule {
    @Provides
    ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions
                .parse("r2dbc:postgresql://localhost:5432/realworld")
                .mutate()
                .option(ConnectionFactoryOptions.USER, "realworld")
                .option(ConnectionFactoryOptions.PASSWORD, "realworld")
                .build()
        );
    }
}
