package com.stringingbackend.backend;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.VerticleBase;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class MainVerticle extends VerticleBase {

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new MainVerticle());
  }

  @Override
  public Future<?> start() {
    // Create a Router
    Router router = Router.router(vertx);
 
    // Mount the handler for all incoming requests at every path and HTTP method
    router.route().handler(context -> {
      // Get the address of the request
      String address = context.request().connection().remoteAddress().toString();
      // Get the query parameter "name"
      MultiMap queryParams = context.queryParams();
      String name = queryParams.contains("name") ? queryParams.get("name") : "unknown";
      // Write a json response
      context.json(
        new JsonObject()
          .put("name", name)
          .put("address", address)
          .put("message", "Hello " + name + " connected from " + address)
      );
    });
 
    // Create the HTTP server
    return vertx.createHttpServer()
      // Handle every request using the router
      .requestHandler(router)
      // Start listening
      .listen(8080)
      // Print the port on success
      .onSuccess(server -> {
        System.out.println("HTTP server started on port " + server.actualPort());
      })
      // Print the problem on failure
      .onFailure(throwable -> {
        throwable.printStackTrace();
      });
  }
}
