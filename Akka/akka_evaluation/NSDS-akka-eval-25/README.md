# Evaluation lab - Akka

## Group number: 06

## Group members

- Jonathan Sciarrabba 10675342 
- QIAO ZHAOCHEN 11021721
- XIONG ZHU 11035936

## Description of message flows
First of all we create the Actors (Client, Balancer, Workers), then we configure Client and Balancer with `ClientConfigMsg` and `BalancerConfigMsg` respectively.

Client send `PutMsg` to insert a name and email inside the replicated AdressBook, then the Balancer sends `PutMsgPrimary` and `PutMsgReplica` to these workers based on the result of the function `splitByIntial()`.

When the Client asks a `GetMsg`, the Balancer first asks the primary copy based on `splitByIntial()`.
If the primary copy is in working mode , it will send `GetMsgReply` to the Balancer and it replies the same `GetMsgReply` to the Client. If the primary copy is in resting mode, the Balancer awaiting the future receive a timeout by the Ask Pattern and it sends again `GetMsg` to the replica copy. If the replica copy is at working mode, it will send `GetMsgReply` to the Balancer and the Balancer sends `GetMsgReply` to the Client. If the replica copy is in resting mode, the Balancer awaiting the future receive a timeout by the Ask Pattern and send `TimeoutMsg` to the client.

If a worker in working mode receives a `RestMsg`， it switches to resting mode.

If a worker in resting mode receives a `ResumeMsg`， it switches to working mode.