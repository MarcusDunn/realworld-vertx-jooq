package io.github.marcusdunn.users;

import io.github.marcusdunn.AbstractDatabaseTest;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.marcusdunn.matcher.BodyMatcher.hasBody;
import static io.github.marcusdunn.matcher.JsonBodyMatcher.withJsonObject;
import static io.github.marcusdunn.matcher.JsonStringFieldMatcher.hasStringField;
import static io.github.marcusdunn.matcher.StatusCodeMatcher.hasStatusCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(VertxExtension.class)
class SignupOperationHandlerTest extends AbstractDatabaseTest {
    @Test
    void checkSignupNewUser(VertxTestContext testContext) {
        String email = "example@example.com";
        String password = "password123";
        main.run().onComplete(testContext.succeeding(server -> webClient
                .post("/users")
                .port(server.actualPort())
                .host("localhost")
                .sendJsonObject(JsonObject.of("user", JsonObject.of(
                        "email", email,
                        "password", password,
                        "username", "frosty"
                )))
                .onComplete(testContext.succeeding(response -> {
                    testContext.verify(() -> assertThat(response, allOf(
                            hasStatusCode(equalTo(200)),
                            hasBody(withJsonObject(allOf(
                                    hasStringField("email", equalTo(email)),
                                    hasStringField("username", equalTo("frosty"))
                            )))
                    )));
                    server.close().onComplete(testContext.succeedingThenComplete());
                }))));
    }

    @Test
    void checkSignupNewUserThenLogin(VertxTestContext testContext) {
        String email = "example@example.com";
        String password = "password123";
        String username = "frosty";
        main.run().onComplete(testContext.succeeding(server -> webClient
                .post("/users")
                .port(server.actualPort())
                .host("localhost")
                .sendJsonObject(JsonObject.of("user", JsonObject.of(
                        "email", email,
                        "password", password,
                        "username", username
                )))
                .onComplete(testContext.succeeding(signupResponse -> webClient.post("/users/login")
                        .port(server.actualPort())
                        .host("localhost")
                        .sendJsonObject(JsonObject.of("user", JsonObject.of(
                                "email", email,
                                "password", password
                        )))
                        .onComplete(testContext.succeeding(loginResponse -> {
                            testContext.verify(() -> assertThat(loginResponse, allOf(
                                    hasStatusCode(equalTo(200)),
                                    hasBody(withJsonObject(allOf(
                                            hasStringField("email", equalTo(email)),
                                            hasStringField("username", equalTo(username))
                                    )))
                            )));
                            server.close().onComplete(testContext.succeedingThenComplete());
                        }))))));
    }
}