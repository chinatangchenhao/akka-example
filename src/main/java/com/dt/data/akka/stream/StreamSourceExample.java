package com.dt.data.akka.stream;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * Source的构建
 */
public class StreamSourceExample {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        Materializer materializer = ActorMaterializer.create(system);

        //1.从集合中构建Source
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        Source<String, NotUsed> s1 = Source.from(list);
        s1.runForeach(System.out::println, materializer);
        //注意：这里的runForeach是对runWith(Sink.foreach(f), materializer)的封装

        //2.从Future中构建
        Source<String, NotUsed> s2 = Source.fromFuture(Futures.successful("Hello Akka!"));

        //3.构建可连续产生某类型数据的Source
        Source<String, NotUsed> s3 = Source.repeat("Hello");
        // 取出前5个
        s3.limit(5).runForeach(System.out::println, materializer);

        //4.从文件中构建
        //Source<ByteString, CompletionStage<IOResult>> s4 = FileIO.fromFile(Paths.get("README.md").toFile());
    }
}
