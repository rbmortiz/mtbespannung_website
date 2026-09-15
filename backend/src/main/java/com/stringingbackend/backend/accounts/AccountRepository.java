package com.stringingbackend.backend.accounts;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.stringingbackend.backend.accounts.tools.Hashing;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
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
        System.out.println("[AccountRepository] isAccountFree called");

        String query = """
            SELECT user_id FROM users WHERE email = $1
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(email))
            .map(rows -> {
                boolean temp = rows.size() == 0;

                System.out.println("[AccountRepository] ("+ temp +") isAccountFree succeeded for "+ email);

                return temp;
            });
    }

    public Future<JsonObject> registerUser(String email, String firstName, String lastName, String password) {
        System.out.println("[AccountRepository] registerUser called");

        String query = """
            INSERT INTO users (first_name, last_name, email, hashed_password) VALUES ($1, $2, $3, $4) RETURNING user_id
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(firstName, lastName, email, password))
            .map(result -> {
                Row row = result.iterator().next();

                System.out.println("[AccountRepository] (200) registerUser succeeded for (" + email + "," + firstName + "," + lastName + ")");
                return new JsonObject()
                    .put("statusCode", 200)
                    .put("user_id", row.getInteger("user_id"))
                    .put("email", email)
                    .put("firstName", firstName)
                    .put("lastName", lastName)
                    .put("role", "user");
            })
            .recover(err -> {
                System.out.println("[AccountRepository] (500) registerUser failed for (" + email + "," + firstName + "," + lastName + ")");
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
        System.out.println("[AccountRepository] getUser called");

        String query = """
            SELECT user_id, first_name, last_name, email, hashed_password, role FROM users WHERE email = $1
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(email))
            .compose(rows -> {

                if (!rows.iterator().hasNext()) {
                    System.out.println("[AccountRepository] (404) getUser failed for "+ email);
                    return Future.succeededFuture(null);
                }

                Row row = rows.iterator().next();

                JsonObject user = new JsonObject()
                    .put("user_id", row.getInteger("user_id"))
                    .put("firstName", row.getString("first_name"))
                    .put("lastName", row.getString("last_name"))
                    .put("email", row.getString("email"))
                    .put("hashedPassword", row.getString("hashed_password"))
                    .put("role", row.getString("role"));
                
                System.out.println("[AccountRepository] (200) getUser succeeded for "+ email);

                return Future.succeededFuture(user);
            });
    }

    public Future<JsonObject> updateUser(String oldEmail, String newEmail, String newPassword, String newFirstName, String newLastName) {
        System.out.println("[AccountRepository] updateUser called");

        Future<Boolean> emailCheck;

        if (newEmail != null && !newEmail.isBlank() && !newEmail.equalsIgnoreCase(oldEmail)) {
            String emailQuery = """
                SELECT user_id FROM users WHERE email = $1
            """;

            emailCheck = pool.preparedQuery(emailQuery)
                .execute(Tuple.of(newEmail))
                .map(rows -> rows.size() == 0);
        } 
        
        else emailCheck = Future.succeededFuture(true);

        return emailCheck.compose(emailFree -> {
            if (!emailFree) {
                System.out.println("[AccountRepository] (409) updateUser failed for "+ oldEmail);
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
                System.out.println("[AccountRepository] (400) updateUser failed for "+ oldEmail);

                return Future.succeededFuture(
                    new JsonObject()
                        .put("statusCode", 400)
                );
            }

            values.add(oldEmail);

            String query = """
                UPDATE users SET %s WHERE email = $%d RETURNING user_id, email, first_name, last_name, role
            """.formatted(String.join(", ", updates), index);

            return pool.preparedQuery(query)
                .execute(Tuple.from(values))
                .map(rows -> {

                    if (!rows.iterator().hasNext()) {
                        System.out.println("[AccountRepository] (404) updateUser failed for "+ oldEmail);

                        return new JsonObject()
                            .put("statusCode", 404);
                    }

                    Row row = rows.iterator().next();
        
                    System.out.println("[AccountRepository] (200) updateUser succeeded for "+ oldEmail);
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

            System.err.println("[AccountRepository] (500) updateUser failed for "+ oldEmail);
            err.printStackTrace();

            return Future.succeededFuture(
                new JsonObject()
                    .put("statusCode", 500)
            );
        });
    }

    public Future<Integer> deleteUser(String email) {
        System.out.println("[AccountRepository] deleteUser called");

        if (email == null || email.isBlank()) {
            return Future.succeededFuture(403);
        }

        String query = """
            DELETE FROM users WHERE email = $1
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(email))
            .map(result -> {

                if (result.rowCount() == 0) {
                    System.out.println("[AccountRepository] (401) updateUser failed for "+ email);
                    return 401;
                }

                System.out.println("[AccountRepository] (200) updateUser succeeded for "+ email);
                return 200;
            })
            .recover(err -> {

                System.out.println("[AccountRepository] (500) updateUser failed for "+ email);
                err.printStackTrace();

                return Future.succeededFuture(500);
            });
    }

    public Future<JsonArray> getAllUsers(){
        System.out.println("[AccountRepository] getAllUsers called");

        String query = """
            SELECT * FROM users
        """;

        return pool.query(query)
            .execute()
            .map(rows -> {
                JsonArray users = new JsonArray();

                for(Row row : rows){
                    OffsetDateTime createdAt = row.getOffsetDateTime("created_at");
                    OffsetDateTime updatedAt = row.getOffsetDateTime("updated_at");

                    users.add(
                        new JsonObject()
                            .put("user_id", row.getInteger("user_id"))
                            .put("first_name", row.getString("first_name"))
                            .put("last_name", row.getString("last_name"))
                            .put("email", row.getString("email"))
                            .put("role", row.getString("role"))
                            .put("created_at", createdAt == null ? null : createdAt.toString())
                            .put("updated_at", updatedAt == null ? null : updatedAt.toString())
                    );
                }
                System.out.println("[AccountRepository] (200) getAllUsers succeeded");

                return users;
            });
    }
}