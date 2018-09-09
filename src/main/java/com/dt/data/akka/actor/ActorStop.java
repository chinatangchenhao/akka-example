package com.dt.data.akka.actor;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * 停止Actor
 */
public class ActorStop {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");
        ActorRef workActor = system.actorOf(Props.create(WorkActor.class), "workActor");
        //system.stop(workActor);
        //workActor.tell(PoisonPill.getInstance(), ActorRef.noSender());
        workActor.tell(Kill.getInstance(), ActorRef.noSender());
    }
}

class WorkActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(this.getContext().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("收到消息: " + message);
    }

    @Override
    public void postStop() throws Exception {
        log.info("WorkActor postStop...");
    }
}