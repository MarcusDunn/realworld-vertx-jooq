package io.github.marcusdunn.user.login;

import io.github.marcusdunn.AbstractDatabaseTest;
import io.vertx.core.CompositeFuture;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.TimeUnit;
import org.jooq.generated.tables.JUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.marcusdunn.BodyMatcher.hasBody;
import static io.github.marcusdunn.JsonBodyMatcher.withJsonObject;
import static io.github.marcusdunn.JsonStringFieldMatcher.hasStringField;
import static io.github.marcusdunn.ReactiveFutureBridge.fetchOne;
import static io.github.marcusdunn.StatusCodeMatcher.hasStatusCode;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jooq.impl.DSL.value;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
@Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
class LoginOperationHandlerTest extends AbstractDatabaseTest {

    @Test
    void testLoginWithNoSuchUser(VertxTestContext vertxTestContext) {
        main.run().onComplete(vertxTestContext.succeeding(server -> webClient
                .post("/users/login")
                .port(server.actualPort())
                .host("localhost")
                .sendJsonObject(JsonObject.of(
                        "user", JsonObject.of(
                                "email", "marcus@example.com",
                                "password", "password123"
                        ))
                )
                .onComplete(vertxTestContext.succeeding(response -> {
                            vertxTestContext.verify(() -> assertEquals(401, response.statusCode()));
                            server.close().onComplete(vertxTestContext.succeedingThenComplete());
                        }
                ))));
    }

    @Test
    void testLoginWithNoBody(VertxTestContext vertxTestContext) {
        main.run().onComplete(vertxTestContext.succeeding(server -> webClient
                .post("/users/login")
                .port(server.actualPort())
                .host("localhost")
                .send()
                .onComplete(vertxTestContext.succeeding((response) -> {
                    vertxTestContext.verify(() -> assertEquals(400, response.statusCode()));
                    server.close().onComplete(vertxTestContext.succeedingThenComplete());
                }))));
    }

    @Test
    void testLoginWithActualUser(VertxTestContext vertxTestContext) {
        final String email = "marcus@example.com";
        final String password = "password";
        CompositeFuture.all(
                        fetchOne(dsl
                                .insertInto(JUser.USER,
                                        JUser.USER.EMAIL,
                                        JUser.USER.PASSWORD
                                )
                                .values(
                                        value(email, JUser.USER.EMAIL),
                                        value(password, JUser.USER.PASSWORD)
                                )),
                        main.run()
                )
                .map(result -> result.<HttpServer>resultAt(1))
                .onComplete(vertxTestContext.succeeding(server ->
                        webClient
                                .post("/users/login")
                                .port(server.actualPort())
                                .host("localhost")
                                .sendJsonObject(JsonObject.of(
                                        "user", JsonObject.of(
                                                "email", email,
                                                "password", password
                                        ))
                                )
                                .onComplete(vertxTestContext.succeeding((response) -> {
                                    vertxTestContext.verify(() -> assertThat(response, hasStatusCode(equalTo(200))));
                                    vertxTestContext.verify(() -> assertThat(response, hasBody(withJsonObject(
                                            allOf(
                                                    hasStringField("email", equalTo(email)),
                                                    hasStringField("username", equalTo(email)),
                                                    hasStringField("token", notNullValue()),
                                                    hasStringField("image", nullValue()),
                                                    hasStringField("bio", nullValue())
                                            )
                                    ))));
                                    server.close().onComplete(vertxTestContext.succeedingThenComplete());
                                }))
                ));
    }
}