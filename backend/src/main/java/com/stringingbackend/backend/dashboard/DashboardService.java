package com.stringingbackend.backend.dashboard;

import com.stringingbackend.backend.accounts.AccountRepository;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class DashboardService {
    private final AccountRepository accountRepository;

    public DashboardService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
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

        String newEmail = body.getString("email");
        String newPassword = body.getString("password");
        String newFirstName = body.getString("firstName");
        String newLastName = body.getString("lastName");

        if (
            oldEmail == null || oldEmail.isBlank() ||
            newEmail == null || newEmail.isBlank() ||
            newFirstName == null || newFirstName.isBlank() ||
            newLastName == null || newLastName.isBlank()
        ) {
            return Future.succeededFuture(
                new JsonObject().put("statusCode", 400)
            );
        }

        return accountRepository.updateUser(
            oldEmail,
            newEmail,
            newPassword,
            newFirstName,
            newLastName
        );
    }

    public Future<Integer> deleteUser(RoutingContext ctx){
        JsonObject body = ctx.user().principal();

        String email = body.getString("email");

        return accountRepository.deleteUser(email);
    }
}
