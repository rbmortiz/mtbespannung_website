package com.stringingbackend.backend.accounts;

import com.stringingbackend.backend.accounts.tools.Hashing;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class LoginService {

    private final AccountRepository accountRepository;

    public LoginService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Future<JsonObject> loginUser(RoutingContext ctx) {
        System.out.println("[LoginService] loginUser called");

        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            return Future.failedFuture("Missing body");
        }

        String email = body.getString("email");
        String password = body.getString("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            System.err.println("[LoginService] loginUser (No Credentials found)");
            return Future.failedFuture("Missing credentials");
        }

        return accountRepository.getUser(email)
            .compose(user -> {
                if (user == null) {
                    System.err.println("[LoginService] loginUser (Invalid Credentials)");
                    return Future.failedFuture("Invalid credentials");
                }

                String hashedPassword = user.getString("hashedPassword");

                boolean valid = Hashing.verifyPassword(password, hashedPassword);

                if (!valid) {
                    System.err.println("[LoginService] loginUser (No Credentials found)");
                    return Future.failedFuture("Invalid credentials");
                }

                user.remove("hashedPassword");

                System.out.println("[LoginService] loginUser succeeded for "+ email);
                return Future.succeededFuture(user);
            });
    }
}