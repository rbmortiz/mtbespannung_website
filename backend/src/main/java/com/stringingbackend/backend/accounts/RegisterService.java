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

    public Future<Integer> registerUser(RoutingContext ctx) {

        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            return Future.succeededFuture(400);
        }

        String email = body.getString("email");
        String firstName = body.getString("firstname");
        String lastName = body.getString("lastname");
        String password = body.getString("password");

        if (
            email == null || email.isBlank() ||
            firstName == null || firstName.isBlank() ||
            lastName == null || lastName.isBlank() ||
            password == null || password.isBlank()
        ) {
            return Future.succeededFuture(400);
        }

        return accountRepository
            .isAccountFree(email)
            .compose(isFree -> {

                if (!isFree) {
                    return Future.succeededFuture(403);
                }

                String hashedPassword =
                    Hashing.hashPassword(password);

                return accountRepository.register(
                    email,
                    firstName,
                    lastName,
                    hashedPassword
                );
            });
    }
}