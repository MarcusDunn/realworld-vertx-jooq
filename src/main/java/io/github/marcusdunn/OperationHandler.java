package io.github.marcusdunn;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface OperationHandler {
    String operationName();
    Handler<RoutingContext> handler();
}
