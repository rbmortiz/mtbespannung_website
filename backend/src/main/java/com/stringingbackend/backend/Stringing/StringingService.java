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
