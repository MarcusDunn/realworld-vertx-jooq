package io.github.marcusdunn;

import liquibase.exception.LiquibaseException;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractDatabaseTest extends AbstractIntegrationTest {
    public final DSLContext dsl = DSL.using(connectionFactory);

    @BeforeEach
    void setUp() {
        try {
            liquibase.dropAll();
            liquibase.update();
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }
}

