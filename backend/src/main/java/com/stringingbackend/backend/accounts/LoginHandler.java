package com.stringingbackend.backend.accounts;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.handler.JWTAuthHandler;
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
            .handler(ctx -> loginUser(ctx));
        router.get("/getUser")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(ctx -> getFirstName(ctx));
    }

    private Future<Void> loginUser(RoutingContext ctx) {

        return loginService.loginUser(ctx)
            .compose(user -> {

                String token = jwtAuth.generateToken(
                    new JsonObject()
                        .put("email", user.getString("email"))
                        .put("firstName", user.getString("firstName"))
                        .put("lastName", user.getString("lastName"))
                        .put("isAdmin", user.getBoolean("isAdmin"))
                );

                JsonObject response = new JsonObject()
                    .put("token", token);

                ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(response.encode());

                return Future.<Void>succeededFuture();
            })
            .recover(err -> {

                if (!ctx.response().ended()) {

                    int statusCode =
                        "Invalid credentials".equals(err.getMessage())
                            ? 403
                            : "Missing credentials".equals(err.getMessage())
                                ? 400
                                : 500;

                    ctx.response()
                        .setStatusCode(statusCode)
                        .end(
                            statusCode == 403
                                ? "Login fehlgeschlagen"
                                : statusCode == 400
                                    ? "Fehlende Daten"
                                    : "Interner Server Fehler"
                        );
                }

                return Future.<Void>succeededFuture();
            });
    }

    private void getFirstName(RoutingContext ctx){
        JsonObject obj = ctx.user().principal();

        String firstName = obj.getString("firstName");
        String isAdmin = obj.getString("email");

        if(firstName == null || firstName.isBlank()){
            ctx.response() 
                .setStatusCode(304)
                .end("First Name could not be decoded");
            return;
        }

        if(isAdmin == null || isAdmin.isBlank()){
            ctx.response() 
                .setStatusCode(304)
                .end("Admin could not be decoded");
            return;
        }

        JsonObject ans = new JsonObject().put("firstName", firstName).put("isAdmin", isAdmin);

        ctx.response()
            .setStatusCode(200)
            .end(ans.encode());
    }
}