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

        System.out.println("registerUser called");

        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            System.out.println("body is null");
            return Future.succeededFuture(400);
        }

        String email = body.getString("email");
        String firstName = body.getString("firstname");
        String lastName = body.getString("lastname");
        String password = body.getString("password");

        System.out.println("email = " + email);
        System.out.println("firstname = " + firstName);
        System.out.println("lastname = " + lastName);

        if (
            email == null || email.isBlank() ||
            firstName == null || firstName.isBlank() ||
            lastName == null || lastName.isBlank() ||
            password == null || password.isBlank()
        ) {
            System.out.println("missing data");
            return Future.succeededFuture(400);
        }

        return accountRepository.isAccountFree(email)
            .compose(isFree -> {

                System.out.println("isAccountFree = " + isFree);

                if (!isFree) {
                    return Future.succeededFuture(403);
                }

                System.out.println("Before hashing");

                String hashedPassword = Hashing.hashPassword(password);

                System.out.println("After hashing");

                System.out.println("calling register");

                return accountRepository.register(
                    email,
                    firstName,
                    lastName,
                    hashedPassword
                );
            })
            .onFailure(err -> {
                System.err.println("RegisterService failed:");
                err.printStackTrace();
            });
    }
}