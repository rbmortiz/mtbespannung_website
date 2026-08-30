package com.stringingbackend.backend.Stringing;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
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
                                string.put("name", row.getString("name"));
                                string.put("color", row.getString("color"));
                                string.put("description", row.getString("description"));
                                string.put("type", row.getString("type"));
                                string.put("is_active", row.getBoolean("is_active"));
                                string.put("created_at", row.getOffsetDateTime("created_at"));
                                string.put("updated_at", row.getOffsetDateTime("updated_at"));

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
                                string.put("created_at", row.getOffsetDateTime("created_at"));
                                string.put("updated_at", row.getOffsetDateTime("updated_at"));

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
}
