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

    public Future<JsonArray> getAllStringingOrders(RoutingContext ctx){
        System.out.println("[StringingService] getAllStringingOrders called");

        return stringingRepository.getAllStringingOrders()
            .compose(data -> {
                System.out.println("[StringingService] (200) getAllStringingOrders succeeded");

                return Future.succeededFuture(data);
            });
    }

    public Future<Integer> deleteStringingOrder(RoutingContext ctx) {
        System.out.println("[StringingService] deleteStringingOrder called");

        JsonObject data = ctx.user().principal();
        JsonObject body = ctx.body().asJsonObject();

        Integer stringId = body.getInteger("stringingOrderId");
        Integer userId = data.getInteger("userId");
        String email = data.getString("email");
        String userRole = data.getString("role");

        if (stringId == null || userId == null || email == null) {
            System.out.println("[StringingService] (403) deleteStringingOrder failed for [UserId: "+ userId + ", Email: " + email + ", StringId: " + stringId + "]");

            return Future.succeededFuture(403);
        }

        if("admin".equals(userRole)){
            return accountRepository.isAccountFree(email)
            .compose(isAccountFree -> {
                if (isAccountFree) {
                    System.out.println("[StringingService] (404) deleteStringingOrder failed for [UserId: "+ userId + ", Email: " + email + ", StringId: " + stringId + "]");

                    return Future.succeededFuture(404);
                }

                return stringingRepository.adminDeleteStringingOrder(stringId);
            });
        }

        return accountRepository.isAccountFree(email)
            .compose(isAccountFree -> {
                if (isAccountFree) {
                    System.out.println("[StringingService] (403) deleteStringingOrder failed for [UserId: "+ userId + ", Email: " + email + ", StringId: " + stringId + "]");

                    return Future.succeededFuture(404);
                }

                return stringingRepository.orderIsModifiable(stringId, userId);
            })
            .compose(status -> {
                if (status != 200) {
                    System.out.println("[StringingService] ("+ status +") deleteStringingOrder failed for [UserId: "+ userId + ", Email: " + email + ", StringId: " + stringId + "]");

                    return Future.succeededFuture(status);
                }

                return stringingRepository.deleteStringingOrder(userId, stringId);
            });
    }

    public Future<Integer> newStringingOrderAccount(RoutingContext ctx) {
        System.out.println("[StringingService] newStringingOrderAccount called");

        JsonObject data = ctx.user().principal();
        JsonObject body = ctx.body().asJsonObject();

        if (body == null) {
            System.out.println("[StringingService] (403) newStringingOrderAccount failed");

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

        if (firstName == null || lastName == null || email == null || racketName == null || stringId == null || userId == null) {
            System.out.println("[StringingService] (403) newStringingOrderAccount failed for [firstName: "+ firstName + ", lastName: "+ lastName + ", email: "+ email + ", racketName: "+ racketName + ", stringId: "+ stringId + ", userId: "+ userId + "]");

            return Future.succeededFuture(403);
        }

        return accountRepository.isAccountFree(email)
            .compose(isAccountFree -> {
                if (isAccountFree) {
                    System.out.println("[StringingService] (404) newStringingOrderAccount failed for [firstName: "+ firstName + ", lastName: "+ lastName + ", email: "+ email + ", racketName: "+ racketName + ", stringId: "+ stringId + ", userId: "+ userId + "]");

                    return Future.succeededFuture(404);
                }

                return stringingRepository.newStringingOrderAccount(userId, email, firstName, lastName, racketName, stringId, infos, vertKG, horKG);
            });
    }

    public Future<Integer> newStringingOrder(RoutingContext ctx) {
        System.out.println("[StringingService] newStringingOrder called");

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

        if(firstName == null || lastName == null || email == null || racketName == null || stringId == null) {
            System.out.println("[StringingService] (404) newStringingOrder failed for [firstName: "+ firstName + ", lastName: "+ lastName + ", email: "+ email + ", racketName: "+ racketName + ", stringId: "+ stringId + "]");

            return Future.succeededFuture(403);
        }

        return stringingRepository.newStringingOrder(email, firstName, lastName, racketName, stringId, infos, vertKG, horKG);
    }

    public Future<JsonArray> getStringingOrders(RoutingContext ctx) {
        System.out.println("[StringingService] getStringingOrders called");

        JsonObject tokenUser = ctx.user().principal();
        String email = tokenUser.getString("email");

        if (email == null || email.isBlank()) {
            System.out.println("[StringingService] (400) getStringingOrders failed");

            return Future.failedFuture("Email missing from JWT");
        }

        return accountRepository.getUser(email)
            .compose(user -> {
                if (user == null) {
                    System.out.println("[StringingService] (404) getStringingOrders failed for Email: "+ email);

                    return Future.succeededFuture(new JsonArray());
                }

                Integer id = user.getInteger("user_id");

                if (id == null) {
                    System.out.println("[StringingService] (404) getStringingOrders failed for Email: "+ email);

                    return Future.failedFuture("User ID missing");
                }

                return stringingRepository.getStringingOrders(id);
            });
    }

    public Future<JsonArray> getStrings(RoutingContext ctx){
        System.out.println("[StringingService] getStrings called");

        return stringingRepository.getStrings();
    }

    public Future<Integer> updateOrder(RoutingContext ctx) {
        System.out.println("[StringingService] updateOrder called");

        JsonObject body = ctx.body().asJsonObject();
        JsonObject data = ctx.user().principal();

        String userRole = data.getString("role");
        Integer userId = data.getInteger("userId");

        if (body == null || userId == null) {
            System.out.println("[StringingService] (400) updateOrder failed");

            return Future.succeededFuture(400);
        }

        Integer orderId = body.getInteger("order_id");
        String infos = body.getString("infos");

        Number kgVertNumber = body.getNumber("kgVert");
        Number kgHorNumber = body.getNumber("kgHor");

        BigDecimal kgVert = kgVertNumber == null ? null : BigDecimal.valueOf(kgVertNumber.doubleValue());
        BigDecimal kgHor = kgHorNumber == null ? null : BigDecimal.valueOf(kgHorNumber.doubleValue());

        if("admin".equals(userRole)){
            String orderStatus = body.getString("status");

            return stringingRepository.adminUpdateOrder(orderId, kgVert, kgHor, infos, orderStatus);
        }

        return stringingRepository.orderIsModifiable(orderId, userId)
            .compose(status -> {

                if (status != 200) {
                    System.out.println("[StringingService] ("+ status +") updateOrder failed");

                    return Future.succeededFuture(status);
                }

                System.out.println("[StringingService] (200) updateOrder succeeded");

                return stringingRepository.updateOrder(orderId, kgVert, kgHor, infos);
            });
    }
}
