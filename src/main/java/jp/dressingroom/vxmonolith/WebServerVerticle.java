package jp.dressingroom.vxmonolith;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.Date;

public class WebServerVerticle extends AbstractVerticle {
  public static final String TEMPLATE = ""
    + "Path [%s]%n"
    + "Session [%s] created on %s%n"
    + "%n"
    + "Page generated on %s%n";

    // セッションありのWebアプリケーション。公式のhow-toを参考にしている。
    // https://how-to.vertx.io/web-session-infinispan-howto/
    @Override
    public void start() {
      HttpServer server = vertx.createHttpServer();
      SessionStore store1 = ClusteredSessionStore.create(vertx);

      Router router = Router.router((Vertx) vertx);
      router.route().handler(SessionHandler.create(store1));

      // 静的コンテンツは /static 以下に配置
      // 静的コンテンツの"webroot"は、プロジェクトのトップディレクトリ内に作成する。
      // 商用環境を想定するなら、これはCDNとかに置いてドメイン名で振り分ける部分。この書き方は、あくまで開発時の話。
      router.route("/static/*").handler(StaticHandler.create());

      // セッションを使うルーティングハンドラを設定
      router.route("/session/*").handler(getSessionedRoutingHandler());
      router.route("/").handler(getSessionedRoutingHandler());

      server.requestHandler(router).listen(8080);
    }

  private static Handler<RoutingContext> getSessionedRoutingHandler() {
    return routingContext -> {
      Session session = routingContext.session();
      session.computeIfAbsent("createdOn", s -> System.currentTimeMillis()); //(3)

      String sessionId = session.id();
      Date createdOn = new Date(session.<Long>get("createdOn"));
      Date now = new Date();

      routingContext.end(String.format(TEMPLATE, routingContext.request().path(), sessionId, createdOn, now));
      // System.out.println("Session ID: " + sessionId);
      // Session IDはこんな感じ Session ID: 7a83ccdb26df09b36ab673bb2f5e6a82
    };
  }
}
