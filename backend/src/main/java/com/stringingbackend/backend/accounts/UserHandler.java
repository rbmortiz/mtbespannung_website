package com.stringingbackend.backend.accounts;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class UserHandler {
    private final UserService userService;
    private final JWTAuth jwtAuth;

    public UserHandler(UserService userService, JWTAuth jwtAuth){
        this.userService = userService;
        this.jwtAuth = jwtAuth;
    }

    public void registerRoutes(Router router){
        router.patch("/editUser")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::editUser);
        router.delete("/deleteUser")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::deleteUser);
        router.get("/getUserInformation")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::getUserInformation);
        router.get("/getAllUsers")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::getAllUsers);
    }

    private void getAllUsers(RoutingContext ctx){
        System.out.println("[UserHandler] getAllUsers called");

        if(ctx.user().principal().getString("role").equals("admin")){
            userService.getAllUsers(ctx)
                .onSuccess(obj -> {
                    if(obj==null || obj.isEmpty()){
                        System.err.println("[UserHandler] (404) getAllUsers failed");

                        ctx.response()
                            .setStatusCode(404)
                            .end("No Users have been found");
                    }

                    System.out.println("[UserHandler] (200) getAllUsers succeeded");

                    ctx.response()
                        .setStatusCode(200)
                        .end(obj.encode());
                })
                .onFailure(err -> {
                    err.printStackTrace();
                    ctx.response()
                        .setStatusCode(500)
                        .end(err.getLocalizedMessage());
                });
            return;
        }

        System.err.println("[UserHandler] (403) getAllUsers failed");

        ctx.response()
            .setStatusCode(403)
            .end("User is not permitted");
    }

    private void editUser(RoutingContext ctx) {
        System.out.println("[UserHandler] editUser called");

        userService.updateUser(ctx)
            .onSuccess(user -> {
                Integer statusCode = user.getInteger("statusCode");

                if (statusCode == 200) {

                    String token = jwtAuth.generateToken(
                        new JsonObject()
                            .put("userId", user.getInteger("userId"))
                            .put("email", user.getString("email"))
                            .put("firstName", user.getString("firstName"))
                            .put("lastName", user.getString("lastName"))
                            .put("role", user.getString("role"))
                    );

                    JsonObject response = new JsonObject().put("token", token);
                    System.out.println("[UserHandler] (200) editUser succeeded");

                    ctx.response()
                        .setStatusCode(200)
                        .putHeader("Content-Type", "application/json")
                        .end(response.encode());

                    return;
                }

                if (statusCode == 400) {
                    System.err.println("[UserHandler] (400) editUser failed");

                    ctx.response()
                        .setStatusCode(400)
                        .end("Fehlende oder ungültige Daten");
                    return;
                }

                if (statusCode == 404) {
                    System.err.println("[UserHandler] (404) editUser failed");

                    ctx.response()
                        .setStatusCode(404)
                        .end("Benutzer wurde nicht gefunden");
                    return;
                }

                if (statusCode == 409) {
                    System.err.println("[UserHandler] (409) editUser failed");

                    ctx.response()
                        .setStatusCode(409)
                        .end("E-Mail-Adresse ist bereits vergeben");
                    return;
                }
                System.err.println("[UserHandler] (500) editUser failed");

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
        System.out.println("[UserHandler] deleteUser called");

        userService.deleteUser(ctx)
            .onSuccess(statusCode -> {
                if(statusCode == 200){
                    System.out.println("[UserHandler] (200) deleteUser succeeded");
                }
                else System.err.println("[UserHandler] ("+statusCode+") deleteUser failed");

                ctx.response()
                    .setStatusCode(statusCode)
                    .end();
            })
            .onFailure(err -> {
                System.err.println("[UserHandler] (500) deleteUser failed");
                err.printStackTrace();

                if (!ctx.response().ended()) {
                    ctx.response()
                        .setStatusCode(500)
                        .end("Interner Server Fehler");
                }
            });
    }

    private void getUserInformation(RoutingContext ctx) {
        JsonObject obj = ctx.user().principal();

        String firstName = obj.getString("firstName");
        String lastName = obj.getString("lastName");
        String email = obj.getString("email");
        String role = obj.getString("role");

        if(firstName == null || firstName.isEmpty() || lastName == null || lastName.isEmpty() || email == null || email.isEmpty() || role == null || role.isEmpty()){
            System.err.println("[UserHandler] (400) getUserInformation failed");

            ctx.response()
                .setStatusCode(400)
                .putHeader("Content-Type", "application/json")
                .end();
            return;
        }

        System.out.println("[UserHandler] (200) getUserInformation succeeded");

        ctx.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "application/json")
            .end(new JsonObject()
                    .put("firstName", firstName)
                    .put("lastName", lastName)
                    .put("email", email)
                    .put("role", role)
                        .encode());
    }
}
