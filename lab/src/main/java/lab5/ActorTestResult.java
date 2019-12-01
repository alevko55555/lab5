package lab5;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.HashMap;

public class ActorTestResult extends AbstractActor {
    private HashMap<GetTest, Integer> storage;
    public void TestStorage() {
        this.storage = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetTest.class, this::createReceive)
                .match(GetUrlTime.class,
                        msg -> storage.put(msg.getTest(), msg.getNum()))
                .build();
    }

    public void receiveTest(GetTest msg) {
        getSender().tell(
                new MessageUrlTime(new GetUrlTime(msg, storage.get(msg))),
                ActorRef.noSender()
        );
    }
}
