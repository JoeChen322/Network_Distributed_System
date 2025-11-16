package com.lab.evaluation25;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AddressBookBalancerActor extends AbstractActor {

    ActorRef worker1 = null;
    ActorRef worker0 = null;

    public AddressBookBalancerActor() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PutMsg.class, this::storeEntry)
                .match(GetMsg.class, this::routeQuery)
                .match(BalancerConfigMsg.class, this::onConfigMsg)
                .build();
    }

    void onConfigMsg(BalancerConfigMsg balancerConfigMsg) {
        worker0 = balancerConfigMsg.getWorker0();
        worker1 = balancerConfigMsg.getWorker1();
    }

    int splitByInitial(String s) {
        char firstChar = s.charAt(0);

        // Normalize case for comparison
        char upper = Character.toUpperCase(firstChar);

        if (upper >= 'A' && upper <= 'M') {
            return 0;
        } else {
            return 1;
        }
    }

    void routeQuery(GetMsg msg) {
        System.out.println("BALANCER: Received query for name " + msg.getName());
        int workerId = splitByInitial(msg.getName());
        ActorRef primaryWorker = null;
        ActorRef replicaWorker = null;

        if (workerId == 0) {
            primaryWorker = worker0;
            replicaWorker = worker1;
        } else {
            primaryWorker = worker1;
            replicaWorker = worker0;
        }


        System.out.println("BALANCER: Primary copy query for name " + msg.getName());
        Future<Object> getMsgReplyFuture = Patterns.ask(primaryWorker, msg, 500);
        try {
            GetMsgReply reply = (GetMsgReply) getMsgReplyFuture.result(Duration.create(500, TimeUnit.MILLISECONDS), null);
            System.out.println("BALANCER: Primary copy query for name " + msg.getName() + " has replied!");
            getSender().tell(reply, getSelf());
        } catch (TimeoutException e) {
            System.out.println("BALANCER: Primary copy query for name " + msg.getName() + " is resting!");

            System.out.println("BALANCER: Replica copy query for name " + msg.getName());
            getMsgReplyFuture = Patterns.ask(replicaWorker, msg, 500);
            try {
                GetMsgReply reply = (GetMsgReply) getMsgReplyFuture.result(Duration.create(500, TimeUnit.MILLISECONDS), null);
                System.out.println("BALANCER: replica copy query for name " + msg.getName() + " has replied!");
                getSender().tell(reply, getSelf());
            } catch (TimeoutException e2) {
                System.out.println("BALANCER: Both copies are resting for name " + msg.getName() + "!");
                getSender().tell(TimeoutMsg.class, getSelf());
            } catch (Exception e2) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void storeEntry(PutMsg msg) {
        System.out.println("BALANCER: Received new entry " + msg.getName() + " - " + msg.getEmail());
        int worker = splitByInitial(msg.getName());

        if (worker == 0) {
            worker0.tell(new PutMsgPrimary(msg), getSelf());
            worker1.tell(new PutMsgReplica(msg), getSelf());
        } else {
            worker1.tell(new PutMsgPrimary(msg), getSelf());
            worker0.tell(new PutMsgReplica(msg), getSelf());
        }
    }

    static Props props() {
        return Props.create(AddressBookBalancerActor.class);
    }

}
