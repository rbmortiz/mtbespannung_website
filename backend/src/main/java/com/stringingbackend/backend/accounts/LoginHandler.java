package com.stringingbackend.backend.accounts;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class LoginHandler {

    private final LoginService loginService;
    private final JWTAuth jwtAuth;

    public LoginHandler(LoginService loginService, JWTAuth jwtAuth){
        this.loginService = loginService;
        this.jwtAuth = jwtAuth;
    }

    public void registerRoutes(Router router){
        router.post("/loginUser")
            .handler(this::loginUser);
    }

    private void loginUser(RoutingContext ctx) {

        loginService.loginUser(ctx)
            .onSuccess(answer -> {

                if(answer==200){

                    JsonObject obj = ctx.body().asJsonObject();

                    String email = obj.getString("email");
                    String firstName = obj.getString("firstName");
                    String lastName = obj.getString("lastName");

                    String token = jwtAuth.generateToken(
                        new JsonObject()
                            .put("email", email)
                            .put("firstName", firstName)
                            .put("lastName", lastName)
                    );

                    JsonObject response = new JsonObject()
                        .put("token", token);

                    ctx.response()
                        .setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(response.encode());

                    return;
                }

                ctx.response()
                    .setStatusCode(answer)
                    .end(
                        answer == 403 ? "Login fehlgeschlagen" :
                        answer == 400 ? "Fehlende Daten" :
                        "Interner Server Fehler"
                    );
            })
            .onFailure(err -> {
                ctx.response()
                    .setStatusCode(500)
                    .end("Interner Server Fehler");
            });
    }
}
