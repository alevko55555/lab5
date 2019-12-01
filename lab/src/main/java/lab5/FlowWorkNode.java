package lab5;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Query;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import javafx.util.Pair;
import org.asynchttpclient.AsyncHttpClient;

import java.util.Optional;

public class FlowWorkNode {
    private final AsyncHttpClient asyncHttpClient;
    //private final ActorSystem system;
    private final ActorRef storage;
    private final ActorMaterializer actorMaterializer;

    public FlowWorkNode(AsyncHttpClient asyncHttpClient, ActorSystem system, ActorMaterializer actorMaterializer) {
        this.asyncHttpClient = asyncHttpClient;
        this.storage = system.actorOf(Props.create(ActorTestResult.class));
        this.actorMaterializer = actorMaterializer;
    }

    public Flow<HttpRequest, HttpResponse, NotUsed> createRoute() {
        return Flow.of(HttpRequest.class)
                .map(req -> {
                    Query query = req.getUri().query();
                    Optional<String> testUrl = query.get("testUrl");
                    Optional<String> num = query.get("num");
                    Pair<String, Integer> pair = Pair <testUrl.get()>;
                    return new GetTest(pair);
                })
                .mapAsync()
                .map();
    }
}
