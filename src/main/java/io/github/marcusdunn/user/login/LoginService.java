package io.github.marcusdunn.user.login;

import io.vertx.core.Future;
import java.util.Optional;
import org.jooq.generated.tables.records.JUserRecord;

public interface LoginService {
    Future<Optional<JUserRecord>> loginEmailPassword(String email, String password);
}
