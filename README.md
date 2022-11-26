# ![RealWorld Example App](logo.png)

> ### Vertx codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API.

This codebase was created to demonstrate a fully fledged backend application built with **Vertx** including
CRUD operations, authentication, routing, pagination, and more.

We've gone to great lengths to adhere to the **Vertx** community styleguide & best practices.

For more information on how to this works with other frontends/backends, head over to
the [RealWorld](https://github.com/gothinkster/realworld) repo.

# Deviations from spec

We deviated from the spec in the following ways:

- tokens are passed via a header of `Authorization: Bearer <token>` not `Authorization: Token <token>`. This is because
  the Vertx JWT Auth module uses the `Bearer` scheme. [Version 2](https://github.com/gothinkster/realworld/issues/808)
  of the realworld spec will use `Bearer` as
  well ([tracking issue](https://github.com/gothinkster/realworld/issues/532)).
  
# How it works

> We use vertx libraries wherever possible. This includes [vertx-web](https://vertx.io/docs/vertx-web/java/)
> , [vertx-openapi](https://vertx.io/docs/vertx-web-openapi/java/)
> and [vertx-auth-jwt](https://vertx.io/docs/vertx-auth-jwt/java/). For dependency injection we
> use [dagger](https://dagger.dev/). And for database interactions we use [jooq](https://www.jooq.org/)
> with [r2dbc](https://r2dbc.io/) for a fully asynchronous application.

# Getting started

> ./gradlew run