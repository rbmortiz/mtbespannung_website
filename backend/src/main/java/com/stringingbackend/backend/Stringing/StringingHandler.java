package com.stringingbackend.backend.Stringing;

import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;

public class StringingHandler {
    private final StringingService stringingService;
    private final JWTAuth jwtAuth;

    public StringingHandler(StringingService stringingService, JWTAuth jwtAuth){
        this.stringingService = stringingService;
        this.jwtAuth = jwtAuth;
    }

    public void registerRoutes(Router router){
        router.get("/getStrings")
            .handler(this::getStrings);
        router.post("/getUserStringingJobs")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::getStringingJobs);
        router.patch("/updateOrder")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::updateOrder);
        router.post("/newStringingOrderAccount")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::newStringingOrderAccount);
        router.post("/newStringingOrder")
            .handler(this::newStringingOrder);
        router.delete("/deleteStringingOrder")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::deleteStringingOrder);
        router.get("/getAdminDashboard")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::getAdminStringingOrders);
        router.get("/getAdminUsers")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::getAdminUsers);
    }

    private void getAdminStringingOrders(RoutingContext ctx){
        if(ctx.user().principal().getString("role").equals("admin")){
            stringingService.getAdminStringingOrders(ctx)
            .onSuccess(obj -> {
                if(obj==null || obj.isEmpty()){
                    ctx.response()
                        .setStatusCode(404)
                        .end("No stringing orders have been found");
                }
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

        ctx.response()
            .setStatusCode(403)
            .end("User is not permitted");
    }

    private void getAdminUsers(RoutingContext ctx){
        if(ctx.user().principal().getString("role").equals("admin")){
            stringingService.getAdminUsers(ctx)
            .onSuccess(obj -> {
                if(obj==null || obj.isEmpty()){
                    ctx.response()
                        .setStatusCode(404)
                        .end("No Users have been found");
                }
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

        ctx.response()
            .setStatusCode(403)
            .end("User is not permitted");
    }

    private void newStringingOrderAccount(RoutingContext ctx){
        stringingService.newStringingOrderAccount(ctx)
            .onSuccess(response -> {
                ctx.response()
                    .setStatusCode(response)
                    .end();
            })
            .onFailure(err -> {
                err.printStackTrace();

                ctx.response()
                    .setStatusCode(500)
                    .end("Database error");
            });
    }

    private void newStringingOrder(RoutingContext ctx){
        stringingService.newStringingOrder(ctx)
            .onSuccess(response -> {
                ctx.response()
                    .setStatusCode(response)
                    .end();
            })
            .onFailure(err -> {
                err.printStackTrace();

                ctx.response()
                    .setStatusCode(500)
                    .end("Database error");
            });
    }

    private void deleteStringingOrder(RoutingContext ctx){
        stringingService.deleteStringingOrder(ctx)
            .onSuccess(response -> {
                ctx.response()
                    .setStatusCode(response)
                    .end();
            })
            .onFailure(err -> {
                err.printStackTrace();

                ctx.response()
                    .setStatusCode(500)
                    .end("Database error");
            });
    }

    private void getStrings(RoutingContext ctx){
        stringingService.getStrings(ctx)
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
        stringingService.getStringingJobs(ctx)
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
                System.err.println("Getting stringing jobs failed:");
                err.printStackTrace();

                ctx.response()
                    .setStatusCode(500)
                    .end("Interner Server Fehler");
            });
    }

    private void updateOrder(RoutingContext ctx) {
        stringingService.updateOrder(ctx)
            .onSuccess(statusCode -> {
                if (statusCode == 400) {
                    ctx.response()
                        .setStatusCode(400)
                        .end("No valid fields to update");
                    return;
                }

                if (statusCode == 403) {
                    ctx.response()
                        .setStatusCode(403)
                        .end("Order can not be modified anymore");
                    return;
                }

                if (statusCode == 404) {
                    ctx.response()
                        .setStatusCode(404)
                        .end("Order not found");
                    return;
                }

                ctx.response()
                    .setStatusCode(200)
                    .end("Order updated");
            })
            .onFailure(err -> {

                err.printStackTrace();

                if (!ctx.response().ended()) {
                    ctx.response()
                        .setStatusCode(500)
                        .end("Database error");
                }
            });
    }
}
