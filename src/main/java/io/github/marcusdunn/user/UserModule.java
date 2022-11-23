package io.github.marcusdunn.user;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import io.github.marcusdunn.OperationHandler;
import io.github.marcusdunn.user.login.LoginModule;
import io.github.marcusdunn.user.login.LoginOperationHandler;

@Module(includes = {LoginModule.class})
public abstract class UserModule {
    @IntoSet
    @Binds
    abstract OperationHandler loginOperationHandler(LoginOperationHandler loginOperationHandler);
}
