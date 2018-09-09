package com.dt.data.akka.mail;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.PriorityGenerator;
import akka.dispatch.UnboundedStablePriorityMailbox;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;

/**
 * 自定义优先级
 */
public class MailboxPriorityCustom {

    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create("sys");

        ActorRef ref = system.actorOf(
                //withMailbox 加载配置文件中的配置
                Props.create(MsgProActor.class).withMailbox("msgprio-mailbox"),
                "msgProActor");
        Object[] messages = {"Spark", "Hadoop", "Flink"};
        for(Object msg : messages) {
            ref.tell(msg, ActorRef.noSender());
        }
    }
}

/**
 * 自定义优先级
 */
class MsgPriorityMailBox extends UnboundedStablePriorityMailbox {
    public MsgPriorityMailBox(ActorSystem.Settings settings, Config config) {
        super(new PriorityGenerator() {
            /**
             * 返回值越小优先级越高
             *
             * @param message
             * @return
             */
            @Override
            public int gen(Object message) {
                if (message.equals("Flink")) {
                    return 0;
                } else if (message.equals("Spark")) {
                    return 1;
                } else if (message.equals("Hadoop")) {
                    return 2;
                } else {
                    return 3;
                }
            }
        });
    }
}

class MsgProActor extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(this.getContext().system(), this);

    @Override
    public void onReceive(Object message) throws Exception {
        log.info("收到消息: " + message);
    }
}

