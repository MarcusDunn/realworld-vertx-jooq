package io.github.marcusdunn.users.login;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.github.marcusdunn.OperationHandler;

@Module
public abstract class UserModule {
    @IntoSet
    @Binds
    abstract OperationHandler loginOperationHandler(LoginOperationHandler loginOperationHandler);
}