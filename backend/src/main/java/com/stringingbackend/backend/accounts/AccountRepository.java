package com.stringingbackend.backend.accounts;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import io.vertx.core.Future;

public class AccountRepository {

    private final Pool pool;

    public AccountRepository(Pool pool){

        this.pool = pool;

    }

    public Future<Boolean> isAccountFree(String email, String firstName, String lastName) {

        String query = """
            SELECT user_id
            FROM users
            WHERE firstName = $1
            AND lastName = $2
            AND email = $3
            """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(firstName, lastName, email))
            .map(rows -> rows.size() == 0)
            .recover(err -> Future.succeededFuture(false));
    }

    public Future<Integer> register(String email, String firstName, String lastName, String password){  

        String query = 
            """
                INSERT INTO users(firstname, lastname, email, hashedPassword) VALUES($1, $2, $3, $4)
            """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(firstName, lastName, email, password))
            .map(result -> 200)
            .recover(err -> Future.succeededFuture(500));
    }

    public Future<String> getPassword(String email, String firstName, String lastName){  

        String query = 
            """
                SELECT hashedPassword FROM users WHERE email=$1 AND firstname=$2 AND lastname=$3
            """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(email, firstName, lastName))
            .compose(rows -> {

                if (rows.size() == 0) {
                    return Future.succeededFuture("");
                }

                Row row = rows.iterator().next();
                String hashedPassword = row.getString("hashedpassword");

                return Future.succeededFuture(hashedPassword);
            });
    }
}
