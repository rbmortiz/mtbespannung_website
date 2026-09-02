package com.stringingbackend.backend.Stringing;

import java.math.BigDecimal;

import com.stringingbackend.backend.accounts.AccountRepository;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Future;

public class StringingService {
    private final AccountRepository accountRepository;
    private final StringingRepository stringingRepository;

    public StringingService(AccountRepository accountRepository, StringingRepository stringingRepository){
        this.stringingRepository = stringingRepository;
        this.accountRepository = accountRepository;
    }

    public Future<Integer> deleteStringingOrder(RoutingContext ctx){
        JsonObject data = ctx.user().principal();
        JsonObject body = ctx.body().asJsonObject();

        Integer stringId = body.getInteger("stringingOrderId");
        Integer userId = data.getInteger("userId");
        String email = data.getString("email");

        if(stringId == null || userId == null || email == null){
            return Future.succeededFuture(403);
        }

        Boolean isAccountFree = accountRepository.isAccountFree(email).await();
        if(isAccountFree)return Future.succeededFuture(404);

        Boolean canStringingJobBeModified = stringingRepository.isOrderModifiable(stringId).await();
        if(!canStringingJobBeModified)return Future.succeededFuture(405);

        return stringingRepository.deleteStringingOrder(userId, stringId)
            .map(status -> {
                return status;
            })
            .recover(err -> {
                return Future.failedFuture("Database error");
            });
    }

    public Future<Integer> newStringingOrderAccount(RoutingContext ctx) {
        JsonObject data = ctx.user().principal();
        JsonObject body = ctx.body().asJsonObject();

        Number kgVertNumber = body.getNumber("kgVert");
        Number kgHorNumber = body.getNumber("kgHor");

        String racketName = body.getString("racket_name");
        BigDecimal vertKG = kgVertNumber == null ? null : BigDecimal.valueOf(kgVertNumber.doubleValue());
        BigDecimal horKG = kgHorNumber == null ? null : BigDecimal.valueOf(kgHorNumber.doubleValue());
        Integer stringId = body.getInteger("string_id");
        String infos = body.getString("additional_info");

        Integer userId = data.getInteger("userId");
        String firstName = data.getString("firstName");
        String lastName = data.getString("lastName");
        String email = data.getString("email");

        if(firstName == null || lastName == null || email == null || racketName == null || stringId == null || userId == null){
            return Future.succeededFuture(403);
        }

        Boolean isAccountFree = accountRepository.isAccountFree(email).await();
        if(isAccountFree)return Future.succeededFuture(404);

        return stringingRepository.newStringingOrderAccount(userId, email, firstName, lastName, racketName, stringId, infos, vertKG, horKG)   
            .map(status -> {
                return status;
            })
            .recover(err -> {
                return Future.failedFuture("Database error");
            });
    }

    public Future<Integer> newStringingOrder(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        Number kgVertNumber = body.getNumber("kgVert");
        Number kgHorNumber = body.getNumber("kgHor");

        String racketName = body.getString("racket_name");
        BigDecimal vertKG = kgVertNumber == null ? null : BigDecimal.valueOf(kgVertNumber.doubleValue());
        BigDecimal horKG = kgHorNumber == null ? null : BigDecimal.valueOf(kgHorNumber.doubleValue());
        Integer stringId = body.getInteger("string_id");
        String infos = body.getString("additional_info");

        String firstName = body.getString("customer_first_name");
        String lastName = body.getString("customer_last_name");
        String email = body.getString("customer_email");

        if(firstName == null || lastName == null || email == null || racketName == null || stringId == null){
            return Future.succeededFuture(403);
        }

        return stringingRepository.newStringingOrder(email, firstName, lastName, racketName, stringId, infos, vertKG, horKG)   
            .map(status -> {
                return status;
            })
            .recover(err -> {
                return Future.failedFuture("Database error");
            });
    }

    public Future<JsonArray> getStringingJobs(RoutingContext ctx) {

        JsonObject tokenUser = ctx.user().principal();
        String email = tokenUser.getString("email");

        if (email == null || email.isBlank()) {
            return Future.failedFuture("Email missing from JWT");
        }

        return accountRepository.getUser(email)
            .compose(user -> {

                if (user == null) {
                    return Future.succeededFuture(new JsonArray());
                }

                Integer id = user.getInteger("user_id");

                if (id == null) {
                    return Future.failedFuture("User ID missing");
                }

                return stringingRepository.getStringingJobs(id);
            });
    }

    public Future<JsonArray> getStrings(RoutingContext ctx){
        return stringingRepository.getStrings();
    }

    public Future<Integer> updateOrder(RoutingContext ctx) {

        JsonObject body = ctx.body().asJsonObject();

        Integer orderId = body.getInteger("order_id");
        String infos = body.getString("infos");

        Number kgVertNumber = body.getNumber("kgVert");
        Number kgHorNumber = body.getNumber("kgHor");

        BigDecimal kgVert = kgVertNumber == null ? null : BigDecimal.valueOf(kgVertNumber.doubleValue());

        BigDecimal kgHor = kgHorNumber == null ? null : BigDecimal.valueOf(kgHorNumber.doubleValue());

        Boolean canStringingJobBeModified = stringingRepository.isOrderModifiable(orderId).await();
        if(!canStringingJobBeModified)return Future.succeededFuture(403);

        return stringingRepository.isValidOrder(orderId)
            .compose(response -> {

                if (response != 200) {
                    return Future.succeededFuture(response);
                }

                return stringingRepository.orderIsModifiable(orderId)
                    .compose(res -> {

                        if (res != 200) {
                            return Future.succeededFuture(res);
                        }

                        return stringingRepository.updateOrder(
                            orderId,
                            kgVert,
                            kgHor,
                            infos
                        );
                    });
            });
    }
}
