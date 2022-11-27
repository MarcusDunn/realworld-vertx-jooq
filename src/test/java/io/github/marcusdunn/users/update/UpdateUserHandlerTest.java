package io.github.marcusdunn.users.update;

import io.github.marcusdunn.AbstractDatabaseTest;
import io.github.marcusdunn.users.signup.SignupHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static io.github.marcusdunn.matcher.BodyMatcher.hasBody;
import static io.github.marcusdunn.matcher.JsonBodyMatcher.withJsonObject;
import static io.github.marcusdunn.matcher.JsonStringFieldMatcher.hasStringField;
import static io.github.marcusdunn.matcher.StatusCodeMatcher.hasStatusCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(VertxExtension.class)
@Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
class UpdateUserHandlerTest extends AbstractDatabaseTest {
    @Test
    void checkUpdateWithNoAuth(VertxTestContext testContext) {
        String email = "marcus.dunn1@example.com";
        main.run().onComplete(testContext.succeeding(server -> webClient
                .put("/user")
                .port(server.actualPort())
                .host("localhost")
                .sendJsonObject(JsonObject.of("user", JsonObject.of("email", email)))
                .onComplete(testContext.succeeding(response -> {
                    testContext.verify(() -> assertThat(response, hasStatusCode(equalTo(401))));
                    server.close().onComplete(testContext.succeedingThenComplete());
                }))));
    }

    @Test
    void checkUpdateWithAuth(VertxTestContext testContext) {
        String newEmail = "marcus.dunn1@example.com";
        String oldEmail = "example@example.com";
        String password = "password123";
        String username = "frosty";
        main.run().onComplete(testContext.succeeding(server -> webClient
                .post("/users")
                .port(server.actualPort())
                .host("localhost")
                .sendJsonObject(new SignupHandler.Request(new SignupHandler.Request.User(oldEmail, username, password)).toJsonObject())
                .flatMap(signupResponse -> webClient.put("/user")
                        .port(server.actualPort())
                        .host("localhost")
                        .putHeader("Authorization", "Bearer " + signupResponse.bodyAsJsonObject().getString("token"))
                        .sendJsonObject(JsonObject.of("user", JsonObject.of("email", newEmail))))
                .onComplete(testContext.succeeding(response -> {
                    testContext.verify(() -> assertThat(response, allOf(
                            hasStatusCode(equalTo(200)),
                            hasBody(withJsonObject(allOf(
                                    hasStringField("email", equalTo(newEmail)),
                                    hasStringField("username", equalTo(username)),
                                    hasStringField("token", notNullValue()),
                                    hasStringField("bio", nullValue()),
                                    hasStringField("image", nullValue())
                            ))))
                    ));
                    server.close().onComplete(testContext.succeedingThenComplete());
                }))));
    }
}