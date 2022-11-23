package io.github.marcusdunn;

import dagger.Component;
import io.github.marcusdunn.user.UserModule;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;

public class Main {
    private final static Logger logger = Logger.getLogger(Main.class.getName());
    private final Vertx vertx;
    private final Set<OperationHandler> operationHandlers;
    private final AuthenticationHandler authenticationHandler;
    private final HttpServerOptions httpServerOptions;
    private final RouterBuilderOptions routerBuilderOptions;

    @Inject
    public Main(Vertx vertx,
                Set<OperationHandler> operationHandlers,
                AuthenticationHandler authenticationHandler,
                HttpServerOptions httpServerOptions,
                RouterBuilderOptions routerBuilderOptions
    ) {
        this.vertx = vertx;
        this.operationHandlers = operationHandlers;
        this.authenticationHandler = authenticationHandler;
        this.httpServerOptions = httpServerOptions;
        this.routerBuilderOptions = routerBuilderOptions;
    }

    public static void main(String[] args) {
        final var startMillis = System.currentTimeMillis();
        DaggerMain_RealWorld
                .create()
                .main()
                .run()
                .onSuccess(httpServer ->
                        logger.info(() -> "started in " + (System.currentTimeMillis() - startMillis) + "ms")
                );
    }

    public Future<HttpServer> run() {
        return RouterBuilder
                .create(vertx, "src/main/resources/openapi.yml")
                .flatMap(builder -> {
                    builder.setOptions(routerBuilderOptions);
                    operationHandlers.forEach(operationHandler -> {
                        final var operationName = operationHandler.operationName();
                        logger.fine(() -> "Registering " + operationName);
                        builder.operation(operationName).handler(operationHandler.handler());
                    });
                    logger.fine(() -> "Registering Security handler Token");
                    builder.securityHandler("Token", authenticationHandler);
                    logger.fine(() -> "Creating httpServer");
                    return vertx.createHttpServer(httpServerOptions)
                            .requestHandler(builder.createRouter())
                            .listen()
                            .onSuccess(httpServer -> logger.info(() -> "Server listening on " + httpServer.actualPort()))
                            .onFailure(t -> logger.log(Level.SEVERE, "HttpServer failed to start listening.", t));
                }).onFailure(t -> logger.log(Level.SEVERE, "Failed to create a routerBuilder from openapi spec.", t));
    }

    @Component(modules = {VertxModule.class, UserModule.class})
    @Singleton
    interface RealWorld {
        Main main();
    }
}
