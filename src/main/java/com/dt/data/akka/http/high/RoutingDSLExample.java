package com.dt.data.akka.http.high;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import java.util.concurrent.CompletionStage;

public class RoutingDSLExample extends AllDirectives {

    public static void main(String[] args) {
        new RoutingDSLExample().start();
    }

    public Route getRoute() {
        return get(() -> route(
                path("index", () -> complete("欢迎来到首页")),
                path("books", () -> complete("书籍列表")),
                path("book", () -> parameterOptional("bookId", bookId -> {
                    String bid = bookId.orElse("-1");
                    return complete("查询数据为:" + bid);
                })),
                path("shoppingcar", () -> {
                    HttpResponse response = HttpResponse.create()
                            .withEntity(ContentTypes.TEXT_HTML_UTF8,
                                    "<font color='red'>购物车列表</font>");
                    return complete(response);
                })
        ));
    }

    public void start() {
        ActorSystem system = ActorSystem.create("sys");
        Materializer materializer = ActorMaterializer.create(system);
        Flow<HttpRequest, HttpResponse, NotUsed> flow = getRoute().flow(system, materializer);
        CompletionStage<ServerBinding> binding =
        Http.get(system).bindAndHandle(flow,
                ConnectHttp.toHost("localhost", 8090), materializer);
    }
}