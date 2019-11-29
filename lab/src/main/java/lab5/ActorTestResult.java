package lab5;

import akka.actor.AbstractActor;

import java.util.HashMap;

public class ActorTestResult extends AbstractActor {
    private HashMap<GetTest, Integer> storage;
    public void TestStorage() {
        this.storage = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetTest.class,
                        msg -> getSender().tell(
                                new GetUrlTime(new )
                        ));
    }
}
