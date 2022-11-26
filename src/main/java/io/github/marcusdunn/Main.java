package io.github.marcusdunn;

import dagger.Component;
import io.github.marcusdunn.users.UsersModule;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.RouterBuilderOptions;

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

    @Inject
    public Main(
            Vertx vertx,
            Map<String, Handler<RoutingContext>> operationHandlers,
            BodyHandler bodyHandler,
            AuthenticationHandler authenticationHandler,
            HttpServerOptions httpServerOptions,
            RouterBuilderOptions routerBuilderOptions
    ) {
        this.vertx = vertx;
        this.operationHandlers = operationHandlers;
        this.bodyHandler = bodyHandler;
        this.authenticationHandler = authenticationHandler;
        this.httpServerOptions = httpServerOptions;
        this.routerBuilderOptions = routerBuilderOptions;
    }

    public static void main(String[] args) {
        logger.info("Starting.");
        final var startMillis = System.currentTimeMillis();
        DaggerMain_RealWorld
                .create()
                .main()
                .run()
                .onSuccess(httpServer ->
                        logger.info(() -> "Started in " + (System.currentTimeMillis() - startMillis) + "ms.")
                );
    }

    public Future<HttpServer> run() {
        return RouterBuilder
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
                            .requestHandler(builder.createRouter())
                            .listen()
                            .onSuccess(httpServer -> logger.info(() -> "Server listening on " + httpServer.actualPort()))
                            .onFailure(t -> logger.log(Level.SEVERE, "HttpServer failed to start listening.", t));
                }).onFailure(t -> logger.log(Level.SEVERE, "Failed to create a routerBuilder from openapi spec.", t));
    }

    @Component(modules = {VertxModule.class, UsersModule.class, DatabaseModule.class, ConfigModule.class})
    @Singleton
    interface RealWorld {
        Main main();
    }
}
