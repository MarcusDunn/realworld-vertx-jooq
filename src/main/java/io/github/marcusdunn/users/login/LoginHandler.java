package io.github.marcusdunn.users.login;

import io.github.marcusdunn.users.UserDto;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

import javax.inject.Inject;
import java.util.Objects;
import java.util.logging.Logger;


public class LoginHandler implements Handler<RoutingContext> {
    private static final Logger logger = Logger.getLogger(LoginHandler.class.getName());
    private final LoginService loginService;
    private final JWTAuth jwtAuth;

    @Inject
    public LoginHandler(LoginService loginService, JWTAuth jwtAuth) {
        this.loginService = loginService;
        this.jwtAuth = jwtAuth;
    }


    @Override
    public void handle(RoutingContext routingContext) {
        JsonObject jsonObject = routingContext
                .<RequestParameters>get(ValidationHandler.REQUEST_CONTEXT_KEY)
                .body()
                .getJsonObject();
        Request request = Request.fromJsonObject(jsonObject);

        loginService
                .loginEmailPassword(request.user.email, request.user.password)
                .onSuccess(userRecordOptional -> userRecordOptional.ifPresentOrElse(user -> {
                    logger.finest("Found " + user);
                    routingContext
                            .response()
                            .setStatusCode(200)
                            .end(new UserDto(
                                            user,
                                            jwtAuth.generateToken(JsonObject.of("id", user.getId()))
                                    ).toJsonBuffer()
                            );
                }, () -> {
                    logger.finer("No user was found");
                    routingContext
                            .response()
                            .setStatusCode(401)
                            .end();
                })).onFailure(routingContext::fail);
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
