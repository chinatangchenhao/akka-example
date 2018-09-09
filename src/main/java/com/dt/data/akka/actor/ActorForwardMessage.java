package com.dt.data.akka.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * 消息转发
 *
 * 使用场景：实现消息路由
 */
public class ActorForwardMessage {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        ActorRef forwardActor = system.actorOf(Props.create(ForwardActor.class), "forwardActor");
        forwardActor.tell("Hello TargetActor", ActorRef.noSender());
    }
}

/**
 * 定义目标接受者
 */
class TargetActor extends UntypedActor {

    @Override
    public void onReceive(Object message) {
        System.out.println("TargetActor receive: " + message + ", sender = " + getSender());
    }
}

/**
 * 定义转发者
 */
class ForwardActor extends UntypedActor {

    /**
     * 初始化目标接受者的ActorRef
     */
    private ActorRef target = getContext().actorOf(Props.create(TargetActor.class), "targetActor");

    @Override
    public void onReceive(Object message) {
        // 转发消息
        target.forward(message, getContext());
    }
}