package com.stringingbackend.backend.accounts;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Router;

public class RegisterHandler {
    
    private final RegisterService registerService;

    public RegisterHandler(RegisterService registerService){
        this.registerService = registerService;
    }

    public void registerRoutes(Router router){
        router.post("/register")
            .handler(this::registerUser);
    }

    private void registerUser(RoutingContext ctx) {
        registerService.registerUser(ctx)
            .onSuccess(answer -> {
                ctx.response()
                    .setStatusCode(answer)
                    .end(
                        answer == 200 ? "Account wurde erstellt" :
                        answer == 403 ? "Account bereits vorhanden" :
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
