package com.dt.data.akka.http.low;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import java.util.concurrent.CompletionStage;

/**
 * HTTP客户端
 */
public class HttpClient {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        Materializer materializer = ActorMaterializer.create(system);

        CompletionStage<HttpResponse> responseFuture =
            Http.get(system)
                .singleRequest(
                    HttpRequest.create("http://localhost:8090/items?itemId=153")
                               .withMethod(HttpMethods.GET),
                    materializer);

        responseFuture.thenAccept(response -> {
            response.entity().getDataBytes().runWith(Sink.foreach(content -> {
                System.out.println(content.utf8String());
            }), materializer);
        });
    }
}
