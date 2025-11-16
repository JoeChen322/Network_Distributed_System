//package sensor;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import akka.actor.ActorRef;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

public class processorsActor extends AbsractActorWIthStash {
   
	private final Map<String,String> addressBook=new HashMap<>();
@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(putMsg.class, this::onPutMessage)
			.match(getMsg.class, this::onGetMessage)
			.build();
	}
	void onPutMessage(putMsg msg) {
        String address = msg.getAddress();
		String name = msg.getName();
        if(name=="Fault")
        {
            System.out.println("Something Wrong");
            throw new RuntimeException("Server failing");
        }
        else{
            addressBook.put(name, address);
        System.out.println("Added to address book: " + "name:"+name+" address:"+address);
        unstashAll();
        }
        
    }

    void onGetMessage(getMsg msg) {
        String email = addressBook.get(msg.getName());
        if (email == null) {
            stash();
            return;
        }

        sender().tell(new replyMsg(email),ActorRef.noSender());
	}

    static Props props() {
        return Props.create(com.mailbox.serverActor.class);
    }
}
