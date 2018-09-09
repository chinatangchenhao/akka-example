package com.dt.data.akka.actor;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * 停止Actor
 */
public class ActorTerminate {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        ActorRef watchActor = system.actorOf(Props.create(WatchActor.class), "watchActor");
        watchActor.tell("stopChild", ActorRef.noSender());
        //watchActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
    }
}

/**
 * 定义一个父级Actor
 */
class WatchActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(this.getContext().system(), this);

    ActorRef child = null;

    @Override
    public void preStart() throws Exception {
        // 创建子级Actor
        child = getContext().actorOf(Props.create(ChildActor.class), "childActor");
        // 监控子级Actor
        getContext().watch(child);
        //
        log.info("watchActor preStart...");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof String) {
            if (message.equals("stopChild")) {
                getContext().stop(child);
            }
        } else if (message instanceof Terminated) {
            Terminated t = (Terminated)message;
            log.info("监控到" + t.getActor() + "停止了");
        } else {
            unhandled(message);
        }
    }

    @Override
    public void postStop() throws Exception {
        log.info("WatchActor postStop...");
    }
}

/**
 * 定义一个子级Actor
 */
class ChildActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(this.getContext().system(), this);

    @Override
    public void preStart() throws Exception {
        log.info("ChildActor preStart...");
    }

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("收到消息: " + message);
    }

    @Override
    public void postStop() throws Exception {
        log.info("ChildActor postStop...");
    }
}