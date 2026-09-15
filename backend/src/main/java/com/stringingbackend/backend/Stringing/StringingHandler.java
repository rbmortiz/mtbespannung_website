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
        router.post("/getUserStringingOrders")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::getStringingOrders);
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
        router.get("/getAllStringingOrders")
            .handler(JWTAuthHandler.create(jwtAuth))
            .handler(this::getAllStringingOrders);

        // TODO: Auftrag neu schicken, alten Auftrag übernehmen
    }

    private void getAllStringingOrders(RoutingContext ctx){
        System.out.println("[StringingHandler] getAllStringingOrders called");

        if(ctx.user().principal().getString("role").equals("admin")){
            stringingService.getAllStringingOrders(ctx)
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
                System.err.println("[StringingHandler] (500) getAllStringingOrders failed");

                err.printStackTrace();
                ctx.response()
                    .setStatusCode(500)
                    .end(err.getLocalizedMessage());
            });
            return;
        }

        System.out.println("[StringingHandler] (403) getAllStringingOrders failed");

        ctx.response()
            .setStatusCode(403)
            .end("User is not permitted");
    }

    private void newStringingOrderAccount(RoutingContext ctx){
        System.out.println("[StringingHandler] newStringingOrderAccount called");

        stringingService.newStringingOrderAccount(ctx)
            .onSuccess(response -> {
                System.out.println("[StringingHandler] (200) newStringingOrderAccount succeeded");

                ctx.response()
                    .setStatusCode(response)
                    .end();
            })
            .onFailure(err -> {
                System.err.println("[StringingHandler] (500) newStringingOrderAccount failed");
                err.printStackTrace();

                ctx.response()
                    .setStatusCode(500)
                    .end("Database error");
            });
    }

    private void newStringingOrder(RoutingContext ctx){
        System.out.println("[StringingHandler] newStringingOrder called");

        stringingService.newStringingOrder(ctx)
            .onSuccess(response -> {
                System.out.println("[StringingHandler] (200) newStringingOrder succeeded");

                ctx.response()
                    .setStatusCode(response)
                    .end();
            })
            .onFailure(err -> {
                System.err.println("[StringingHandler] (500) newStringingOrder failed");
                err.printStackTrace();

                ctx.response()
                    .setStatusCode(500)
                    .end("Database error");
            });
    }

    private void deleteStringingOrder(RoutingContext ctx){
        System.out.println("[StringingHandler] deleteStringingOrder called");

        stringingService.deleteStringingOrder(ctx)
            .onSuccess(response -> {
                System.out.println("[StringingHandler] (200) deleteStringingOrder succeeded");

                ctx.response()
                    .setStatusCode(response)
                    .end();
            })
            .onFailure(err -> {
                System.err.println("[StringingHandler] (500) deleteStringingOrder failed");
                err.printStackTrace();

                ctx.response()
                    .setStatusCode(500)
                    .end("Database error");
            });
    }

    private void getStrings(RoutingContext ctx){
        System.out.println("[StringingHandler] getStrings called");

        stringingService.getStrings(ctx)
            .onSuccess(response -> {
                if(response == null){
                    System.err.println("[StringingHandler] (500) getStrings failed");

                    ctx.response()
                        .setStatusCode(500)
                        .end("Database could not be reached");
                    return;
                }
                else if(response.isEmpty()){
                    System.out.println("[StringingHandler] (304) getStrings failed");

                    ctx.response()
                        .setStatusCode(304)
                        .end("No Strings could be found");
                    return;
                }

                System.out.println("[StringingHandler] (200) getStrings succeeded");

                ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(response.encode());
            })
            .onFailure(err -> {
                System.err.println("[StringingHandler] (500) getStrings failed");
                err.printStackTrace();

                ctx.response()
                    .setStatusCode(500)
                    .end("No connection to Database");
            });
    }

    private void getStringingOrders(RoutingContext ctx){
        System.out.println("[StringingHandler] getStringingOrders called");

        stringingService.getStringingOrders(ctx)
            .onSuccess(response -> {
                if(response.isEmpty()){
                    System.out.println("[StringingHandler] (304) getStringingOrders failed");

                    ctx.response()
                        .setStatusCode(304)
                        .end("No Stringing Orders could be found");
                    return;
                }

                System.out.println("[StringingHandler] (200) getStringingOrders succeeded");

                ctx.response()
                    .setStatusCode(200)
                    .putHeader("Content-Type", "application/json")
                    .end(response.encode());
            })
            .onFailure(err -> {
                System.err.println("[StringingHandler] (500) getStringingOrders failed");
                err.printStackTrace();

                ctx.response()
                    .setStatusCode(500)
                    .end("Interner Server Fehler");
            });
    }

    private void updateOrder(RoutingContext ctx) {
        System.out.println("[StringingHandler] updateOrder called");

        stringingService.updateOrder(ctx)
            .onSuccess(statusCode -> {
                if (statusCode == 400) {
                    System.out.println("[StringingHandler] (400) updateOrder failed");

                    ctx.response()
                        .setStatusCode(400)
                        .end("No valid fields to update");
                    return;
                }

                if (statusCode == 403) {
                    System.out.println("[StringingHandler] (403) updateOrder failed");

                    ctx.response()
                        .setStatusCode(403)
                        .end("Order can not be modified anymore");
                    return;
                }

                if (statusCode == 404) {
                    System.out.println("[StringingHandler] (404) updateOrder failed");

                    ctx.response()
                        .setStatusCode(404)
                        .end("Order not found");
                    return;
                }

                System.out.println("[StringingHandler] (200) updateOrder succeeded");

                ctx.response()
                    .setStatusCode(200)
                    .end("Order updated");
            })
            .onFailure(err -> {
                System.err.println("[StringingHandler] (500) updateOrder failed");
                err.printStackTrace();

                if (!ctx.response().ended()) {
                    ctx.response()
                        .setStatusCode(500)
                        .end("Database error");
                }
            });
    }
}
