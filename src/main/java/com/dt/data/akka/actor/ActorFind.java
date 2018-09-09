package com.dt.data.akka.actor;

import akka.actor.*;

/**
 * 查找Actor
 */
public class ActorFind {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        ActorRef lookupActor = system.actorOf(Props.create(LookupActor.class), "lookupActor");
        lookupActor.tell("find", ActorRef.noSender());
    }
}

/**
 * 定义目标Actor
 */
class BassActor extends UntypedActor {

    @Override
    public void onReceive(Object message) {
        System.out.println("target receive: " + message);
    }
}

/**
 * 定义用于查找的Actor
 */
class LookupActor extends UntypedActor {

    /**
     * 初始化目标接受者的ActorRef
     */
    private ActorRef bass = null;

    {
        bass = getContext().actorOf(Props.create(BassActor.class), "bassActor");
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof String) {
            if ("find".equals(message)) {
                /**
                 * 获取ActorSelection对象，需要传递一个Path
                 * 这里由于BassActor是LookupActor的子级，path可以直接填写相对路径
                 * 也可以填写绝对路径/user/lookupActor/bassActor
                 */
                ActorSelection as = getContext().actorSelection("bassActor");
                /**
                 *  向ActorSelection发送Identify 这是一个messageId 用来区分查找不同的Actor
                 */
                as.tell(new Identify("A001"), getSelf());
            }
        } else if (message instanceof ActorIdentity) {
            /**
             * 当前Actor会自动收到一个ActorIdentity对象，通过getRef方法得到ActorRef
             */
            ActorIdentity ai = (ActorIdentity) message;
            if (ai.correlationId().equals("A001")) {
                ActorRef ref = ai.getRef();
                if (null != ref) {
                    System.out.println("ActorIdentity:" + ai.correlationId() + " " + ref);
                    ref.tell("hello bassActor", getSelf());
                }
            }
        } else {
            unhandled(message);
        }
    }
}
