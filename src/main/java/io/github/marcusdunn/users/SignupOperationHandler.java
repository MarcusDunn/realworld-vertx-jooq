package io.github.marcusdunn.users;

import io.github.marcusdunn.OperationHandler;
import io.github.marcusdunn.users.login.UserDto;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

import javax.inject.Inject;
import java.util.Objects;
import java.util.logging.Logger;

public class SignupOperationHandler implements OperationHandler {
    private static final Logger logger = Logger.getLogger(SignupOperationHandler.class.getName());
    private final SignupService signupService;
    private final JWTAuth jwtAuth;

    @Inject
    public SignupOperationHandler(SignupService signupService, JWTAuth jwtAuth) {
        this.signupService = signupService;
        this.jwtAuth = jwtAuth;
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
        Request request = Request.fromJsonObject(jsonObject);
        logger.finest(() -> "Extracted: " + request);
        signupService
                .signup(request.user.username, request.user.email, request.user.password)
                .onSuccess(optionalUser -> optionalUser.ifPresentOrElse((user) -> {
                    logger.finest(() -> "Signed up: " + user);
                    routingContext
                            .response()
                            .setStatusCode(200)
                            .send(new UserDto(user, jwtAuth.generateToken(JsonObject.of(
                                    "username", user.getUsername(),
                                    "email", user.getEmail()
                            ))).toJsonBuffer());
                }, () -> routingContext.fail(500)))
                .onFailure(routingContext::fail);
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
