package io.github.marcusdunn.user.login;

import io.github.marcusdunn.OperationHandler;
import io.github.marcusdunn.user.UserDto;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;


public class LoginOperationHandler implements OperationHandler {
    private static final Logger logger = Logger.getLogger(LoginOperationHandler.class.getName());
    private final LoginService loginService;

    @Inject
    public LoginOperationHandler(LoginService loginService) {
        this.loginService = loginService;
    }

    private static Request extractRequest(RoutingContext routingContext) {
        final var jsonObject = routingContext
                .body()
                .asJsonObject();
        if (jsonObject == null) {
            logger.finer("Json object was null");
            routingContext
                    .response()
                    .setStatusCode(422)
                    .end();
            return null;
        }
        logger.finest("Extracted: " + jsonObject);
        final var email = jsonObject.getString("email");
        if (email == null) {
            logger.finer("Email was null");
            routingContext
                    .response()
                    .setStatusCode(422)
                    .end();
            return null;
        }
        logger.finest("Extracted email: " + email);
        final var password = jsonObject.getString("password");
        if (password == null) {
            logger.finer("Password was null");
            routingContext
                    .response()
                    .setStatusCode(422)
                    .end();
            return null;
        }
        logger.finest("Extracted password");
        return new Request(email, password);
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
        final Request request = extractRequest(routingContext);
        if (request == null) return;

        loginService
                .loginEmailPassword(request.email, request.password)
                .onSuccess(userRecordOptional -> {
                    if (userRecordOptional.isPresent()) {
                        final var user = userRecordOptional.get();
                        logger.finest("Found " + user);
                        routingContext
                                .response()
                                .setStatusCode(200)
                                .end(new UserDto(user).toJsonBuffer());
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
    private record Request(String email, String password) {
        Request(String email, String password) {
            this.email = Objects.requireNonNull(email, "email");
            this.password = Objects.requireNonNull(password, "password");
        }
    }
}
