package com.lab.evaluation25;

import static java.util.concurrent.TimeUnit.SECONDS;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AddressBookClientActor extends AbstractActor {

    private ActorRef balancer = null;
	private scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(3, SECONDS);

	@Override
	public Receive createReceive() {
		return receiveBuilder()
                .match(PutMsg.class, this::putEntry)
                .match(GetMsg.class, this::query)
                .match(ClientConfigMsg.class, this::onConfigMsg)
                .build();
	}

    void onConfigMsg(ClientConfigMsg clientConfigMsg) {
        balancer = clientConfigMsg.getBalancer();
    }

	void putEntry(PutMsg msg) {
		System.out.println("CLIENT: Sending new entry " + msg.getName() + " - " + msg.getEmail());
        balancer.tell(msg, getSelf());
	}

	void query(GetMsg msg) {
		System.out.println("CLIENT: Issuing query for " + msg.getName());

        Future<Object> getMsgReplyFuture = Patterns.ask(balancer, msg, 2000);
        try {
            Object o = getMsgReplyFuture.result(Duration.create(2, TimeUnit.SECONDS), null);
            if (o instanceof GetMsgReply) {
                GetMsgReply reply = (GetMsgReply) o;
                if (reply.getEmail() == null) {
                    System.out.println("CLIENT: Received reply, no email found!");
                } else {
                    System.out.println("CLIENT: Received reply!");
                    System.out.println("CLIENT: " +reply.getName() + " - " + reply.getEmail());
                }
            } else if (o instanceof TimeoutMsg) {
                System.out.println("CLIENT: Received timeout, both copies are resting!");
            }
        } catch (TimeoutException e) {
            System.out.println("CLIENT: timeout expired, no one responding!");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	static Props props() {
		return Props.create(AddressBookClientActor.class);
	}

}
