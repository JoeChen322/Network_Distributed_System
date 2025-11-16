package com.lab.evaluation25;

import java.util.HashMap;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class AddressBookWorkerActor extends AbstractActor {

    private HashMap<String, String> primaryAddresses;
    private HashMap<String, String> replicaAddresses;

    public AddressBookWorkerActor() {
        this.primaryAddresses = new HashMap<String, String>();
        this.replicaAddresses = new HashMap<String, String>();
    }

    public Receive resting() {
        return receiveBuilder()
                .match(PutMsgPrimary.class, this::onPutMsgPrimaryResting)
                .match(PutMsgReplica.class, this::onPutMsgReplicaResting)
                .match(GetMsg.class, this::onGetMsgResting)
                .match(ResumeMsg.class, this::onResumeMsg)
                .build();
    }

    public Receive working() {
        return receiveBuilder()
                .match(PutMsgPrimary.class, this::onPutMsgPrimary)
                .match(PutMsgReplica.class, this::onPutMsgReplica)
                .match(GetMsg.class, this::generateReply)
                .match(RestMsg.class, this::onRestMsg)
                .build();
    }

    @Override
    public Receive createReceive() {
        return working();
    }

    void onPutMsgPrimary(PutMsgPrimary putMsgPrimary) {
        System.out.println(this.toString() + ": Received PutMsgPrimary inserting " + putMsgPrimary.getName() + ", " + putMsgPrimary.getEmail());
        primaryAddresses.put(putMsgPrimary.getName(), putMsgPrimary.getEmail());
    }

    void onPutMsgReplica(PutMsgReplica putMsgReplica) {
        System.out.println(this.toString() + ": Received PutMsgReplica inserting " + putMsgReplica.getName() + ", " + putMsgReplica.getEmail());
        replicaAddresses.put(putMsgReplica.getName(), putMsgReplica.getEmail());
    }

    void onPutMsgPrimaryResting(PutMsgPrimary putMsgPrimary) {
        System.out.println(this.toString() + ": Received PutMsgPrimary but resting");
    }

    void onPutMsgReplicaResting(PutMsgReplica putMsgReplica) {
        System.out.println(this.toString() + ": Received PutMsgReplica but resting");
    }

    void onRestMsg(RestMsg restMsg) {
        System.out.println(this.toString() + ": Received RestMsg go to sleep...");
        getContext().become(resting());
    }

    void onResumeMsg(ResumeMsg resumeMsg) {
        System.out.println(this.toString() + ": Received ResumeMsg start working...");
        getContext().become(working());
    }

    void onGetMsgResting(GetMsg getMsg) {
        System.out.println(this.toString() + ": Received query for name " + getMsg.getName() + " but resting");
    }

    void generateReply(GetMsg msg) {
        System.out.println(this.toString() + ": Received query for name " + msg.getName());
        String email = primaryAddresses.get(msg.getName());

        if (email == null) {
            email = replicaAddresses.get(msg.getName());
        }

        getSender().tell(new GetMsgReply(msg.getName(), email), getSelf());
    }

    static Props props() {
        return Props.create(AddressBookWorkerActor.class);
    }
}
