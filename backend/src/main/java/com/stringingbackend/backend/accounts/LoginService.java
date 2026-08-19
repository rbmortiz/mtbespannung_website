package com.stringingbackend.backend.accounts;

import com.stringingbackend.backend.accounts.tools.Hashing;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class LoginService {
    
    private final AccountRepository accountRepository;

    public LoginService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    public Future<Integer> loginUser(RoutingContext ctx){
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

        String dbPassword = accountRepository.getPassword(email, firstName, lastName).await();

        if(dbPassword.isBlank() || dbPassword == null) return Future.succeededFuture(500);

        if(Hashing.verifyPassword(password, dbPassword)){
            return Future.succeededFuture(200);
        }
        else if(!Hashing.verifyPassword(password, dbPassword)) return Future.succeededFuture(403);

        return Future.succeededFuture(500);
    }
}
