package io.github.marcusdunn;


import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import org.apache.commons.configuration2.Configuration;

import javax.inject.Singleton;

@Module
public class VertxModule {
    private static String PORT_KEY = "PORT";
    private static int DEFAULT_PORT = 8080;
    @Provides
    @Singleton
    static Vertx vertx(VertxOptions vertxOptions) {
        return Vertx.vertx(vertxOptions);
    }

    @Provides
    static VertxOptions vertxOptions() {
        return new VertxOptions();
    }

    @Provides
    static AuthenticationHandler authenticationHandler(JWTAuth jwtAuth) {
        return JWTAuthHandler.create(jwtAuth);
    }

    @Provides
    static HttpServerOptions httpServerOptions(Configuration configuration) {
        return new HttpServerOptions()
                .setPort(configuration.getInt(PORT_KEY, DEFAULT_PORT));
    }

    @Provides
    static RouterBuilderOptions routerBuilderOptions() {
        return new RouterBuilderOptions();
    }

    @Provides
    static BodyHandler bodyHandler() {
        return BodyHandler.create();
    }

    @Provides
    static WebClient webClient(Vertx vertx, WebClientOptions webClientOptions) {
        return WebClient.create(vertx, webClientOptions);
    }

    @Provides
    static WebClientOptions webClientOptions() {
        return new WebClientOptions();
    }

    @Provides
    static JWTAuth jwtAuth(Vertx vertx, JWTAuthOptions jwtAuthOptions) {
        return JWTAuth.create(vertx, jwtAuthOptions);
    }

    @Provides
    static JWTAuthOptions jwtAuthOptions() {
        return new JWTAuthOptions();
    }
}
