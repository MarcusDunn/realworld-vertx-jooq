package io.github.marcusdunn.user.login;

import io.github.marcusdunn.AbstractDatabaseTest;
import io.github.marcusdunn.OperationHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
class LoginOperationHandlerTest extends AbstractDatabaseTest {
    final OperationHandler loginOperationHandler = new LoginOperationHandler(new LoginServiceImpl(connectionFactory));

    @Test
    void testLoginWithNoSuchUser(Vertx vertx, VertxTestContext vertxTestContext) {
        final var router = Router.router(vertx);
        router
                .route("/")
                .handler(BodyHandler.create())
                .handler(loginOperationHandler.handler());
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(0, vertxTestContext.succeeding((server) -> WebClient.create(vertx)
                        .get("/")
                        .port(server.actualPort())
                        .host("localhost")
                        .sendBuffer(
                                new JsonObject()
                                        .put("email", "marcus.s.dunn@example.com")
                                        .put("password", "password")
                                        .toBuffer()
                        ).onComplete(vertxTestContext.succeeding((response) -> {
                            vertxTestContext.verify(() -> assertEquals(401, response.statusCode()));
                            vertxTestContext.completeNow();
                        }))));
    }

    @Test
    void testLoginWithNoBody(Vertx vertx, VertxTestContext vertxTestContext) {
        final var router = Router.router(vertx);
        router
                .route("/")
                .handler(BodyHandler.create())
                .handler(loginOperationHandler.handler());
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(0, vertxTestContext.succeeding((server) -> WebClient.create(vertx)
                        .get("/")
                        .port(server.actualPort())
                        .host("localhost")
                        .send()
                        .onComplete(vertxTestContext.succeeding((response) -> {
                            vertxTestContext.verify(() -> assertEquals(400, response.statusCode()));
                            vertxTestContext.completeNow();
                        }))));
    }
}