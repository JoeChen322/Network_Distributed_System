package com.lab.evaluation25;

import akka.actor.ActorRef;

public class ClientConfigMsg {
    private final ActorRef balancer;

    public ClientConfigMsg(ActorRef balancer) {
        this.balancer = balancer;
    }

    public ActorRef getBalancer() {
        return balancer;
    }
}
