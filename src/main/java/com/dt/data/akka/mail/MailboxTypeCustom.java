package com.dt.data.akka.mail;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Envelope;
import akka.dispatch.MailboxType;
import akka.dispatch.MessageQueue;
import akka.dispatch.ProducesMessageQueue;
import com.typesafe.config.Config;
import scala.Option;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MailboxTypeCustom {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("sys");

        ActorRef controlWareActor = system.actorOf(
                Props.create(ControlWareActor.class).withMailbox("business-mailbox"),
                "controlWareActor");

        Object[] messages = {"Spark", "Hadoop", "Flink"};
        for(Object msg : messages) {
            controlWareActor.tell(msg, ActorRef.noSender());
        }
    }
}

/**
 * 自定义邮箱队列（实现MessageQueue接口）
 */
class BusinessMsgQueue implements MessageQueue {

    private Queue<Envelope> queue = new ConcurrentLinkedQueue<Envelope>();

    /**
     * 投递消息的过程中Actor不可用，没有投递成功的消息会进入死信队列
     *
     * @param owner
     * @param deadLetters
     */
    public void cleanUp(ActorRef owner, MessageQueue deadLetters) {
        for (Envelope ele : queue) {
            deadLetters.enqueue(owner, ele);
        }
    }

    /**
     * 消息出队
     *
     * @return
     */
    public Envelope dequeue() {
        return queue.poll();
    }

    /**
     * 消息入队
     *
     * @param receiver
     * @param envelope
     */
    public void enqueue(ActorRef receiver, Envelope envelope) {
        queue.offer(envelope);
    }

    public boolean hasMessages() {
        return !queue.isEmpty();
    }

    public int numberOfMessages() {
        return queue.size();
    }
}

/**
 * 定义邮箱类型
 */
class BusinessMailBoxType implements MailboxType, ProducesMessageQueue<BusinessMsgQueue> {

    public BusinessMailBoxType(ActorSystem.Settings settings, Config config) {

    }

    public MessageQueue create(Option<ActorRef> owner, Option<ActorSystem> system) {
        return new BusinessMsgQueue();
    }
}
