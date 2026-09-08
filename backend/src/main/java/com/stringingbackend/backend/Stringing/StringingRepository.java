package com.stringingbackend.backend.Stringing;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import io.vertx.core.Future;

public class StringingRepository {
    private final Pool pool;

    public StringingRepository(Pool pool){
        this.pool = pool;
    }

    public Future<JsonArray> getAdminUsers(){
        String query = """
            SELECT * FROM stringing_orders
        """;

        return pool.query(query)
                    .execute()
                    .map(rows -> {
                        JsonArray orders = new JsonArray();

                        for(Row row : rows){

                            BigDecimal verticalKg = row.getBigDecimal("vertical_kg");
                            BigDecimal horizontalKg = row.getBigDecimal("horizontal_kg");

                            OffsetDateTime createdAt = row.getOffsetDateTime("created_at");
                            OffsetDateTime updatedAt = row.getOffsetDateTime("updated_at");

                            orders.add(
                                new JsonObject()
                                    .put("order_id", row.getInteger("order_id"))
                                    .put("user_id", row.getInteger("user_id"))
                                    .put("customer_first_name", row.getString("customer_first_name"))
                                    .put("customer_last_name", row.getString("customer_last_name"))
                                    .put("customer_email", row.getString("customer_email"))
                                    .put("racket_name", row.getString("racket_name"))
                                    .put("additional_info", row.getString("additional_info"))
                                    .put("vertical_kg", verticalKg == null ? null : verticalKg.doubleValue())
                                    .put("horizontal_kg", horizontalKg == null ? null : horizontalKg.doubleValue())
                                    .put("string_id", row.getInteger("string_id"))
                                    .put("status", row.getString("status"))
                                    .put("created_at", createdAt == null ? null : createdAt.toString())
                                    .put("updated_at", updatedAt == null ? null : updatedAt.toString())
                            );
                        }

                        return orders;
                    });
    }

