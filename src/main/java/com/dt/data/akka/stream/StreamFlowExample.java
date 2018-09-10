package com.dt.data.akka.stream;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Flow的使用
 */
public class StreamFlowExample {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        Materializer materializer = ActorMaterializer.create(system);

        //1.从集合中构建Source
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("5");
        // source
        Source<String, NotUsed> source = Source.from(list);

        // sink
        Sink<Integer, CompletionStage<Done>> sink = Sink.foreach(System.out::println);

        // flow 第一个泛型为数据输入的类型，第二个泛型为数据输出的类型
        Flow<String, Integer, NotUsed> flow = Flow.of(String.class).map(x -> {
            return Integer.parseInt(x) * 3;
        });

        /**
         * via方法将flow组建附加在Source上
         */
        source.via(flow).to(sink).run(materializer);
    }
}
