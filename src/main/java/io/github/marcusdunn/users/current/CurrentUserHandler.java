package io.github.marcusdunn.users.current;

import io.github.marcusdunn.users.UserDto;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import javax.inject.Inject;
import java.util.logging.Logger;

public class CurrentUserHandler implements Handler<RoutingContext> {
    private static final Logger logger = Logger.getLogger(CurrentUserHandler.class.getName());
    private final FindUserService findUserService;

    @Inject
    public CurrentUserHandler(FindUserService findUserService) {
        this.findUserService = findUserService;
    }

    @Override
    public void handle(RoutingContext event) {
        int id = event.user().principal().getInteger("id");
        findUserService.findUserById(id)
                .onSuccess(optUser -> optUser.ifPresentOrElse(user -> {
                            logger.finest("Found user: " + user);
                            event
                                    .response()
                                    .end(new UserDto(user, event.user().principal().encode()).toJsonBuffer());
                        },
                        () -> {
                            logger.finer("could not find user with id: " + id);
                            event.response().setStatusCode(404).end();
                        }))
                .onFailure(event::fail);
    }
}

