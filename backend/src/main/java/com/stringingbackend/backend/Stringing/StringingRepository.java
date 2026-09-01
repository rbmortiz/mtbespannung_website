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

                                string.put("order_id", row.getInteger("order_id"));
                                string.put("racket_name", row.getString("racket_name"));
                                string.put("additional_info", row.getString("additional_info"));
                                string.put("horizontal_kg", row.getBigDecimal("horizontal_kg").doubleValue());
                                string.put("vertical_kg", row.getBigDecimal("vertical_kg").doubleValue());
                                string.put("string_id", row.getInteger("string_id"));
                                string.put("status", row.getString("status"));
                                
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

    public Future<Integer> orderIsModifiable(Integer id) {
        if (id == null) {
            return Future.succeededFuture(404);
        }

        String query = """
            SELECT status
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

                if ("pending".equals(status)) {
                    return 200;
                }

                return 403;
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


