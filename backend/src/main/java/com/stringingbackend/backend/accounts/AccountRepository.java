package com.stringingbackend.backend.accounts;

import com.stringingbackend.backend.accounts.tools.Hashing;

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

        System.out.println("Checking if isAccountFree" + email);

        String query = """
            SELECT user_id
            FROM users
            WHERE email = $1
            """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(email))
            .map(rows -> rows.size() == 0);
    }

    public Future<JsonObject> register(String email, String firstName, String lastName, String password) {

        System.out.println("AccountRepository.register called");

        String query = """
            INSERT INTO users (
                firstname,
                lastname,
                email,
                hashed_password
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
            .map(result -> {

                System.out.println("INSERT successful");

                return new JsonObject()
                    .put("statusCode", 200)
                    .put("email", email)
                    .put("firstName", firstName)
                    .put("lastName", lastName)
                    .put("role", "user");
            })
            .recover(err -> {

                System.out.println("INSERT failed");
                err.printStackTrace();

                return Future.succeededFuture(
                    new JsonObject()
                        .put("statusCode", 500)
                        .put("email", email)
                        .put("firstName", firstName)
                        .put("lastName", lastName)
                        .put("role", "user")
                );
            });
    }

    public Future<JsonObject> getUser(String email) {

        String query = """
            SELECT
                user_id,
                firstname,
                lastname,
                email,
                hashed_password,
                role
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
                    .put("role", row.getString("role"));

                return Future.succeededFuture(user);
            });
    }

    public Future<JsonObject> updateUser(String oldEmail, String newEmail, String newPassword, String newFirstName, String newLastName) {

        boolean changePassword =
            newPassword != null && !newPassword.isBlank();

        Future<Boolean> emailCheck;

        if (oldEmail.equalsIgnoreCase(newEmail)) {
            emailCheck = Future.succeededFuture(true);
        } else {
            String emailQuery = """
                SELECT user_id
                FROM users
                WHERE email = $1
                """;

            emailCheck = pool.preparedQuery(emailQuery)
                .execute(Tuple.of(newEmail))
                .map(rows -> rows.size() == 0);
        }

        return emailCheck.compose(emailFree -> {

            if (!emailFree) {
                return Future.succeededFuture(
                    new JsonObject()
                        .put("statusCode", 409)
                );
            }

            String query;
            Tuple values;

            if (changePassword) {

                String hashedPassword =
                    Hashing.hashPassword(newPassword);

                query = """
                    UPDATE users
                    SET email = $1,
                        firstname = $2,
                        lastname = $3,
                        hashed_password = $4
                    WHERE email = $5
                    RETURNING
                        user_id,
                        email,
                        firstname,
                        lastname,
                        role
                    """;

                values = Tuple.of(
                    newEmail,
                    newFirstName,
                    newLastName,
                    hashedPassword,
                    oldEmail
                );

            } else {

                query = """
                    UPDATE users
                    SET email = $1,
                        firstname = $2,
                        lastname = $3
                    WHERE email = $4
                    RETURNING
                        user_id,
                        email,
                        firstname,
                        lastname,
                        role
                    """;

                values = Tuple.of(
                    newEmail,
                    newFirstName,
                    newLastName,
                    oldEmail
                );
            }

            return pool.preparedQuery(query)
                .execute(values)
                .map(rows -> {

                    if (!rows.iterator().hasNext()) {
                        return new JsonObject()
                            .put("statusCode", 404);
                    }

                    Row row = rows.iterator().next();

                    return new JsonObject()
                        .put("statusCode", 200)
                        .put("userId", row.getInteger("user_id"))
                        .put("email", row.getString("email"))
                        .put("firstName", row.getString("firstname"))
                        .put("lastName", row.getString("lastname"))
                        .put("role", row.getString("role"));
                });
        })
        .recover(err -> {

            System.err.println("Updating user failed:");
            err.printStackTrace();

            return Future.succeededFuture(
                new JsonObject()
                    .put("statusCode", 500)
            );
        });
    }

    public Future<JsonObject> checkEmailAvailable(String oldEmail, String newEmail, String newFirstName, String newLastName) {

        if (oldEmail == null || oldEmail.isBlank()) {
            return Future.succeededFuture(
                new JsonObject()
                    .put("statusCode", 401)
            );
        }

        if (newEmail == null || newEmail.isBlank()) {
            return Future.succeededFuture(
                new JsonObject()
                    .put("statusCode", 400)
            );
        }

        String query = """
            SELECT user_id
            FROM users
            WHERE email = $1
            AND email <> $2
            """;

        return pool.preparedQuery(query)
            .execute(
                Tuple.of(
                    newEmail,
                    oldEmail
                )
            )
            .map(rows -> {

                // Another account already uses the new email
                if (rows.iterator().hasNext()) {
                    return new JsonObject()
                        .put("statusCode", 401);
                }

                // Email is available
                return new JsonObject()
                    .put("statusCode", 200)
                    .put("email", newEmail)
                    .put("firstName", newFirstName)
                    .put("lastName", newLastName);
            })
            .recover(err -> {

                System.err.println("Checking email availability failed:");
                err.printStackTrace();

                return Future.succeededFuture(
                    new JsonObject()
                        .put("statusCode", 500)
                );
            });
    }

    public Future<Integer> deleteUser(String email){
        if(email.isBlank() || email == null) return Future.succeededFuture(403);

        String query = """
                    DELETE FROM users WHERE email = ?;
                """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(email))
            .map(result -> {

                if (result.rowCount() == 0) {
                    return 401;
                }

                return 200;
            })
            .recover(err -> {

                System.err.println("Deleting user failed:");
                err.printStackTrace();

                return Future.succeededFuture(500);
            });
    }
}