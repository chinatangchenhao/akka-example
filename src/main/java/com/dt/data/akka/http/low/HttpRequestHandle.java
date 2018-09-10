package com.dt.data.akka.http.low;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.IncomingConnection;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.*;
import akka.http.javadsl.model.headers.Location;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.util.concurrent.CompletionStage;

/**
 * HTTP请求处理
 */
public class HttpRequestHandle {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        Materializer materializer = ActorMaterializer.create(system);

        // 定义source
        Source<IncomingConnection, CompletionStage<ServerBinding>> source =
            Http.get(system)
                .bind(ConnectHttp.toHost("localhpst", 8090), materializer);

        // 定义请求处理
        Function<HttpRequest, HttpResponse> processRequest = new Function<HttpRequest, HttpResponse>() {

            HttpResponse resp404 = HttpResponse.create()
                    .withStatus(StatusCodes.NOT_FOUND)
                    .withEntity("404 Not Found!");

            @Override
            public HttpResponse apply(HttpRequest request) {
                // 获取请求路径
                String path = request.getUri().path();
                /**
                 * http://localhost:8090
                 */
                if (path.equals("/")) {
                    return HttpResponse.create()
                            .withEntity(ContentTypes.TEXT_HTML_UTF8,
                                    "<font color='red'>欢迎来到首页</font>");
                }
                /**
                 * http://localhost:8090/items?itemId=153
                 */
                else if (path.equals("/items")) {
                    Query query = request.getUri().query();
                    String itemId = query.get("itemId").orElse("not found");
                    return HttpResponse.create()
                            .withEntity("查找商品id=" + itemId);
                }
                /**
                 * http://localhost:8090/redirect
                 */
                else if (path.equals("/redirect")) {
                    // 重定向
                    Location location = Location.create("http://localhost:8090");
                    return HttpResponse.create()
                            .withStatus(StatusCodes.FOUND)
                            .addHeader(location);
                } else {
                    return resp404;
                }
            }
        };

        CompletionStage<ServerBinding> future =
            source.to(
                Sink.foreach(conn -> {
                    // to do something
                    System.out.println("来自" + conn.remoteAddress() + "的访问！");
                    conn.handleWithSyncHandler(processRequest, materializer);
                })
            ).run(materializer);
    }
}
