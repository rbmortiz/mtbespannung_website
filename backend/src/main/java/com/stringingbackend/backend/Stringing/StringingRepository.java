package com.stringingbackend.backend.Stringing;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.Future;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class StringingRepository {
    private final Pool pool;

    public StringingRepository(Pool pool){
        this.pool = pool;
    }

    public Future<JsonArray> getAllStringingOrders(){
        System.out.println("[StringingRepository] getAllStringingOrders called");

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

                    BigDecimal price = row.getBigDecimal("price");

                    orders.add(new JsonObject()
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
                            .put("price", price == null ? null : price.doubleValue())
                            .put("created_at", createdAt == null ? null : createdAt.toString())
                            .put("updated_at", updatedAt == null ? null : updatedAt.toString())
                    );
                }

                System.out.println("[StringingRepository] (200) getAllStringingOrders succeeded");

                return orders;
            });
    }

    public Future<Integer> newStringingOrder(String email, String firstName, String lastName, String racketName, Integer stringId, String infos, BigDecimal vertKG, BigDecimal horKG) {
        System.out.println("[StringingRepository] newStringingOrder called");

        String query = """
            INSERT INTO stringing_orders(customer_first_name, customer_last_name, customer_email, racket_name, additional_info, horizontal_kg, vertical_kg, string_id, price)
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
        """;

        return getPriceOfString(stringId)
            .compose(priceObj -> {
                if (priceObj.isEmpty()) {
                    System.err.println("[StringingRepository] (404) newStringingOrder failed");

                    return Future.succeededFuture(404);
                }

                Number priceNumber = priceObj.getNumber("price");

                if (priceNumber == null) {
                    System.err.println("[StringingRepository] (500) newStringingOrder failed");

                    return Future.succeededFuture(500);
                }

                BigDecimal price = BigDecimal.valueOf(priceNumber.doubleValue());
                price = price.add(new BigDecimal(15));

                return pool.preparedQuery(query)
                    .execute(Tuple.of(firstName, lastName, email, racketName, infos, horKG, vertKG, stringId, price))
                    .map(result -> {
                        System.out.println("[StringingRepository] (200) newStringingOrder succeeded");
                        return 200;
                    });
            });
    }

    public Future<Integer> newStringingOrderAccount(Integer userId, String email, String firstName, String lastName, String racketName, Integer stringId, String infos, BigDecimal vertKG, BigDecimal horKG){
        System.out.println("[StringingRepository] newStringingOrderAccount called");

        String query = """
            INSERT INTO stringing_orders(user_id, customer_first_name, customer_last_name, customer_email, racket_name, additional_info, horizontal_kg, vertical_kg, string_id, price)
            VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10)
        """;

        return getPriceOfString(stringId)
            .compose(priceObj -> {
                if (priceObj.isEmpty()) {
                    System.err.println("[StringingRepository] (404) newStringingOrderAccount failed");

                    return Future.succeededFuture(404);
                }

                Number priceNumber = priceObj.getNumber("price");

                if (priceNumber == null) {
                    System.err.println("[StringingRepository] (500) newStringingOrderAccount failed");

                    return Future.succeededFuture(500);
                }

                BigDecimal price = BigDecimal.valueOf(priceNumber.doubleValue());
                price = price.add(BigDecimal.valueOf(15));

                return pool.preparedQuery(query)
                    .execute(Tuple.of(userId, firstName, lastName, email, racketName, infos, horKG, vertKG, stringId, price))
                    .map(result -> {
                        System.out.println("[StringingRepository] (200) newStringingOrderAccount succeeded");
                        return 200;
                    });
            });
    }

    public Future<JsonObject> getPriceOfString(Integer string_id){
        System.out.println("[StringingRepository] getPriceOfString called");

        String query = """
            SELECT price FROM strings WHERE string_id = $1
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(string_id))
            .map(rows -> {
                if(rows.rowCount() != 1){
                    System.err.println("[StringingRepository] (404) getPriceOfString failed");
                    return new JsonObject();
                }

                Row row = rows.iterator().next();
                BigDecimal price = row.getBigDecimal("price");

                return new JsonObject().put("price", price == null ? null : price.doubleValue());
            })
            .recover(err -> {
                System.err.println("[StringingRepository] (500) getPriceOfString failed");

                err.printStackTrace();
                return Future.failedFuture(err);
            });
    }

    public Future<JsonArray> getStrings() {
        System.out.println("[StringingRepository] getStrings called");

        String query = """
            SELECT * FROM strings
        """;

        return pool.query(query)
            .execute()
            .map(result -> {
                JsonArray ans = new JsonArray();

                if(result.rowCount()>0){
                    for(Row row : result){
                        OffsetDateTime createdAt = row.getOffsetDateTime("created_at");
                        OffsetDateTime updatedAt = row.getOffsetDateTime("updated_at");
                        BigDecimal price = row.getBigDecimal("price");

                        ans.add(new JsonObject()
                            .put("string_id", row.getInteger("string_id"))
                            .put("sport", row.getString("sport"))
                            .put("name", row.getString("name"))
                            .put("color", row.getString("color"))
                            .put("description", row.getString("description"))
                            .put("type", row.getString("type"))
                            .put("is_active", row.getBoolean("is_active"))

                            .put("price", price == null ? null : price.doubleValue())
                            .put("created_at", createdAt.toString())
                            .put("updated_at", updatedAt.toString())
                        );
                    }
                } 
                
                else {
                    System.out.println("[StringingRepository] (404) getStrings failed");
                    return ans;
                }

                System.out.println("[StringingRepository] (200) getStrings succeeded");

                return ans;
            })
            .recover(err -> {
                System.err.println("[StringingRepository] (500) getStrings failed");

                err.printStackTrace();
                return Future.failedFuture("Database error");
            });
    }

    public Future<JsonArray> getStringingOrders(Integer id) {
        System.out.println("[StringingRepository] getStringingOrders called");

        String query = """
            SELECT * FROM stringing_orders WHERE user_id = $1
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(id))
            .map(result -> {
                JsonArray ans = new JsonArray();

                if(result.rowCount()>0){
                    for(Row row : result){
                        BigDecimal horizontalKg = row.getBigDecimal("horizontal_kg");
                        BigDecimal verticalKg = row.getBigDecimal("vertical_kg");
                        BigDecimal price = row.getBigDecimal("price");

                        OffsetDateTime createdAt = row.getOffsetDateTime("created_at");
                        OffsetDateTime updatedAt = row.getOffsetDateTime("updated_at");

                        ans.add(new JsonObject()
                            .put("order_id", row.getInteger("order_id"))
                            .put("racket_name", row.getString("racket_name"))
                            .put("additional_info", row.getString("additional_info"))
                            .put("horizontal_kg", horizontalKg == null ? null : horizontalKg.doubleValue())
                            .put("vertical_kg", verticalKg == null ? null : verticalKg.doubleValue())
                            .put("string_id", row.getInteger("string_id"))
                            .put("status", row.getString("status"))
                            .put("price", price == null ? null : price.doubleValue())
                            .put("created_at", createdAt.toString())
                            .put("updated_at", updatedAt.toString())
                        );
                    }
                }

                else {
                    System.out.println("[StringingRepository] (404) getStringingOrders failed");
                    return ans;
                }

                System.out.println("[StringingRepository] (200) getStringingOrders succeeded");

                return ans;
            })
            .recover(err -> {
                System.err.println("[StringingRepository] (500) getStrings failed");

                err.printStackTrace();
                return Future.failedFuture("Database error");
            });
    }

    public Future<Integer> isValidOrder(Integer id){
        System.out.println("[StringingRepository] isValidOrder called");

        String query = """
            SELECT * FROM stringing_orders WHERE order_id=$1
        """;

        if(id == null) {
            System.err.println("[StringingRepository] (404) isValidOrder failed");
            return Future.succeededFuture(404);
        }

        return pool.preparedQuery(query)
            .execute(Tuple.of(id))
            .map(result -> {
                if(result.rowCount()>0){
                    System.out.println("[StringingRepository] (200) isValidOrder succeeded");

                    return 200;
                }

                System.out.println("[StringingRepository] (404) isValidOrder failed");

                return 404;
            })
            .recover(err -> {
                System.err.println("[StringingRepository] (500) isValidOrder failed");

                return Future.failedFuture("Database error");
            });
    }

    public Future<Integer> deleteStringingOrder(Integer userId, Integer orderId){
        System.out.println("[StringingRepository] deleteStringingOrder called");

        String query = """
            DELETE FROM stringing_orders WHERE user_id=$1 AND order_id=$2
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(userId, orderId))
            .map(result -> {
                if(result.rowCount() == 0){
                    System.out.println("[StringingRepository] (404) deleteStringingOrder failed");

                    return 404;
                }

                System.out.println("[StringingRepository] (200) deleteStringingOrder succeeded");

                return 200;
            })
            .recover(error -> {
                System.out.println("[StringingRepository] (500) deleteStringingOrder failed");

                return Future.failedFuture(error);
            });
    }

    public Future<Integer> adminDeleteStringingOrder(Integer orderId){
        System.out.println("[StringingRepository] adminDeleteStringingOrder called");

        String query = """
            DELETE FROM stringing_orders WHERE order_id=$1
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(orderId))
            .map(result -> {
                if(result.rowCount() == 0){
                    System.out.println("[StringingRepository] (404) adminDeleteStringingOrder failed");

                    return 404;
                }

                System.out.println("[StringingRepository] (200) adminDeleteStringingOrder succeeded");

                return 200;
            })
            .recover(error -> {
                System.err.println("[StringingRepository] (500) adminDeleteStringingOrder failed");

                return Future.failedFuture(error);
            });
    }

    public Future<Integer> orderIsModifiable(Integer id, Integer userId) {
        System.out.println("[StringingRepository] orderIsModifiable called");

        if (id == null) {
            System.out.println("[StringingRepository] (404) orderIsModifiable failed");

            return Future.succeededFuture(404);
        }

        String query = """
            SELECT status, user_id FROM stringing_orders WHERE order_id = $1
        """;

        return pool.preparedQuery(query)
            .execute(Tuple.of(id))
            .map(result -> {

                if (!result.iterator().hasNext()) {
                    System.out.println("[StringingRepository] (404) orderIsModifiable failed");

                    return 404;
                }

                Row row = result.iterator().next();
                String status = row.getString("status");
                Integer uId = row.getInteger("user_id");

                if ("pending".equals(status) && userId.equals(uId)) {
                    System.out.println("[StringingRepository] (200) orderIsModifiable succeeded");

                    return 200;
                }

                System.out.println("[StringingRepository] (403) orderIsModifiable failed");

                return 403;
            });
    }

    public Future<Integer> adminUpdateOrder(Integer orderId, BigDecimal kgVert, BigDecimal kgHor, String infos, String orderStatus) {
        System.out.println("[StringingRepository] adminUpdateOrder called");

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

        if (updates.isEmpty()) {
            return Future.succeededFuture(400);
        }

        values.add(orderId);

        String query = """
            UPDATE stringing_orders SET %s WHERE order_id = $%d
        """ .formatted(String.join(", ", updates), index);

        return pool.preparedQuery(query)
            .execute(Tuple.from(values))
            .map(result -> {
                if (result.rowCount() == 0) {
                    System.out.println("[StringingRepository] (400) adminUpdateOrder failed");

                    return 404;
                }

                System.out.println("[StringingRepository] (200) adminUpdateOrder succeeded");

                return 200;
            })
            .recover(err -> {
                System.err.println("[StringingRepository] (500) adminUpdateOrder failed");

                err.printStackTrace();
                return Future.succeededFuture(500);
            });
    }

    public Future<Integer> updateOrder(Integer orderId, BigDecimal kgVert, BigDecimal kgHor, String infos) {
        System.out.println("[StringingRepository] updateOrder called");

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

        if (updates.isEmpty()) {
            return Future.succeededFuture(400);
        }

        values.add(orderId);

        String query = """
            UPDATE stringing_orders SET %s WHERE order_id = $%d
        """ .formatted(String.join(", ", updates), index);

        return pool.preparedQuery(query)
            .execute(Tuple.from(values))
            .map(result -> {
                if (result.rowCount() == 0) {
                    System.out.println("[StringingRepository] (404) updateOrder failed");

                    return 404;
                }

                System.out.println("[StringingRepository] (200) updateOrder succeeded");

                return 200;
            })
            .recover(err -> {
                System.err.println("[StringingRepository] (500) updateOrder failed");

                err.printStackTrace();
                return Future.succeededFuture(500);
            });
    }
}