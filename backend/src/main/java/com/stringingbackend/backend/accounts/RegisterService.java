package com.stringingbackend.backend.accounts;

import com.stringingbackend.backend.accounts.tools.Hashing;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class RegisterService {
    
    private final AccountRepository accountRepository;

    public RegisterService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    public Future<Integer> registerUser(RoutingContext ctx){
        JsonObject body = ctx.body().asJsonObject();

        String email = body.getString("email");

        String firstName = body.getString("firstname");
        String lastName = body.getString("lastname");

        String password = body.getString("password");

        if(
            email.isBlank() || email == null ||
            firstName.isBlank() || firstName == null ||
            lastName.isBlank() || lastName == null ||
            password.isBlank() || password == null) return Future.succeededFuture(401);

        if(!accountRepository.isAccountFree(email, firstName, lastName).await()) return Future.succeededFuture(403);

        String hashedPassword = Hashing.hashPassword(password);

        Integer answer = accountRepository.register(email, firstName, lastName, hashedPassword).await();

        return Future.succeededFuture(answer);
    }
}
