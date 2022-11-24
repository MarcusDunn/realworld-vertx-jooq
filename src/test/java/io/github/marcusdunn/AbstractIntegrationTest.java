package io.github.marcusdunn;

import dagger.Component;
import io.github.marcusdunn.users.login.UserModule;
import io.github.marcusdunn.users.login.LoginService;
import io.r2dbc.spi.ConnectionFactory;
import io.vertx.ext.web.client.WebClient;
import javax.inject.Singleton;
import org.testcontainers.containers.PostgreSQLContainer;

public class AbstractIntegrationTest {
    private static final TestApp testApp = DaggerAbstractIntegrationTest_TestApp.create();
    public static final ConnectionFactory connectionFactory = testApp.connectionFactory();
    public static final PostgreSQLContainer<?> postgres = testApp.postgresSqlContainer();
    public static final LoginService loginService = testApp.loginService();
    public static final Main main = testApp.main();
    public static final WebClient webClient = testApp.webClient();

    @Component(modules = {VertxModule.class, UserModule.class, TestDatabaseModule.class, ConfigModule.class})
    @Singleton
    interface TestApp {
        Main main();

        ConnectionFactory connectionFactory();

        PostgreSQLContainer<?> postgresSqlContainer();

        LoginService loginService();

        WebClient webClient();
    }
}
