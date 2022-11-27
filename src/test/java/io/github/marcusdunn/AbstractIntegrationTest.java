package io.github.marcusdunn;

import dagger.Component;
import io.github.marcusdunn.users.UsersModule;
import io.github.marcusdunn.users.login.LoginService;
import io.r2dbc.spi.ConnectionFactory;
import io.vertx.ext.web.client.WebClient;
import liquibase.Liquibase;

import javax.inject.Singleton;

public class AbstractIntegrationTest {
    private static final TestApp testApp = DaggerAbstractIntegrationTest_TestApp.create();
    public static final ConnectionFactory connectionFactory = testApp.connectionFactory();
    public static final LoginService loginService = testApp.loginService();
    public static final Main main = testApp.main();
    public static final WebClient webClient = testApp.webClient();
    public static final Liquibase liquibase = testApp.liquibase();

    @Component(modules = {VertxModule.class, UsersModule.class, TestDatabaseModule.class, ConfigModule.class})
    @Singleton
    interface TestApp {
        Main main();

        ConnectionFactory connectionFactory();
        LoginService loginService();

        WebClient webClient();

        Liquibase liquibase();
    }
}
