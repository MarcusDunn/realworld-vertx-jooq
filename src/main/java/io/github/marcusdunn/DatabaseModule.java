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
import java.text.MessageFormat;
import java.util.Properties;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Module
public class DatabaseModule {
    private static final String DATABASE_KEY = "DATABASE_DATABASE";
    private static final String PORT_KEY = "DATABASE_PORT";
    private static final String HOST_KEY = "DATABASE_HOST";
    private static final String DRIVER_KEY = "DATABASE_DRIVER";
    private static final String USER_KEY = "DATABASE_USER";
    private static final String PASSWORD_KEY = "DATABASE_PASSWORD";
    private static final String CHANGELOG_KEY = "DATABASE_CHANGELOG";
    private static final String DEFAULT_DATABASE = "realworld";
    private static final int DEFAULT_PORT = 5432;
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_DRIVER = "postgresql";
    private static final String DEFAULT_USER = "realworld";
    private static final String DEFAULT_PASSWORD = "realworld";
    private static final String DEFAULT_CHANGELOG = "dbchangelog.xml";

    @Provides
    @Singleton
    static ConnectionFactory connectionFactory(Configuration configuration) {

        return ConnectionFactories.get(ConnectionFactoryOptions
                .builder()
                .option(DATABASE, configuration.getString(DATABASE_KEY, DEFAULT_DATABASE))
                .option(PORT, configuration.getInt(PORT_KEY, DEFAULT_PORT))
                .option(HOST, configuration.getString(HOST_KEY, DEFAULT_HOST))
                .option(DRIVER, configuration.getString(DRIVER_KEY, DEFAULT_DRIVER))
                .option(USER, configuration.getString(USER_KEY, DEFAULT_USER))
                .option(PASSWORD, configuration.getString(PASSWORD_KEY, DEFAULT_PASSWORD))
                .build()
        );
    }

    @Provides
    @Singleton
    Liquibase liquibase(Configuration configuration) {
        final String url = MessageFormat.format(
                "jdbc:{0}://{1}:{2}/{3}",
                configuration.getString(DRIVER_KEY, DEFAULT_DRIVER),
                configuration.getString(HOST_KEY, DEFAULT_HOST),
                Integer.toString(configuration.getInt(PORT_KEY, DEFAULT_PORT)),
                configuration.getString(DATABASE_KEY, DEFAULT_DATABASE)
        );
        final var properties = new Properties();
        properties.put("user", configuration.getString(USER_KEY, DEFAULT_USER));
        properties.put("password", configuration.getString(PASSWORD_KEY, DEFAULT_PASSWORD));
        try {
            return new Liquibase(
                    configuration.getString(CHANGELOG_KEY, DEFAULT_CHANGELOG),
                    new ClassLoaderResourceAccessor(),
                    new JdbcConnection(new Driver().connect(url, properties))
            );
        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException("failed to connect to: " + url, e);
        }
    }
}
