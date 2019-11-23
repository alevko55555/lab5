package lab5;

import akka.actor.ActorRef;
import akka.stream.ActorMaterializer;
import org.asynchttpclient.AsyncHttpClient;

public class FlowWorkNode {
    private final AsyncHttpClient asyncHttpClient;
    private final ActorRef storage;
    private final ActorMaterializer actorMaterializer;

    public FlowWorkNode(AsyncHttpClient asyncHttpClient, ActorRef storage, ActorMaterializer actorMaterializer) {
        this.asyncHttpClient = asyncHttpClient;
        this.storage = storage;
        this.actorMaterializer = actorMaterializer;
    }
}
