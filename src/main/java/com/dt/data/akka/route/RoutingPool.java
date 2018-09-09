package com.dt.data.akka.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.FromConfig;
import akka.routing.RoundRobinPool;

/**
 * 路由Actor Pool方式
 */
public class RoutingPool {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");

        ActorRef masterRouterActor = system.actorOf(Props.create(MasterRouterActor.class),"masterRouterActor");

        masterRouterActor.tell("helloA", ActorRef.noSender());
        masterRouterActor.tell("helloB", ActorRef.noSender());
        masterRouterActor.tell("helloC", ActorRef.noSender());
    }
}

/**
 * 定义路由Actor
 */
class MasterRouterActor extends UntypedActor {

    private ActorRef router = null;

    /**
     * 创建路由Actor的两种方式
     *
     * @throws Exception
     */
    @Override
    public void preStart() throws Exception {
        /**
         * 创建方式一：
         *
         *    通过编写配置路由Actor
         *    RoundRobinPool定义了pool类型路由器并指定RouteeActor，这些RouteeActor将自动成为路由器的子级
         */
        /*router = getContext().actorOf(
                new RoundRobinPool(3).props(Props.create(TaskActor.class)), "taskActor");*/

        /**
         * 创建方式二：
         *
         * 通过配置文件配置路由Actor
         *
         * akka.actor.deployment {
         *    /masterRouterActor/taskActor {
         *       router = round-robin-pool
         *       nr-of-instance = 3
         *    }
         * }
         *
         * router表示路由类型
         * nr-of-instance表示Routee池大小
         */
        router = getContext().actorOf(
                FromConfig.getInstance().props(Props.create(TaskActor.class)), "taskActor");

        System.out.println("router:"+router);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        router.tell(message, getSender());
    }
}

/**
 * 定义路由目标
 */
class TaskActor extends UntypedActor {

    @Override
    public void onReceive(Object message) throws Exception {
        System.out.println(getSelf() + "->" + message + "-->" + getContext().parent());
    }
}
