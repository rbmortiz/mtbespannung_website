package com.stringingbackend.backend.dashboard;

import com.stringingbackend.backend.Stringing.StringingRepository;
import com.stringingbackend.backend.accounts.AccountRepository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class DashboardService {
    private final AccountRepository accountRepository;
    private final StringingRepository stringingRepository;

    public DashboardService(AccountRepository accountRepository, StringingRepository stringingRepository){
        this.accountRepository = accountRepository;
        this.stringingRepository = stringingRepository;
    }

    public Future<JsonObject> updateUser(RoutingContext ctx) {

        JsonObject body = ctx.body().asJsonObject();
        JsonObject tokenUser = ctx.user().principal();

        if (body == null || tokenUser == null) {
            return Future.succeededFuture(
                new JsonObject().put("statusCode", 400)
            );
        }

        String oldEmail = tokenUser.getString("email");

        if (oldEmail == null || oldEmail.isBlank()) {
            return Future.succeededFuture(
                new JsonObject().put("statusCode", 401)
            );
        }

        String newEmail = body.getString("email");
        String newPassword = body.getString("password");
        String newFirstName = body.getString("firstName");
        String newLastName = body.getString("lastName");

        return accountRepository.updateUser(
            oldEmail,
            newEmail,
            newPassword,
            newFirstName,
            newLastName
        );
    }

    public Future<Integer> deleteUser(RoutingContext ctx) {

        JsonObject user = ctx.user().principal();

        String email = user.getString("email");

        if (email == null || email.isBlank()) {
            return Future.succeededFuture(403);
        }

        return accountRepository.deleteUser(email);
    }

    public Future<JsonArray> getStrings(RoutingContext ctx){
        return stringingRepository.getStrings();
    }

    public Future<JsonArray> getStringingJobs(RoutingContext ctx) {

        JsonObject tokenUser = ctx.user().principal();
        String email = tokenUser.getString("email");

        if (email == null || email.isBlank()) {
            return Future.failedFuture("Email missing from JWT");
        }

        return accountRepository.getUser(email)
            .compose(user -> {

                if (user == null) {
                    return Future.succeededFuture(new JsonArray());
                }

                Integer id = user.getInteger("user_id");

                if (id == null) {
                    return Future.failedFuture("User ID missing");
                }

                return stringingRepository.getStringingJobs(id);
            });
    }

    public Future<Integer> updateOrder(RoutingContext ctx) {

        JsonObject body = ctx.body().asJsonObject();

        Integer orderId = body.getInteger("order_id");
        Integer kgVert = body.getInteger("kgVert");
        Integer kgHor = body.getInteger("kgHor");
        String infos = body.getString("infos");

        return stringingRepository.isValidOrder(orderId)
            .compose(response -> {

                if (response != 200) {
                    return Future.succeededFuture(response);
                }

                return stringingRepository.orderIsModifiable(orderId)
                    .compose(res -> {

                        if (res != 200) {
                            return Future.succeededFuture(res);
                        }

                        return stringingRepository.updateOrder(
                            orderId,
                            kgVert,
                            kgHor,
                            infos
                        );
                    });
            });
    }
}
