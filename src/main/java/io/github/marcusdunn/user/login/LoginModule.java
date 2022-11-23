package io.github.marcusdunn.user.login;

import dagger.Binds;
import dagger.Module;
import io.github.marcusdunn.DatabaseModule;
import javax.inject.Singleton;

@Module(includes = {DatabaseModule.class})
public abstract class LoginModule {
    @Binds
    @Singleton
    abstract LoginService loginService(LoginServiceImpl loginServiceImpl);
}
