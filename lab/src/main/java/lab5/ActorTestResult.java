package lab5;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.HashMap;
import java.util.Map;

public class ActorTestResult extends AbstractActor {
    private HashMap<String, Map<Integer, Integer>> storage = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder().create()
                .match(GetTest.class, test -> {
                    String url = test.getUrl();
                    Integer count = test.getNum();
                    if(storage.containsKey(url) && storage.get(url).containsKey(count)) {
                        getSender().tell(storage.get(url).get(count), ActorRef.noSender());
                    } else {
                        getSender().tell(-1, ActorRef.noSender());
                    }
                })
                .match(GetUrlTime.class, test -> {
                    Map<Integer, Integer> store;
                    if(storage.containsKey(test.getUrl())) {
                        store = storage.get(test.getUrl());
                    } else {
                        store = new HashMap<>();
                    }
                    store.put(test.getCount(), test.getTime());
                    store.put(test.getUrl(), store);
                })
                .build();
    }
}
