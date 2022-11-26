package io.github.marcusdunn.users;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import io.github.marcusdunn.users.login.LoginHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * contains the classes used in the sub-route /users
 */
@Module
public abstract class UsersModule {
    @IntoMap
    @StringKey("Login")
    @Binds
    abstract Handler<RoutingContext> loginHandler(LoginHandler loginHandler);

    @IntoMap
    @StringKey("CreateUser")
    @Binds
    abstract Handler<RoutingContext> signupHandler(SignupHandler signupHandler);

    @IntoMap
    @StringKey("GetCurrentUser")
    @Binds
    abstract Handler<RoutingContext> currentUserHandler(CurrentUserHandler currentUserHandler);
}