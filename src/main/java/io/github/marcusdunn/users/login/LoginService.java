package io.github.marcusdunn.users.login;

import io.github.marcusdunn.users.AbstractDatabaseService;
import io.r2dbc.spi.ConnectionFactory;
import io.vertx.core.Future;
import org.jooq.generated.tables.records.JUserRecord;

import javax.inject.Inject;
import java.util.Optional;

import static io.github.marcusdunn.ReactiveFutureBridge.fetchOne;
import static org.jooq.generated.tables.JUser.USER;

public class LoginService extends AbstractDatabaseService {

    @Inject
    public LoginService(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    public Future<Optional<JUserRecord>> loginEmailPassword(String email, String password) {
        return fetchOne(
                dsl()
                        .selectFrom(USER)
                        .where(USER.EMAIL.eq(email).and(USER.PASSWORD.eq(password)))
        );
    }
}
