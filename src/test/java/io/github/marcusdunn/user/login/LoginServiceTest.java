package io.github.marcusdunn.user.login;

import io.github.marcusdunn.AbstractDatabaseTest;
import io.github.marcusdunn.ReactiveFutureBridge;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jooq.generated.tables.JUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static io.github.marcusdunn.matcher.ApprovalMatcher.isApproved;
import static io.github.marcusdunn.matcher.EmptyValueMatcher.isEmpty;
import static io.github.marcusdunn.matcher.PresentValueMatcher.hasPresentValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jooq.impl.DSL.value;

@ExtendWith(VertxExtension.class)
@Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
class LoginServiceTest extends AbstractDatabaseTest {

    @Test
    void checkLoginWithNoMatchingUser(VertxTestContext testContext) {
        loginService
                .loginEmailPassword("hello", "world")
                .onComplete(testContext.succeeding(result -> testContext
                        .verify(() -> assertThat(result, isEmpty()))
                        .completeNow()));
    }

    @Test
    void checkLoginWithMatchingUser(VertxTestContext testContext) {
        String email = "marcus.dunn@example.com";
        String password = "password123";
        ReactiveFutureBridge.fetchOne(
                        dsl.insertInto(
                                        JUser.USER,
                                        JUser.USER.EMAIL,
                                        JUser.USER.PASSWORD
                                )
                                .values(
                                        value(email, JUser.USER.EMAIL),
                                        value(password, JUser.USER.PASSWORD)
                                )
                )
                .onComplete(testContext.succeeding(i -> loginService
                        .loginEmailPassword(email, password)
                        .onComplete(testContext.succeeding(result -> testContext
                                .verify(() -> assertThat(result, hasPresentValue(isApproved())))
                                .completeNow()
                        ))));
    }
}