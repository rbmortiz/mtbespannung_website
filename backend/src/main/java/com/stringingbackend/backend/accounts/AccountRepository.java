package com.stringingbackend.backend.accounts;

import java.util.ArrayList;
import java.util.List;

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
                first_name,
                last_name,
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
                first_name,
                last_name,
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
                    .put("user_id", row.getInteger("user_id"))
                    .put("firstName", row.getString("first_name"))
                    .put("lastName", row.getString("last_name"))
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

        // Only check uniqueness if the email is actually being changed
        Future<Boolean> emailCheck;

        if (newEmail != null && !newEmail.isBlank() && !newEmail.equalsIgnoreCase(oldEmail)) {

            String emailQuery = """
                SELECT user_id
                FROM users
                WHERE email = $1
                """;

            emailCheck = pool.preparedQuery(emailQuery)
                .execute(Tuple.of(newEmail))
                .map(rows -> rows.size() == 0);

        } else {
            emailCheck = Future.succeededFuture(true);
        }

        return emailCheck.compose(emailFree -> {

            if (!emailFree) {
                return Future.succeededFuture(
                    new JsonObject()
                        .put("statusCode", 409)
                );
            }

            List<String> updates = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            int index = 1;

            if (newEmail != null && !newEmail.isBlank()) {
                updates.add("email = $" + index++);
                values.add(newEmail);
            }

            if (newFirstName != null && !newFirstName.isBlank()) {
                updates.add("first_name = $" + index++);
                values.add(newFirstName);
            }

            if (newLastName != null && !newLastName.isBlank()) {
                updates.add("last_name = $" + index++);
                values.add(newLastName);
            }

            if (newPassword != null && !newPassword.isBlank()) {

                String hashedPassword =
                    Hashing.hashPassword(newPassword);

                updates.add("hashed_password = $" + index++);
                values.add(hashedPassword);
            }

            // Nothing was supplied to update
            if (updates.isEmpty()) {
                return Future.succeededFuture(
                    new JsonObject()
                        .put("statusCode", 400)
                );
            }

            values.add(oldEmail);

            String query = """
                UPDATE users
                SET %s
                WHERE email = $%d
                RETURNING
                    user_id,
                    email,
                    first_name,
                    last_name,
                    role
                """.formatted(
                    String.join(", ", updates),
                    index
                );

            return pool.preparedQuery(query)
                .execute(Tuple.from(values))
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
                        .put("firstName", row.getString("first_name"))
                        .put("lastName", row.getString("last_name"))
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

    public Future<Integer> deleteUser(String email) {

        if (email == null || email.isBlank()) {
            return Future.succeededFuture(403);
        }

        String query = """
            DELETE FROM users
            WHERE email = $1
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