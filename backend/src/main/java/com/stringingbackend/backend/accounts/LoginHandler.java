package com.stringingbackend.backend.accounts;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class LoginHandler {

    private final LoginService loginService;
    private final JWTAuth jwtAuth;

    public LoginHandler(LoginService loginService, JWTAuth jwtAuth) {
        this.loginService = loginService;
        this.jwtAuth = jwtAuth;
    }

    public void registerRoutes(Router router) {
        router.post("/loginUser")
            .handler(this::loginUser);
    }

    private Future<Void> loginUser(RoutingContext ctx) {
        System.out.println("[LoginHandler] loginUser called");

        return loginService.loginUser(ctx)
            .compose(user -> {

                String token = jwtAuth.generateToken(
                    new JsonObject()
                        .put("userId", user.getInteger("user_id"))
                        .put("email", user.getString("email"))
                        .put("firstName", user.getString("firstName"))
                        .put("lastName", user.getString("lastName"))
                        .put("role", user.getString("role"))
                );

                JsonObject response = new JsonObject()
                    .put("token", token);

                System.out.println("[LoginHandler] (200) loginUser succeeded for "+ user.getString("email"));

                ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(response.encode());

                return Future.<Void>succeededFuture();
            })
            .recover(err -> {
                if (!ctx.response().ended()) {
                    int statusCode = "Invalid credentials".equals(err.getMessage()) ? 403 : "Missing credentials".equals(err.getMessage()) ? 400 : 500;

                    System.err.println("[LoginHandler] ("+ statusCode +") loginUser failed");

                    ctx.response()
                        .setStatusCode(statusCode)
                        .end(statusCode == 403 ? "Login fehlgeschlagen" : statusCode == 400 ? "Fehlende Daten" : "Interner Server Fehler");
                }

                return Future.<Void>succeededFuture();
            });
    }
}