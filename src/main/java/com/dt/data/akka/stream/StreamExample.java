package com.dt.data.akka.stream;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.RunnableGraph;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import java.util.concurrent.CompletionStage;

/**
 * mock数据打印到控制台
 */
public class StreamExample {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        Materializer materializer = ActorMaterializer.create(system);

        // 定义计算图
        // 第一个泛型表示它产生的数据类型，第二个泛型表示运行时产生的其他辅助数据，假如没有则设置为NotUsed
        Source<Integer, NotUsed> source = Source.range(1, 5);
        // 循环输出每个元素并输出到标准输出
        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);
        RunnableGraph<NotUsed> graph = source.to(sink);

        // 开始执行
        graph.run(materializer);
    }
}
