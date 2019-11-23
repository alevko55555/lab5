package lab5;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
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

    public Flow<HttpRequest, HttpResponse, NotUsed> createRoute() {
        return Flow.of(HttpRequest.class)
                .map();
    }
}
