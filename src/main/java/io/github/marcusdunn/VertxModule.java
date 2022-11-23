package io.github.marcusdunn;


import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.openapi.RouterBuilderOptions;
import javax.inject.Singleton;

@Module
public class VertxModule {
    @Provides
    @Singleton
    Vertx vertx() {
        return Vertx.vertx(new VertxOptions());
    }

    @Provides
    @Singleton
    AuthenticationHandler authenticationHandler(Vertx vertx) {
        return JWTAuthHandler.create(JWTAuth.create(vertx, new JWTAuthOptions()));
    }

    @Provides
    @Singleton
    HttpServerOptions httpServerOptions() {
        return new HttpServerOptions()
                .setPort(8080);
    }

    @Provides
    @Singleton
    RouterBuilderOptions routerBuilderOptions() {
        return new RouterBuilderOptions();
    }
}
