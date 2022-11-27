package io.github.marcusdunn;

import dagger.Component;
import io.github.marcusdunn.users.UsersModule;
import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import io.vertx.ext.web.validation.BodyProcessorException;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private final static Logger logger = Logger.getLogger(Main.class.getName());
    private final Vertx vertx;
    private final Map<String, Handler<RoutingContext>> operationHandlers;
    private final BodyHandler bodyHandler;
    private final AuthenticationHandler authenticationHandler;
    private final HttpServerOptions httpServerOptions;
    private final RouterBuilderOptions routerBuilderOptions;
    private final Liquibase liquibase;

    @Inject
    public Main(
            Vertx vertx,
            Map<String, Handler<RoutingContext>> operationHandlers,
            BodyHandler bodyHandler,
            AuthenticationHandler authenticationHandler,
            HttpServerOptions httpServerOptions,
            RouterBuilderOptions routerBuilderOptions,
            Liquibase liquibase
    ) {
        this.vertx = vertx;
        this.operationHandlers = operationHandlers;
        this.bodyHandler = bodyHandler;
        this.authenticationHandler = authenticationHandler;
        this.httpServerOptions = httpServerOptions;
        this.routerBuilderOptions = routerBuilderOptions;
        this.liquibase = liquibase;
    }

    public static void main(String[] args) {
        logger.info("Starting.");
        final var startMillis = System.currentTimeMillis();
        DaggerMain_RealWorld
                .create()
                .main()
                .run()
                .onSuccess(httpServer -> {
                    logger.info(() -> "Server listening on " + httpServer.actualPort());
                    logger.info(() -> "Started in " + (System.currentTimeMillis() - startMillis) + "ms.");
                })
                .onFailure(throwable -> {
                    logger.log(Level.SEVERE, "Failed to start server", throwable);
                    System.exit(1);
                });
    }

    public Future<HttpServer> run() {
        Future<Object> liquibaseUpdate = vertx.executeBlocking((promise) -> {
            try {
                liquibase.update();
                liquibase.close();
                promise.complete();
            } catch (LiquibaseException e) {
                promise.fail(e);
            }
        });
        var createServer = RouterBuilder
                .create(vertx, "openapi.yml")
                .flatMap(builder -> {
                    builder
                            .setOptions(routerBuilderOptions)
                            .rootHandler(bodyHandler)
                            .securityHandler("Token", authenticationHandler)
                            .operations()
                            .forEach(operation ->
                                    Optional.ofNullable(operationHandlers.get(operation.getOperationId()))
                                            .ifPresentOrElse(handler -> {
                                                logger.info("registering " + handler.getClass().getSimpleName() + " for operation: " + operation.getOperationId());
                                                operation.handler(handler);
                                            }, () -> logger.warning("No handler for operation: " + operation.getOperationId()))
                            );
                    return vertx
                            .createHttpServer(httpServerOptions)
                            .requestHandler(builder.createRouter()
                                    .errorHandler(400, rc -> {
                                        logger.warning("Bad request: " + rc.failure().getMessage());
                                        if (rc.failed() && rc.failure() instanceof BodyProcessorException bodyProcessorException) {
                                            rc
                                                    .response()
                                                    .setStatusCode(422)
                                                    .end(JsonObject.of("errors", JsonObject.of("body", JsonArray.of(bodyProcessorException.getCause().getMessage()))).toBuffer());
                                        } else {
                                            rc.next();
                                        }
                                    }))
                            .listen()
                            .onSuccess(t -> logger.info("Server listening on " + t.actualPort()))
                            .onFailure(t -> logger.log(Level.SEVERE, "HttpServer failed to start listening.", t));
                }).onFailure(t -> logger.log(Level.SEVERE, "Failed to create a routerBuilder from openapi spec.", t));
        return CompositeFuture
                .all(liquibaseUpdate, createServer)
                .compose(cf -> Future.succeededFuture(cf.resultAt(1)));
    }

    @Component(modules = {VertxModule.class, UsersModule.class, DatabaseModule.class, ConfigModule.class})
    @Singleton
    interface RealWorld {
        Main main();
    }
}
