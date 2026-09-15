package com.stringingbackend.backend.accounts;

import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;

public class RegisterHandler {
    
    private final RegisterService registerService;
    private final JWTAuth jwtAuth;

    public RegisterHandler(RegisterService registerService, JWTAuth jwtAuth){
        this.registerService = registerService;
        this.jwtAuth = jwtAuth;
    }

    public void registerRoutes(Router router){
        router.post("/register")
            .handler(this::registerUser);
    }

    private void registerUser(RoutingContext ctx) {
        System.out.println("[RegisterHandler] registerUser called");

        registerService.registerUser(ctx)
            .onSuccess(answer -> {
                int statusCode = answer.getInteger("statusCode");

                if (statusCode == 200) {
                    
                    String token = jwtAuth.generateToken(
                        new JsonObject()
                            .put("userId", answer.getInteger("user_id"))
                            .put("email", answer.getString("email"))
                            .put("firstName", answer.getString("firstName"))
                            .put("lastName", answer.getString("lastName"))
                            .put("role", answer.getString("role"))
                    );

                    JsonObject response = new JsonObject()
                        .put("token", token);

                    System.out.println("[RegisterHandler] (200) registerUser succeeded for "+ answer.getString("email"));

                    ctx.response()
                        .setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(response.encode());

                    return;
                }

                System.err.println("[RegisterHandler] ("+ statusCode +") registerUser failed");

                ctx.response()
                    .setStatusCode(statusCode)
                    .end(statusCode == 403 ? "Account bereits vorhanden" : statusCode == 400 ? "Fehlende Daten" : "Interner Server Fehler");
            })
            .onFailure(err -> {
                System.err.println("[RegisterHandler] (500) registerUser failed");
                err.printStackTrace();

                if (!ctx.response().ended()) {
                    ctx.response()
                        .setStatusCode(500)
                        .end("Interner Server Fehler");
                }
            });
    }
}
