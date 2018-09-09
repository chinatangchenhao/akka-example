package com.dt.data.akka.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现轮询投递
 */
public class RoutingRoundRobin {

    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create("sys");

        ActorRef routerActor = system.actorOf(Props.create(RouterTaskActor.class),"routerActor");

        for(int i = 0; i < 4; i++) {
            routerActor.tell("Hello" + i, ActorRef.noSender());
        }
    }
}

/**
 * 定义路由器
 */
class RouterTaskActor extends UntypedActor {

    private Router router;

    @Override
    public void preStart() throws Exception {
        List<Routee> listRoutee = new ArrayList<Routee>();
        /**
         * 创建两个Routee
         */
        for (int i = 0; i < 2; i++) {
            // RouteeActor不能直接使用，必须通过Routee的子类ActorRefRoutee进行包装
            listRoutee.add(new ActorRefRoutee(
               getContext().actorOf(Props.create(RouteeActor.class), "routeeActor" + i)
            ));
        }
        /**
         * RoundRobinRoutingLogic为实现好的轮询投递策略
         * Akka提供了很多策略：
         *     RandomRoutingLogic
         *     BroadcastRouyingLogic
         *     ScatterGatherFirstCompletedRoutingLogic
         */
        router = new Router(new RoundRobinRoutingLogic(), listRoutee);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        router.route(message, getSender());
    }
}

/**
 * 定义路由目标
 */
class RouteeActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(this.getContext().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
        log.info(getSelf() + "-->" + message);
    }
}
