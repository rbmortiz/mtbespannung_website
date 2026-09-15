package com.stringingbackend.backend.accounts;

import com.stringingbackend.backend.accounts.tools.Hashing;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class RegisterService {

    private final AccountRepository accountRepository;

    public RegisterService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Future<JsonObject> registerUser(RoutingContext ctx) {
        System.out.println("[RegisterService] registerUser called");

        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            System.err.println("[RegisterService] (400) registerUser failed");
            return Future.succeededFuture(new JsonObject().put("statusCode", 400));
        }

        String email = body.getString("email");
        String firstName = body.getString("firstname");
        String lastName = body.getString("lastname");
        String password = body.getString("password");

        if (email == null || email.isBlank() || firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank() || password == null || password.isBlank()) {
            System.err.println("[RegisterService] (400) registerUser failed");
            return Future.succeededFuture(new JsonObject().put("statusCode", 400));
        }

        return accountRepository.isAccountFree(email)
            .compose(isFree -> {
                if (!isFree) {
                    System.err.println("[RegisterService] (403) registerUser failed");
                    return Future.succeededFuture(new JsonObject().put("statusCode", 403));
                }
                
                String hashedPassword = Hashing.hashPassword(password);

                return accountRepository.registerUser(email, firstName, lastName, hashedPassword);
            })
            .recover(err -> {
                System.err.println("[RegisterService] (500) registerUser failed");
                err.printStackTrace();

                return Future.succeededFuture(new JsonObject().put("statusCode", 500));
            });
    }
}