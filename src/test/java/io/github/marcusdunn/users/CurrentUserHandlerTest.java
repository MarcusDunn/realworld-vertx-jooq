package io.github.marcusdunn.users;

import io.github.marcusdunn.AbstractDatabaseTest;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.marcusdunn.matcher.BodyMatcher.hasBody;
import static io.github.marcusdunn.matcher.JsonBodyMatcher.withJsonObject;
import static io.github.marcusdunn.matcher.JsonStringFieldMatcher.hasStringField;
import static io.github.marcusdunn.matcher.StatusCodeMatcher.hasStatusCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith(VertxExtension.class)
class CurrentUserHandlerTest extends AbstractDatabaseTest {
    @Test
    void testBadRequestWhenNoLogin(VertxTestContext testContext) {
        main.run().onComplete(testContext.succeeding(server -> webClient
                .get("/user")
                .port(server.actualPort())
                .host("localhost")
                .send(testContext.succeeding(response -> {
                    testContext.verify(() -> assertThat(response, hasStatusCode(equalTo(401))));
                    server.close().onComplete(testContext.succeedingThenComplete());
                }))));
    }

    @Test
    void testGetsUserWhenExists(VertxTestContext testContext) {
        String email = "example@example.com";
        String password = "password123";
        String username = "frosty";
        main.run().onComplete(testContext.succeeding(server -> webClient
                .post("/users")
                .port(server.actualPort())
                .host("localhost")
                .sendJsonObject(new SignupHandler.Request(new SignupHandler.Request.User(email, username, password)).toJsonObject())
                .flatMap(signupResponse -> webClient
                        .get("/user")
                        .port(server.actualPort())
                        .host("localhost")
                        .putHeader("Authorization", "Bearer " + signupResponse.body().toJsonObject().getString("token"))
                        .send())
                .onComplete(testContext.succeeding(response -> {
                    testContext.verify(() -> assertThat(response, allOf(
                            hasStatusCode(equalTo(200)),
                            hasBody(withJsonObject(allOf(
                                    hasStringField("email", equalTo(email)),
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