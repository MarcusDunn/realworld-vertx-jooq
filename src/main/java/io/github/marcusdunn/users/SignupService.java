package io.github.marcusdunn.users;

import io.github.marcusdunn.ReactiveFutureBridge;
import io.r2dbc.spi.ConnectionFactory;
import io.vertx.core.Future;
import org.jooq.Record1;
import org.jooq.generated.tables.JUser;
import org.jooq.generated.tables.records.JUserRecord;
import org.jooq.impl.DSL;

import javax.inject.Inject;
import java.util.Optional;

import static org.jooq.impl.DSL.value;

public class SignupService {
    private final ConnectionFactory connectionFactory;

    @Inject
    public SignupService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Future<Optional<JUserRecord>> signup(String username, String email, String password) {
        return ReactiveFutureBridge.fetchOne(
                        DSL.using(connectionFactory)
                                .insertInto(JUser.USER,
                                        JUser.USER.EMAIL,
                                        JUser.USER.PASSWORD,
                                        JUser.USER.USERNAME
                                )
                                .values(
                                        value(email, JUser.USER.EMAIL),
                                        value(password, JUser.USER.PASSWORD),
                                        value(username, JUser.USER.USERNAME)
                                )
                                .returningResult(JUser.USER)
                )
                .map(optionalRecord1 -> optionalRecord1.map(Record1::value1));
    }
}
