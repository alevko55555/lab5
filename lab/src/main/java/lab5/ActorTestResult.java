package lab5;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.HashMap;

public class ActorTestResult extends AbstractActor {
    private HashMap<GetTest, Long> storage = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetTest.class,
                        msg -> {
                    //String url = msg.getUrl();
                    //int count = msg.getNum();
                    getSender().tell(
                                new MessageUrlTime(new GetUrlTime(msg, storage.get(msg))),
                                ActorRef.noSender()
                        );})
                .match(GetUrlTime.class,
                        msg -> storage.put(msg.getTest(), msg.getNum()))
                .build();
    }
}
