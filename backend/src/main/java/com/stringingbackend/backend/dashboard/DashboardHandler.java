package com.stringingbackend.backend.dashboard;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class DashboardHandler {
    private final DashboardService dashboardService;
    private final JWTAuth jwtAuth;

    public DashboardHandler(DashboardService dashboardService, JWTAuth jwtAuth){
        this.dashboardService = dashboardService;
        this.jwtAuth = jwtAuth;
    }

    public void registerRoutes(Router router){
        router.patch("/editUser")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::editUser);
        router.delete("/deleteUser")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::deleteUser);
        router.get("/getStrings")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::getStrings);
        router.post("/getUserStringingJobs")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::getStringingJobs);
    }

    private void editUser(RoutingContext ctx) {

        dashboardService.updateUser(ctx)
            .onSuccess(user -> {

                Integer statusCode = user.getInteger("statusCode");

                if (statusCode == null) {
                    ctx.response()
                        .setStatusCode(500)
                        .end("Interner Server Fehler");
                    return;
                }

                // Update successful
                if (statusCode == 200) {

                    String token = jwtAuth.generateToken(
                        new JsonObject()
                            .put("userId", user.getInteger("userId"))
                            .put("email", user.getString("email"))
                            .put("firstName", user.getString("firstName"))
                            .put("lastName", user.getString("lastName"))
                            .put("role", user.getString("role"))
                    );

                    JsonObject response = new JsonObject()
                        .put("token", token);

                    ctx.response()
                        .setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(response.encode());

                    return;
                }

                // Invalid / missing data
                if (statusCode == 400) {
                    ctx.response()
                        .setStatusCode(400)
                        .end("Fehlende oder ungültige Daten");
                    return;
                }

                // User from JWT doesn't exist anymore
                if (statusCode == 404) {
                    ctx.response()
                        .setStatusCode(404)
                        .end("Benutzer wurde nicht gefunden");
                    return;
                }

                // Email already exists
                if (statusCode == 409) {
                    ctx.response()
                        .setStatusCode(409)
                        .end("E-Mail-Adresse ist bereits vergeben");
                    return;
                }

                // Everything else
                ctx.response()
                    .setStatusCode(500)
                    .end("Interner Server Fehler");
            })
            .onFailure(err -> {

                System.err.println("Dashboard editUser failed:");
                err.printStackTrace();

                if (!ctx.response().ended()) {
                    ctx.response()
                        .setStatusCode(500)
                        .end("Interner Server Fehler");
                }
            });
    }

    private void deleteUser(RoutingContext ctx) {

        dashboardService.deleteUser(ctx)
            .onSuccess(statusCode -> {

                ctx.response()
                    .setStatusCode(statusCode)
                    .end();
            })
            .onFailure(err -> {

                System.err.println("Deleting account failed:");
                err.printStackTrace();

                if (!ctx.response().ended()) {
                    ctx.response()
                        .setStatusCode(500)
                        .end("Interner Server Fehler");
                }
            });
    }

    private void getStrings(RoutingContext ctx){
        dashboardService.getStrings(ctx)
            .onSuccess(response -> {
                if(response == null){
                    ctx.response()
                        .setStatusCode(500)
                        .end("Database could not be reached");
                    return;
                }
                else if(response.isEmpty()){
                    ctx.response()
                        .setStatusCode(304)
                        .end("No Strings could be found");
                    return;
                }

                ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(response.encode());
            })
            .onFailure(err -> {
                ctx.response()
                    .setStatusCode(500)
                    .end("No connection to Database");
            });
    }

    private void getStringingJobs(RoutingContext ctx){
        dashboardService.getStringingJobs(ctx)
            .onSuccess(response -> {
                if(response.isEmpty()){
                    ctx.response()
                        .setStatusCode(304)
                        .end("No Stringing Orders could be found");
                    return;
                }

                ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(response.encode());
            })
            .onFailure(err -> {
                ctx.response()
                    .setStatusCode(500)
                    .end("No connection to Database");
            });
    }
}
