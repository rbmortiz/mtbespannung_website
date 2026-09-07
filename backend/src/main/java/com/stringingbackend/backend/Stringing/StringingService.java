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

    public Future<JsonArray> getAdminUsers(RoutingContext ctx){
        return stringingRepository.getAdminUsers()
            .compose(data -> {
                return Future.succeededFuture(data);
            });
    }

    public Future<JsonArray> getAdminStringingOrders(RoutingContext ctx){
        return stringingRepository.getAdminStringingOrders()
            .compose(data -> {
                return Future.succeededFuture(data);
            });
    }

    public Future<Integer> deleteStringingOrder(RoutingContext ctx) {

        JsonObject data = ctx.user().principal();
        JsonObject body = ctx.body().asJsonObject();

        Integer stringId = body.getInteger("stringingOrderId");

        Integer userId = data.getInteger("userId");
        String email = data.getString("email");

        System.out.println(stringId);
        System.out.println(userId);
        System.out.println(email);

        if (stringId == null || userId == null || email == null) return Future.succeededFuture(403);

        return accountRepository.isAccountFree(email)
            .compose(isAccountFree -> {

                if (isAccountFree) {
                    return Future.succeededFuture(404);
                }
                System.out.println("Executing orderIsModifiable for: " + email);
                return stringingRepository.orderIsModifiable(stringId);
            })
            .compose(status -> {

                if (status != 200) {
                    return Future.succeededFuture(status);
                }

                System.out.println("Executing deleteStringingOrder for: " + userId + ", " + stringId);
                return stringingRepository.deleteStringingOrder(userId, stringId);
            });
    }

    public Future<Integer> newStringingOrderAccount(RoutingContext ctx) {
        JsonObject data = ctx.user().principal();
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            return Future.succeededFuture(403);
        }

        Number kgVertNumber = body.getNumber("vertical_kg");
        Number kgHorNumber = body.getNumber("horizontal_kg");

        String racketName = body.getString("racket_name");

        BigDecimal vertKG = kgVertNumber == null ? null : BigDecimal.valueOf(kgVertNumber.doubleValue());

        BigDecimal horKG = kgHorNumber == null ? null : BigDecimal.valueOf(kgHorNumber.doubleValue());

        Integer stringId = body.getInteger("string_id");
        String infos = body.getString("additional_info");

        Integer userId = data.getInteger("userId");
        String firstName = data.getString("firstName");
        String lastName = data.getString("lastName");
        String email = data.getString("email");

        System.out.println(stringId);
        System.out.println(userId);
        System.out.println(email);
        System.out.println(firstName);
        System.out.println(lastName);

        if (firstName == null || lastName == null || email == null || racketName == null || stringId == null || userId == null) return Future.succeededFuture(403);

        return accountRepository.isAccountFree(email)
            .compose(isAccountFree -> {

                if (isAccountFree) {
                    return Future.succeededFuture(404);
                }

                return stringingRepository.newStringingOrderAccount(userId, email, firstName, lastName, racketName, stringId, infos, vertKG, horKG);
            });
    }

    public Future<Integer> newStringingOrder(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        Number kgVertNumber = body.getNumber("vertical_kg");
        Number kgHorNumber = body.getNumber("horizontal_kg");

        String racketName = body.getString("racket_name");
        BigDecimal vertKG = kgVertNumber == null ? null : BigDecimal.valueOf(kgVertNumber.doubleValue());
        BigDecimal horKG = kgHorNumber == null ? null : BigDecimal.valueOf(kgHorNumber.doubleValue());
        Integer stringId = body.getInteger("string_id");
        String infos = body.getString("additional_info");

        String firstName = body.getString("customer_first_name");
        String lastName = body.getString("customer_last_name");
        String email = body.getString("customer_email");

        System.out.println(stringId);
        System.out.println(email);
        System.out.println(firstName);
        System.out.println(lastName);

        if(firstName == null || lastName == null || email == null || racketName == null || stringId == null) return Future.succeededFuture(403);

        return stringingRepository.newStringingOrder(email, firstName, lastName, racketName, stringId, infos, vertKG, horKG);
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

        if (body == null) {
            return Future.succeededFuture(400);
        }

        Integer orderId = body.getInteger("order_id");
        String infos = body.getString("infos");

        Number kgVertNumber = body.getNumber("kgVert");
        Number kgHorNumber = body.getNumber("kgHor");

        BigDecimal kgVert = kgVertNumber == null ? null : BigDecimal.valueOf(kgVertNumber.doubleValue());

        BigDecimal kgHor = kgHorNumber == null ? null : BigDecimal.valueOf(kgHorNumber.doubleValue());

        return stringingRepository.orderIsModifiable(orderId)
            .compose(status -> {

                if (status != 200) {
                    return Future.succeededFuture(status);
                }

                return stringingRepository.updateOrder(orderId, kgVert, kgHor, infos);
            });
    }
}
