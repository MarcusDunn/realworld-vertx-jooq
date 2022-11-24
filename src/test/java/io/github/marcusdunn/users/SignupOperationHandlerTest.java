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
        main.run().onComplete(testContext.succeeding(server -> webClient
                .post("/users")
                .port(server.actualPort())
                .host("localhost")
                .sendJsonObject(JsonObject.of("user", JsonObject.of(
                        "email", "example@example.com",
                        "password", "password123",
                        "username", "frosty"
                )))
                .onComplete(testContext.succeeding(response -> {
                    testContext.verify(() -> assertThat(response, allOf(
                            hasStatusCode(equalTo(200)),
                            hasBody(withJsonObject(allOf(
                                    hasStringField("email", equalTo("example@example.com")),
                                    hasStringField("username", equalTo("frosty"))
                            )))
                    )));
                    server.close().onComplete(testContext.succeedingThenComplete());
                }))));
    }
}