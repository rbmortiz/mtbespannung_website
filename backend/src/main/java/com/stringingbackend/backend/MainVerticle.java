package com.stringingbackend.backend;

import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgBuilder;
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
import com.stringingbackend.backend.dashboard.DashboardHandler;
import com.stringingbackend.backend.dashboard.DashboardService;

// CORS Handler
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.CorsHandler;

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

    router.route().handler(
        CorsHandler.create()
            .addOrigin("https://mtbespannung.de")
            .allowedMethod(HttpMethod.GET)
            .allowedMethod(HttpMethod.POST)
            .allowedMethod(HttpMethod.PUT)
            .allowedMethod(HttpMethod.DELETE)
            .allowedMethod(HttpMethod.OPTIONS)
            .allowedMethod(HttpMethod.UPDATE)
            .allowedHeader("Content-Type")
            .allowedHeader("Authorization")
    );

    // Database
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setHost(System.getenv("DB_HOST"))
      .setPort(Integer.parseInt(System.getenv("DB_PORT")))
      .setDatabase(System.getenv("DB_NAME"))
      .setUser(System.getenv("DB_USER"))
      .setPassword(System.getenv("DB_PASSWORD"));

    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(5);

    pool = PgBuilder
      .pool()
      .with(poolOptions)
      .connectingTo(connectOptions)
      .using(vertx)
      .build();

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
    RegisterHandler registerHandler = new RegisterHandler(registerService, jwtAuth);

    DashboardService dashboardService = new DashboardService(accountRepository);
    DashboardHandler dashboardHandler = new DashboardHandler(dashboardService, jwtAuth);

    // Route declaration
    router.route().handler(BodyHandler.create());
    loginHandler.registerRoutes(router);
    registerHandler.registerRoutes(router);
    dashboardHandler.registerRoutes(router);

    router.get("/health").handler(ctx -> {
      ctx.response() 
        .setStatusCode(200)
        .putHeader("Content-Type", "application/json")
        .end(new JsonObject().put("status", "ok").put("host", System.getenv("DB_HOST")).encode());
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