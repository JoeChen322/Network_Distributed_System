# Evaluation lab - Contiki-NG

## Group number: 06

## Group members

- Jonathan Sciarrabba
- Zhu Xiong
- Zhaochen Qiao

## Solution description

**In the `common.h` header file**, we define a struct `message_t` which contains to field `type` and `val`, the first means:

````c++
NO_OP		\\  when the resource is already locked, and no mesg sent
ERROR 		\\ server-->client the resource is locked by other client
OK			\\ server-->client request got reply successfully
LOCK		\\ client->server
WRITRE		\\ client->server
READ		\\ client->server
````

The `val` field simply contains the value for `WRITE` and `READ` operations.

##### Server (`udp-server.c`)

53-56 		\\\function to unlock 

58-73		\\\function called when the request of `LOCK` is received

75-89		\\\function called when the request of `WRITE` is received

91-97		\\\function called when the request of `READ` is received

102-138	   \\\msg received callback 

##### Client 1 (`udp-client1.c`)

It simulates a series of requests in the order : `LOCK-WRITE-READ`. It goes on the next request after receiving an `OK` from the server. It waits a variable amount of time (from 0s to 20s) before issuing the next series of requests.

##### Client 2 (`udp-client2.c`)

It simulates random requests every random delay (from 0s to 5s) .

