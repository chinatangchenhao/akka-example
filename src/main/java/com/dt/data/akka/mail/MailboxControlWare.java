package com.dt.data.akka.mail;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.ControlMessage;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * 控制指令优先
 */
public class MailboxControlWare {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");

        ActorRef controlWareActor = system.actorOf(
                Props.create(ControlWareActor.class).withMailbox("control-aware-mailbox"),
                "controlWareActor");
        Object[] messages = {"Spark", new ControlMsg("Hadoop"), "Flink"};
        for(Object msg : messages) {
            controlWareActor.tell(msg, ActorRef.noSender());
        }
    }
}

class ControlWareActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(this.getContext().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("收到消息: " + message);
    }
}

/**
 * 定义具有高优先级的消息
 *
 * 注意：这里的ControlMessage接口仅仅是一个标识
 */
class ControlMsg implements ControlMessage {

    private final String msg;

    public ControlMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return  this.msg;
    }
}
