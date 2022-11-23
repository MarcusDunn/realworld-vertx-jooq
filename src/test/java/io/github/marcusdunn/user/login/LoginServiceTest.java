package io.github.marcusdunn.user.login;

import io.github.marcusdunn.AbstractDatabaseTest;
import io.github.marcusdunn.ReactiveFutureBridge;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.TimeUnit;
import org.jooq.generated.tables.JUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.jooq.impl.DSL.value;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
@Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
class LoginServiceTest extends AbstractDatabaseTest {

    @Test
    void checkLoginWithNoMatchingUser(VertxTestContext testContext) {
        loginService
                .loginEmailPassword("hello", "world")
                .onComplete(asyncResult -> {
                    if (asyncResult.failed()) {
                        testContext.failNow(asyncResult.cause());
                    } else {
                        final var result = asyncResult.result();
                        result.ifPresentOrElse(
                                (user) -> testContext.failNow("user was not empty, instead was " + user),
                                testContext::completeNow
                        );
                    }
                });
    }

    @Test
    void checkLoginWithMatchingUser(VertxTestContext testContext) {
        ReactiveFutureBridge.fetchOne(
                        dsl.insertInto(
                                        JUser.USER,
                                        JUser.USER.EMAIL,
                                        JUser.USER.PASSWORD
                                )
                                .values(
                                        value("marcus.dunn@example.com", JUser.USER.EMAIL),
                                        value("password123", JUser.USER.PASSWORD)
                                )
                )
                .onComplete(
                        (insertResult) -> {
                            assertTrue(insertResult.succeeded());

                            loginService
                                    .loginEmailPassword("marcus.dunn@example.com", "password123")
                                    .onComplete(loginResult -> {
                                        if (loginResult.failed()) {
                                            testContext.failNow(loginResult.cause());
                                        } else {
                                            final var result = loginResult.result();
                                            result.ifPresentOrElse(
                                                    (user) -> testContext.completeNow(),
                                                    () -> testContext.failNow("did not find user")
                                            );
                                        }
                                    });
                        }
                );
    }
}