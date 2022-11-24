package io.github.marcusdunn.users.login;

import io.github.marcusdunn.OperationHandler;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;


public class LoginOperationHandler implements OperationHandler {
    private static final Logger logger = Logger.getLogger(LoginOperationHandler.class.getName());
    private final LoginService loginService;
    private final JWTAuth jwtAuth;

    @Inject
    public LoginOperationHandler(LoginService loginService, JWTAuth jwtAuth) {
        this.loginService = loginService;
        this.jwtAuth = jwtAuth;
    }

    @Override
    public String operationName() {
        return "Login";
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

        loginService
                .loginEmailPassword(request.user.email, request.user.password)
                .onSuccess(userRecordOptional -> {
                    if (userRecordOptional.isPresent()) {
                        final var user = userRecordOptional.get();
                        logger.finest("Found " + user);
                        routingContext
                                .response()
                                .setStatusCode(200)
                                .end(new UserDto(
                                                user,
                                                jwtAuth.generateToken(JsonObject.of("email", user.getEmail()))
                                        ).toJsonBuffer()
                                );
                    } else {
                        logger.finer("No user was found");
                        routingContext
                                .response()
                                .setStatusCode(401)
                                .end();
                    }
                }).onFailure(t -> {
                    logger.log(Level.SEVERE, "Error while logging in with email and password", t);
                    routingContext
                            .response()
                            .setStatusCode(500);
                });
    }

    /**
     * see <a href="https://realworld-docs.netlify.app/docs/specs/backend-specs/endpoints#authentication">Authentication</a>
     */
    private record Request(User user) {
        Request(User user) {
            this.user = Objects.requireNonNull(user);
        }

        static Request fromJsonObject(JsonObject jsonObject) {
            Objects.requireNonNull(jsonObject, "jsonObject");
            return new Request(User.fromJsonObject(jsonObject.getJsonObject("user")));
        }

        private record User(String email, String password) {
            User(String email, String password) {
                this.email = Objects.requireNonNull(email, "email");
                this.password = Objects.requireNonNull(password, "password");
            }

            static User fromJsonObject(JsonObject jsonObject) {
                Objects.requireNonNull(jsonObject, "jsonObject");
                return new User(
                        jsonObject.getString("email"),
                        jsonObject.getString("password")
                );
            }
        }
    }
}