    public Future<JsonArray> getAdminStringingOrders(){
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

                        return users;
                    });
    }

    public Future<Integer> newStringingOrder(String email, String firstName, String lastName, String racketName, Integer stringId, String infos, BigDecimal vertKG, BigDecimal horKG) {

        String query = """
            INSERT INTO stringing_orders(
                customer_first_name,
                customer_last_name,
                customer_email,
                racket_name,
                additional_info,
                horizontal_kg,
                vertical_kg,
                string_id,
                price
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
            """;

        return getPriceOfString(stringId)
            .compose(priceObj -> {

                if (priceObj.isEmpty()) {
                    return Future.succeededFuture(404);
                }

                Number priceNumber = priceObj.getNumber("price");

                if (priceNumber == null) {
                    return Future.succeededFuture(500);
                }

                BigDecimal price = BigDecimal.valueOf(priceNumber.doubleValue());
                price = price.add(new BigDecimal(15));

                return pool.preparedQuery(query)
                    .execute(Tuple.of(firstName, lastName, email, racketName, infos, horKG, vertKG, stringId, price))
                    .map(result -> 200);
            });
    }

    public Future<Integer> newStringingOrderAccount(Integer userId, String email, String firstName, String lastName, String racketName, Integer stringId, String infos, BigDecimal vertKG, BigDecimal horKG){
        String query = """
            INSERT INTO stringing_orders(
                user_id,
                customer_first_name,
                customer_last_name,
                customer_email,
                racket_name,
                additional_info,
                horizontal_kg,
                vertical_kg,
                string_id,
                price
            )
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
            """;

        return getPriceOfString(stringId)
            .compose(priceObj -> {

                if (priceObj.isEmpty()) {
                    return Future.succeededFuture(404);
                }

                Number priceNumber = priceObj.getNumber("price");

                if (priceNumber == null) {
                    return Future.succeededFuture(500);
                }

                BigDecimal price = BigDecimal.valueOf(priceNumber.doubleValue());
                price = price.add(BigDecimal.valueOf(15));

                return pool.preparedQuery(query)
                    .execute(Tuple.of(userId, firstName, lastName, email, racketName, infos, horKG, vertKG, stringId, price))
                    .map(result -> 200);
            });
    }

    public Future<JsonObject> getPriceOfString(Integer string_id){
        String query = """
            SELECT price FROM strings WHERE string_id = $1
        """;

        return pool.preparedQuery(query)
                    .execute(Tuple.of(string_id))
                    .map(rows -> {
                        if(rows.rowCount() != 1){
                            return new JsonObject();
                        }

                        Row row = rows.iterator().next();
                        BigDecimal price = row.getBigDecimal("price");

                        return new JsonObject().put("price", price == null ? null : price.doubleValue());
                    })
                    .recover(err -> {
                        err.printStackTrace();
                        return Future.failedFuture(err);
                    });
    }

    public Future<JsonArray> getStrings() {

        String query = """
            SELECT * FROM strings
        """;

        return pool.query(query)
                    .execute()
                    .map(result -> {
                        JsonArray ans = new JsonArray();

                        if(result.rowCount()>0){
                            for(Row row : result){
                                JsonObject string = new JsonObject();

                                string.put("string_id", row.getInteger("string_id"));
                                string.put("sport", row.getString("sport"));
                                string.put("name", row.getString("name"));
                                string.put("color", row.getString("color"));
                                string.put("description", row.getString("description"));
                                string.put("type", row.getString("type"));
                                string.put("is_active", row.getBoolean("is_active"));

                                OffsetDateTime createdAt = row.getOffsetDateTime("created_at");
                                OffsetDateTime updatedAt = row.getOffsetDateTime("updated_at");
                                BigDecimal price = row.getBigDecimal("price");

                                string.put("price", price == null ? null : price.doubleValue());
                                string.put("created_at", createdAt.toString());
                                string.put("updated_at", updatedAt.toString());

                                ans.add(string);
                            }
                        }

                        return ans;
                    })
                    .recover(err -> {
                        err.printStackTrace();
                        return Future.failedFuture("Database error");
                    });
    }

    public Future<JsonArray> getStringingJobs(Integer id) {

        String query = """
            SELECT * FROM stringing_orders WHERE user_id = $1
        """;

        return pool.preparedQuery(query)
                    .execute(Tuple.of(id))
                    .map(result -> {
                        JsonArray ans = new JsonArray();

                        if(result.rowCount()>0){
                            for(Row row : result){
                                JsonObject string = new JsonObject();

                                BigDecimal horizontalKg = row.getBigDecimal("horizontal_kg");
                                BigDecimal verticalKg = row.getBigDecimal("vertical_kg");
                                BigDecimal price = row.getBigDecimal("price");

                                string.put("order_id", row.getInteger("order_id"));
                                string.put("racket_name", row.getString("racket_name"));
                                string.put("additional_info", row.getString("additional_info"));
                                string.put("horizontal_kg", horizontalKg == null ? null : horizontalKg.doubleValue());
                                string.put("vertical_kg", verticalKg == null ? null : verticalKg.doubleValue());
                                string.put("string_id", row.getInteger("string_id"));
                                string.put("status", row.getString("status"));
                                string.put("price", price == null ? null : price.doubleValue());
                                
                                OffsetDateTime createdAt = row.getOffsetDateTime("created_at");
                                OffsetDateTime updatedAt = row.getOffsetDateTime("updated_at");

                                string.put("created_at", createdAt.toString());
                                string.put("updated_at", updatedAt.toString());

                                ans.add(string);
                            }
                        }

                        return ans;
                    })
                    .recover(err -> {
                        err.printStackTrace();
                        return Future.failedFuture("Database error");
                    });
    }

    public Future<Integer> isValidOrder(Integer id){
        String query = """
                SELECT * FROM stringing_orders WHERE order_id=$1
            """;

        if(id == null) return Future.succeededFuture(404);

        return pool.preparedQuery(query)
                    .execute(Tuple.of(id))
                    .map(result -> {
                        if(result.rowCount()>0){
                            return 200;
                        }
                        return 404;
                    })
                    .recover(err -> {
                        return Future.failedFuture("Database error");
                    });
    }

    public Future<Integer> deleteStringingOrder(Integer userId, Integer orderId){
        String query = """
            DELETE FROM stringing_orders WHERE user_id=$1 AND order_id=$2
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(userId, orderId))
            .map(result -> {
                if(result.rowCount() == 0){
                    return 404;
                }

                return 200;
            })
            .recover(error -> {
                return Future.failedFuture(error);
            });
    }

    public Future<Integer> adminDeleteStringingOrder(Integer orderId){
        String query = """
            DELETE FROM stringing_orders WHERE order_id=$1
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(orderId))
            .map(result -> {
                if(result.rowCount() == 0){
                    return 404;
                }

                return 200;
            })
            .recover(error -> {
                return Future.failedFuture(error);
            });
    }

    public Future<Integer> orderIsModifiable(Integer id, Integer userId) {
        if (id == null) {
            return Future.succeededFuture(404);
        }

        String query = """
            SELECT status, user_id
            FROM stringing_orders
            WHERE order_id = $1
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(id))
            .map(result -> {

                if (!result.iterator().hasNext()) {
                    return 404;
                }

                Row row = result.iterator().next();
                String status = row.getString("status");
                Integer uId = row.getInteger("user_id");

                if ("pending".equals(status) && userId.equals(uId)) {
                    return 200;
                }

                return 403;
            });
    }

    public Future<Integer> adminUpdateOrder(Integer orderId, BigDecimal kgVert, BigDecimal kgHor, String infos, String orderStatus) {

        List<String> updates = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        int index = 1;

        if (kgVert != null) {
            updates.add("vertical_kg = $" + index++);
            values.add(kgVert);
        }

        if (kgHor != null) {
            updates.add("horizontal_kg = $" + index++);
            values.add(kgHor);
        }

        if (infos != null && !infos.isBlank()) {
            updates.add("additional_info = $" + index++);
            values.add(infos);
        }

        if (orderStatus != null && !orderStatus.isBlank()) {
            updates.add("orderStatus = $" + index++);
            values.add(orderStatus);
        }

        // No fields to update
        if (updates.isEmpty()) {
            return Future.succeededFuture(400);
        }

        values.add(orderId);

        String query = """
            UPDATE stringing_orders
            SET %s
            WHERE order_id = $%d
            """.formatted(
                String.join(", ", updates),
                index
            );

        return pool.preparedQuery(query)
            .execute(Tuple.from(values))
            .map(result -> {

                if (result.rowCount() == 0) {
                    return 404;
                }

                return 200;
            })
            .recover(err -> {
                err.printStackTrace();
                return Future.succeededFuture(500);
            });
    }

    public Future<Integer> updateOrder(Integer orderId, BigDecimal kgVert, BigDecimal kgHor, String infos) {

        List<String> updates = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        int index = 1;

        if (kgVert != null) {
            updates.add("vertical_kg = $" + index++);
            values.add(kgVert);
        }

        if (kgHor != null) {
            updates.add("horizontal_kg = $" + index++);
            values.add(kgHor);
        }

        if (infos != null && !infos.isBlank()) {
            updates.add("additional_info = $" + index++);
            values.add(infos);
        }

        // No fields to update
        if (updates.isEmpty()) {
            return Future.succeededFuture(400);
        }

        values.add(orderId);

        String query = """
            UPDATE stringing_orders
            SET %s
            WHERE order_id = $%d
            """.formatted(
                String.join(", ", updates),
                index
            );

        return pool.preparedQuery(query)
            .execute(Tuple.from(values))
            .map(result -> {

                if (result.rowCount() == 0) {
                    return 404;
                }

                return 200;
            })
            .recover(err -> {
                err.printStackTrace();
                return Future.succeededFuture(500);
            });
    }
}


