package io.github.marcusdunn.users;

import io.github.marcusdunn.OperationHandler;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

import javax.inject.Inject;
import java.util.Objects;

public class SignupOperationHandler implements OperationHandler {
    @Inject
    public SignupOperationHandler() {
    }

    @Override
    public String operationName() {
        return "CreateUser";
    }

    @Override
    public Handler<RoutingContext> handler() {
        return this::handle;
    }

    private void handle(RoutingContext routingContext) {
        JsonObject jsonObject = routingContext
                .<RequestParameters>get(ValidationHandler.REQUEST_CONTEXT_KEY)
                .body()
                .getJsonObject();

        routingContext
                .response()
                .setStatusCode(200)
                .send(jsonObject.getJsonObject("user").toBuffer());
    }

    /**
     * See <a href="https://realworld-docs.netlify.app/docs/specs/backend-specs/endpoints#registration">Registration</a>
     */
    private record Request(User user) {
        private Request(User user) {
            this.user = Objects.requireNonNull(user, "user");
        }

        static Request fromJsonObject(JsonObject jsonObject) {
            return new Request(User.fromJsonObject(jsonObject.getJsonObject("user")));
        }

        private record User(String email, String username, String password) {
            private User(String email, String username, String password) {
                this.email = Objects.requireNonNull(email, "email");
                this.username = Objects.requireNonNull(username, "username");
                this.password = Objects.requireNonNull(password, "password");
            }

            static User fromJsonObject(JsonObject jsonObject) {
                return new User(
                        jsonObject.getString("email"),
                        jsonObject.getString("username"),
                        jsonObject.getString("password")
                );
            }
        }
    }
}
