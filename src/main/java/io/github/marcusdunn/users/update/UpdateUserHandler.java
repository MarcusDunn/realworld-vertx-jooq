package io.github.marcusdunn.users.update;

import io.github.marcusdunn.users.AbstractDatabaseService;
import io.github.marcusdunn.users.UserDto;
import io.r2dbc.spi.ConnectionFactory;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.TableField;
import org.jooq.generated.tables.JUser;
import org.jooq.generated.tables.records.JUserRecord;

import javax.inject.Inject;

import static io.github.marcusdunn.ReactiveFutureBridge.fetchOne;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.value;

public class UpdateUserHandler extends AbstractDatabaseService implements Handler<RoutingContext> {

    private final JWTAuth jwtAuth;

    @Inject
    public UpdateUserHandler(ConnectionFactory connectionFactory, JWTAuth jwtAuth) {
        super(connectionFactory);
        this.jwtAuth = jwtAuth;
    }

    @Override
    public void handle(RoutingContext event) {
        int id = event.user().principal().getInteger("id");
        JsonObject user = event.body().asJsonObject().getJsonObject("user");
        String username = "username";
        TableField<JUserRecord, String> username1 = JUser.USER.USERNAME;
        Field<String> value = user.containsKey(username) ? coalesce(value(user.getString(username)), username1) : username1;
        fetchOne(dsl().update(JUser.USER)
                .set(username1, value)
                .set(JUser.USER.EMAIL, getUpdateFromJsonNotNull(user, "email", JUser.USER.EMAIL))
                .set(JUser.USER.USERNAME, getUpdateFromJsonNotNull(user, "username", JUser.USER.USERNAME))
                .set(JUser.USER.PASSWORD, getUpdateFromJsonNotNull(user, "email", JUser.USER.PASSWORD))
                .set(JUser.USER.BIO, getUpdateFromJson(user, "bio", JUser.USER.BIO))
                .set(JUser.USER.IMAGE, getUpdateFromJson(user, "image", JUser.USER.IMAGE))
                .where(JUser.USER.ID.eq(id))
                .returningResult(JUser.USER)
        )
                .map(opt -> opt.map(Record1::value1))
                .onFailure(event::fail)
                .onSuccess(optUser -> optUser.ifPresentOrElse(userRecord -> event
                                .response()
                                .end(new UserDto(userRecord, jwtAuth.generateToken(JsonObject.of("id", userRecord.getId()))).toJsonBuffer()),
                        () -> event.fail(500))
                );
    }

    Field<String> getUpdateFromJsonNotNull(JsonObject user, String key, TableField<JUserRecord, String> field) {
        return user.containsKey(key) ? coalesce(value(user.getString(key)), field) : field;
    }

    Field<String> getUpdateFromJson(JsonObject user, String key, TableField<JUserRecord, String> field) {
        return user.containsKey(key) ? value(user.getString(key)) : field;
    }
}
