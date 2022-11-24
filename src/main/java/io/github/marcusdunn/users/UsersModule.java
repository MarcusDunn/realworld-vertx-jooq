package io.github.marcusdunn.users;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.github.marcusdunn.OperationHandler;
import io.github.marcusdunn.users.login.LoginOperationHandler;

/**
 * contains the classes used in the sub-route /users
 */
@Module
public abstract class UsersModule {
    @IntoSet
    @Binds
    abstract OperationHandler loginOperationHandler(LoginOperationHandler loginOperationHandler);

    @IntoSet
    @Binds
    abstract OperationHandler SignupOperationHandler(SignupOperationHandler loginOperationHandler);
}