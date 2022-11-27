package io.github.marcusdunn;

import dagger.Module;
import dagger.Provides;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.commons.configuration2.Configuration;
import org.postgresql.Driver;

import javax.inject.Singleton;
import java.sql.SQLException;
import java.util.Properties;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Module
public class DatabaseModule {

    @Provides
    @Singleton
    static ConnectionFactory connectionFactory(Configuration configuration) {
        return ConnectionFactories.get(ConnectionFactoryOptions
                .builder()
                .option(DATABASE, configuration.getString("DATABASE_DATABASE", "realworld"))
                .option(PORT, configuration.getInt("DATABASE_PORT", 5432))
                .option(HOST, configuration.getString("DATABASE_HOST", "localhost"))
                .option(DRIVER, configuration.getString("DATABASE_DRIVER", "postgresql"))
                .option(USER, configuration.getString("DATABASE_USER", "realworld"))
                .option(PASSWORD, configuration.getString("DATABASE_PASSWORD", "realworld"))
                .build()
        );
    }

    @Provides
    @Singleton
    Liquibase liquibase(Configuration configuration) {
        final String url = "jdbc:" +
                configuration.getString("DATABASE_DRIVER", "postgresql") +
                "://" +
                configuration.getString("DATABASE_HOST", "localhost") +
                ":" +
                configuration.getInt("DATABASE_PORT", 5432) +
                "/" +
                configuration.getString("DATABASE_DATABASE", "realworld");
        final var properties = new Properties();
        properties.put("user", configuration.getString("DATABASE_USER", "realworld"));
        properties.put("password", configuration.getString("DATABASE_PASSWORD", "realworld"));
        try {
            return new Liquibase(configuration.getString("DATABASE_CHANGELOG", "dbchangelog.xml"), new ClassLoaderResourceAccessor(), new JdbcConnection(new Driver().connect(url, properties)));
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }
}
