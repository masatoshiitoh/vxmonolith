package jp.dressingroom.vxmonolith;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    vertx.deployVerticle(WebServerVerticle.class.getName()).onComplete(ar -> {
      if (ar.succeeded()) {
        System.out.println("WebServerVerticle started");
        startPromise.complete();
      } else {
        System.out.println("WebServerVerticle failed to start");
        startPromise.fail(ar.cause());
      }
    });


  }
}
