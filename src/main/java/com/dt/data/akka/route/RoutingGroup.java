package com.dt.data.akka.route;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.FromConfig;

/**
 * 路由Actor Pool方式
 */
public class RoutingGroup {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");

        ActorRef masterActor = system.actorOf(Props.create(MasterActor.class),"masterActor");

        masterActor.tell("helloA", ActorRef.noSender());
        masterActor.tell("helloB", ActorRef.noSender());
        masterActor.tell("helloC", ActorRef.noSender());
    }
}

/**
 * 定义路由Actor
 */
class MasterActor extends UntypedActor {

    private ActorRef router = null;

    /**
     * 创建路由Actor
     *
     * @throws Exception
     */
    @Override
    public void preStart() throws Exception {
        //定义Routee
        getContext().actorOf(Props.create(WorkActor.class), "wt1");
        getContext().actorOf(Props.create(WorkActor.class), "wt2");
        getContext().actorOf(Props.create(WorkActor.class), "wt3");

        router = getContext().actorOf(
                FromConfig.getInstance().props(), "router");

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
class WorkActor extends UntypedActor {

    @Override
    public void onReceive(Object message) throws Exception {
        System.out.println(getSelf() + "->" + message + "-->" + getContext().parent());
    }
}
