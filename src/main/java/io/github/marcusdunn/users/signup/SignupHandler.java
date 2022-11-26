package io.github.marcusdunn.users.signup;

import io.github.marcusdunn.users.UserDto;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.validation.RequestParameters;
import io.vertx.ext.web.validation.ValidationHandler;

import javax.inject.Inject;
import java.util.Objects;
import java.util.logging.Logger;

public class SignupHandler implements Handler<RoutingContext> {
    private static final Logger logger = Logger.getLogger(SignupHandler.class.getName());
    private final SignupService signupService;
    private final JWTAuth jwtAuth;

    @Inject
    public SignupHandler(SignupService signupService, JWTAuth jwtAuth) {
        this.signupService = signupService;
        this.jwtAuth = jwtAuth;
    }

    @Override
    public void handle(RoutingContext routingContext) {
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
                            .send(new UserDto(user, jwtAuth.generateToken(JsonObject.of("id", user.getId()))).toJsonBuffer());
                }, () -> routingContext.fail(500)))
                .onFailure(routingContext::fail);
    }

    /**
     * See <a href="https://realworld-docs.netlify.app/docs/specs/backend-specs/endpoints#registration">Registration</a>
     */
    public record Request(User user) {
        public Request(User user) {
            this.user = Objects.requireNonNull(user, "user");
        }

        public static Request fromJsonObject(JsonObject jsonObject) {
            return new Request(User.fromJsonObject(jsonObject.getJsonObject("user")));
        }

        public JsonObject toJsonObject() {
            return JsonObject.of()
                    .put("user", user.toJsonObject());
        }

        public Buffer toJsonBuffer() {
            return toJsonObject().toBuffer();
        }

        public record User(String email, String username, String password) {
            public User(String email, String username, String password) {
                this.email = Objects.requireNonNull(email, "email");
                this.username = Objects.requireNonNull(username, "username");
                this.password = Objects.requireNonNull(password, "password");
            }

            public static User fromJsonObject(JsonObject jsonObject) {
                return new User(
                        jsonObject.getString("email"),
                        jsonObject.getString("username"),
                        jsonObject.getString("password")
                );
            }

            public JsonObject toJsonObject() {
                return JsonObject.of()
                        .put("email", email)
                        .put("username", username)
                        .put("password", password);
            }
        }
    }
}
