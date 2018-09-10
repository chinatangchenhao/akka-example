package com.dt.data.akka.http.low;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.IncomingConnection;
import akka.http.javadsl.ServerBinding;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletionStage;

/**
 * HTTP服务端
 */
public class HttpServer {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        Materializer materializer = ActorMaterializer.create(system);

        /**
         * 通过bind方法绑定IP和端口，绑定成功后拿到一个IncomingConnection的Source对象。
         *
         * IncomingConnection表示一次的请求连接，当客户端访问会创建该对象
         *
         */
        Source<IncomingConnection, CompletionStage<ServerBinding>> source =
            Http.get(system)
                .bind(ConnectHttp.toHost("localhost", 8090), materializer);

        CompletionStage<ServerBinding> future =
            source.to(
                Sink.foreach(conn -> {
                    // to do something
                    System.out.println("来自" + conn.remoteAddress() + "的访问！");
                })
            ).run(materializer);
    }
}
