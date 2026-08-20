package com.stringingbackend.backend.accounts;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

public class AccountRepository {

    private final Pool pool;

    public AccountRepository(Pool pool) {
        this.pool = pool;
    }

    public Future<Boolean> isAccountFree(String email) {

        String query = """
            SELECT user_id
            FROM users
            WHERE email = $1
            """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(email))
            .map(rows -> rows.size() == 0);
    }

    public Future<Integer> register(String email, String firstName, String lastName, String password) {

        String query = """
            INSERT INTO users(
                firstname,
                lastname,
                email,
                hashed_password,
            )
            VALUES ($1, $2, $3, $4)
            """;

        return pool.preparedQuery(query)
            .execute(
                Tuple.of(
                    firstName,
                    lastName,
                    email,
                    password
                )
            )
            .map(result -> 200)
            .recover(err -> {
                err.printStackTrace();
                return Future.succeededFuture(500);
            });
    }

    public Future<JsonObject> getUserByEmail(String email) {

        String query = """
            SELECT
                user_id,
                firstname,
                lastname,
                email,
                hashed_password,
                is_admin
            FROM users
            WHERE email = $1
            """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(email))
            .compose(rows -> {

                if (!rows.iterator().hasNext()) {
                    return Future.succeededFuture(null);
                }

                Row row = rows.iterator().next();

                JsonObject user = new JsonObject()
                    .put("userId", row.getInteger("user_id"))
                    .put("firstName", row.getString("firstname"))
                    .put("lastName", row.getString("lastname"))
                    .put("email", row.getString("email"))
                    .put(
                        "hashedPassword",
                        row.getString("hashed_password")
                    )
                    .put("isAdmin", row.getBoolean("is_admin"));

                return Future.succeededFuture(user);
            });
    }
}