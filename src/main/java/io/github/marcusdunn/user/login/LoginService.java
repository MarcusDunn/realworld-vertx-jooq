package io.github.marcusdunn.user.login;

import io.r2dbc.spi.ConnectionFactory;
import io.vertx.core.Future;
import java.util.Optional;
import javax.inject.Inject;
import org.jooq.generated.tables.records.JUserRecord;
import org.jooq.impl.DSL;

import static io.github.marcusdunn.ReactiveFutureBridge.fetchOne;
import static org.jooq.generated.tables.JUser.USER;

public class LoginService {
    private final ConnectionFactory connectionFactory;

    @Inject
    public LoginService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Future<Optional<JUserRecord>> loginEmailPassword(String email, String password) {
        return fetchOne(
                DSL.using(connectionFactory)
                        .selectFrom(USER)
                        .where(USER.EMAIL.eq(email).and(USER.PASSWORD.eq(password)))
        );
    }
}
