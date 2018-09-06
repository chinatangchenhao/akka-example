package com.dt.data.akka.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * 创建和使用Actor
 */
public class UntypedActorCreator {
    public static void main(String[] args) {
        /**
         * 通过ActorSystem创建的是一个顶级的Actor(即/user路径下)
         */
        ActorSystem system = ActorSystem.create("sys");
        /**
         * 通过该方法创建一个Actor的引用 ActorRef，我们只能通过ActorRef来进行消息通信
         */
        ActorRef actorRef = system.actorOf(Props.create(ActorDemo.class), "actorDemo");
        /**
         * 向Actor发送消息
         */
        actorRef.tell("Hello Akka", ActorRef.noSender());
    }
}

class ActorDemo extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(this.getContext().system(), this);

    /**
     * 这个方法是用于接受并处理消息的方法
     *
     * @param message
     */
    @Override
    public void onReceive(Object message) {
        if (message instanceof String) {
            log.info(message.toString());
        } else {
            unhandled(message);
        }
    }
}


