package io.github.marcusdunn.users.current;

import io.github.marcusdunn.ReactiveFutureBridge;
import io.github.marcusdunn.users.AbstractDatabaseService;
import io.r2dbc.spi.ConnectionFactory;
import io.vertx.core.Future;
import org.jooq.generated.tables.JUser;

import javax.inject.Inject;
import java.util.Optional;

public class FindUserService extends AbstractDatabaseService {
    @Inject
    public FindUserService(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    public Future<Optional<org.jooq.generated.tables.records.JUserRecord>> findUserById(int id) {
        return ReactiveFutureBridge.fetchOne(
                dsl()
                        .selectFrom(JUser.USER)
                        .where(JUser.USER.ID.eq(id))
        );
    }
}
