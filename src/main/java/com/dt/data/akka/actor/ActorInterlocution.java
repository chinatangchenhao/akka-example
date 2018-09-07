package com.dt.data.akka.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.OnSuccess;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 * 发送和接受消息
 */
public class ActorInterlocution {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        ActorRef ask_ar = system.actorOf(Props.create(AskActorDemo.class), "askActorDemo");

        // tell
        //ask_ar.tell("Hello Akka", ActorRef.noSender()); // noSender 表示没有发送着（deadLetter Actor）

        // ask
        // 设置超时时间
        Timeout timeout = new Timeout(Duration.create(2, "seconds"));
        // 模拟发送一个消息(异步)
        Future<Object> f = Patterns.ask(ask_ar, "Where are you doing now?", timeout);
        System.out.println("ask......");
        // 拿到返回结果
        f.onSuccess(new OnSuccess<Object>() {
            @Override
            public void onSuccess(Object result) throws Throwable {
                System.out.println("收到消息：" + result);
            }
        }, system.dispatcher());
        System.out.println("continue......");
    }
}

class AskActorDemo extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(this.getContext().system(), this);

    @Override
    public void onReceive(Object message) {
        if (message instanceof String) {
            // 请求
            System.out.println("发送者是：" + getSender() + " 发送消息：" + message); // getSender方法可以获得发送方
            // 响应
            getSender().tell("I'm coding!", getSelf()); //getSelf方法可以获得Actor自己
        } else {
            unhandled(message);
        }
    }
}