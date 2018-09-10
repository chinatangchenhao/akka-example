package com.dt.data.akka.stream;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletionStage;

/**
 * Sink构建方式
 */
public class StreamSinkExample {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        Materializer materializer = ActorMaterializer.create(system);

        // 1.循环输出
        Sink<Integer, CompletionStage<Done>> sink1 = Sink.foreach(System.out::println);

        // 2.fold
        Sink<Integer, CompletionStage<Integer>> sink2 = Sink.fold(1, (x, y) -> x * y);
        CompletionStage<Integer> r1 = Source.range(1, 5).runWith(sink2, materializer);
        // 1*2*3*4*5 = 120
        r1.thenAccept(System.out::println);

        // 3.reduce
        Sink<Integer, CompletionStage<Integer>> sink3 = Sink.reduce((x, y) -> x + y);
        CompletionStage<Integer> r2 = Source.range(1, 5).runWith(sink3, materializer);
        // 1+2+3+4+5 = 15
        r2.thenAccept(System.out::println);

        // 4.输出到文件
        //Sink<ByteString, CompletionStage<IOResult>> sink4 = FileIO.toFile(Paths.get("out.log").toFile());
    }
}
