package io.github.marcusdunn;

import dagger.Module;
import dagger.Provides;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.EnvironmentConfiguration;

@Module
public class ConfigModule {
    @Provides
    static Configuration configuration() {
        return new EnvironmentConfiguration();
    }
}
