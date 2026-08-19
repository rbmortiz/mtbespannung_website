package com.stringingbackend.backend;

import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

// Database imports
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.ext.auth.PubSecKeyOptions;

// JWTAuth
import io.vertx.ext.auth.jwt.*;

// Service Imports
import com.stringingbackend.backend.accounts.AccountRepository;
import com.stringingbackend.backend.accounts.LoginHandler;
import com.stringingbackend.backend.accounts.LoginService;
import com.stringingbackend.backend.accounts.RegisterHandler;
import com.stringingbackend.backend.accounts.RegisterService;

// Misc imports
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends VerticleBase {

  private Pool pool;
  private JWTAuth jwtAuth;

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }

  @Override
  public Future<?> start() {

    // Router init
    Router router = Router.router(vertx);

    // Database
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setHost("localhost")
      .setPort(5432)
      .setDatabase("mtbespannung")
      .setUser("postgres")
      .setPassword("samplePassword");

    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    pool = Pool.pool(vertx, connectOptions, poolOptions);

    // JWT
    jwtAuth = JWTAuth.create(
      vertx,
      new JWTAuthOptions()
        .addPubSecKey(
          new PubSecKeyOptions()
            .setAlgorithm("HS256")
            .setBuffer("mtbespannung_server")
        )
      );

    // Service Creation
    AccountRepository accountRepository = new AccountRepository(pool);

    LoginService loginService = new LoginService(accountRepository);
    LoginHandler loginHandler = new LoginHandler(loginService, jwtAuth);

    RegisterService registerService = new RegisterService(accountRepository);
    RegisterHandler registerHandler = new RegisterHandler(registerService);

    // Route declaration
    router.route().handler(BodyHandler.create());
    loginHandler.registerRoutes(router);
    registerHandler.registerRoutes(router);

    router.get("/health").handler(ctx -> {
      ctx.response() 
        .setStatusCode(200)
        .putHeader("Content-Type", "application/json")
        .end(new JsonObject().put("status", "ok").encode());
    });

    // Create Server
    return vertx.createHttpServer()
      .requestHandler(router)
      .listen(8080)
        .onSuccess(server -> {
          System.out.println("Backend listening on Port: " + server.actualPort());
        })
        // Print the problem on failure
        .onFailure(throwable -> {
          throwable.printStackTrace();
        });
  }
}
