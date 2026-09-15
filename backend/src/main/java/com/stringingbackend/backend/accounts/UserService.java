package com.stringingbackend.backend.accounts;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class UserService {
    private final AccountRepository accountRepository;

    public UserService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    public Future<JsonObject> updateUser(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        JsonObject tokenUser = ctx.user().principal();

        if (body == null || tokenUser == null) {
            System.err.println("[UserService] (400) updateUser failed");

            return Future.succeededFuture(
                new JsonObject().put("statusCode", 400)
            );
        }

        String oldEmail = tokenUser.getString("email");

        if (oldEmail == null || oldEmail.isBlank()) {
            System.err.println("[UserService] (401) updateUser failed");

            return Future.succeededFuture(
                new JsonObject().put("statusCode", 401)
            );
        }

        String newEmail = body.getString("email");
        String newPassword = body.getString("password");
        String newFirstName = body.getString("firstName");
        String newLastName = body.getString("lastName");

        return accountRepository.updateUser(oldEmail, newEmail, newPassword, newFirstName, newLastName);
    }

    public Future<Integer> deleteUser(RoutingContext ctx) {
        JsonObject user = ctx.user().principal();
        String email = user.getString("email");

        if (email == null || email.isBlank()) {
            System.err.println("[UserService] (403) deleteUser failed");

            return Future.succeededFuture(403);
        }

        return accountRepository.deleteUser(email);
    }

    public Future<JsonArray> getAllUsers(RoutingContext ctx){
        System.out.println("[UserService] getAllUsers called");

        return accountRepository.getAllUsers()
            .compose(data -> {
                System.out.println("[UserService] (200) getAllUsers succeeded");
                return Future.succeededFuture(data);
            });
    }
}
