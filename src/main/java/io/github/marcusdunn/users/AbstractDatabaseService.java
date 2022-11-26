package io.github.marcusdunn.users;

import io.r2dbc.spi.ConnectionFactory;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

public abstract class AbstractDatabaseService {
    private final ConnectionFactory connectionFactory;

    public AbstractDatabaseService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    protected DSLContext dsl() {
        return DSL.using(connectionFactory);
    }
}
