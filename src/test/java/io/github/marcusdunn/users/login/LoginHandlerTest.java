package io.github.marcusdunn.users.login;

import io.github.marcusdunn.AbstractDatabaseTest;
import io.vertx.core.CompositeFuture;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jooq.generated.tables.JUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static io.github.marcusdunn.ReactiveFutureBridge.fetchOne;
import static io.github.marcusdunn.matcher.BodyMatcher.hasBody;
import static io.github.marcusdunn.matcher.JsonBodyMatcher.withJsonObject;
import static io.github.marcusdunn.matcher.JsonStringFieldMatcher.hasStringField;
import static io.github.marcusdunn.matcher.StatusCodeMatcher.hasStatusCode;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jooq.impl.DSL.value;

@ExtendWith(VertxExtension.class)
@Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
class LoginHandlerTest extends AbstractDatabaseTest {

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
                            vertxTestContext.verify(() -> assertThat(response, hasStatusCode(equalTo(401))));
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
                    vertxTestContext.verify(() -> assertThat(response, hasStatusCode(equalTo(400))));
                    server.close().onComplete(vertxTestContext.succeedingThenComplete());
                }))));
    }

    @Test
    void testLoginWithActualUser(VertxTestContext vertxTestContext) {
        final String email = "marcus@example.com";
        final String password = "password";
        final String username = "frosty";
        CompositeFuture.all(
                        fetchOne(dsl
                                .insertInto(JUser.USER,
                                        JUser.USER.EMAIL,
                                        JUser.USER.PASSWORD,
                                        JUser.USER.USERNAME
                                )
                                .values(
                                        value(email, JUser.USER.EMAIL),
                                        value(password, JUser.USER.PASSWORD),
                                        value(username, JUser.USER.USERNAME)
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
                                    vertxTestContext.verify(() -> assertThat(response, allOf(
                                            hasStatusCode(equalTo(200)),
                                            hasBody(withJsonObject(allOf(
                                                    hasStringField("email", equalTo(email)),
                                                    hasStringField("username", equalTo(username)),
                                                    hasStringField("token", notNullValue()),
                                                    hasStringField("image", nullValue()),
                                                    hasStringField("bio", nullValue()))
                                            )))
                                    ));
                                    server.close().onComplete(vertxTestContext.succeedingThenComplete());
                                }))
                ));
    }
}