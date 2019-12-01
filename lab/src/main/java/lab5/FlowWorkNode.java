package lab5;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.*;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.collection.IntObjectHashMap;
import javafx.util.Pair;
import org.asynchttpclient.AsyncHttpClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
                    String url = req.getUri().query().get("testUrl").orElse("");
                    String count = req.getUri().query().get("count").orElse("");
                    Integer countInt = Integer.parseInt(count);
                    Pair<String, Integer> newReq = new Pair<>(url, countInt);
                    Flow<Pair<String, Integer>, HttpResponse, NotUsed> testSink =
                            Flow.<Pair<String, Integer>>create()
                                    .map(pair -> new Pair<>(HttpRequest.create().withUri(pair.getKey()), pair.getValue()))
                                    .mapAsync()
                                    .map(result -> {
                                        storage.tell(result, ActorRef.noSender());
                                        return HttpResponse.create()
                                                .withStatus(StatusCodes.OK)
                                                .withEntity(ContentTypes.APPLICATION_JSON, ByteString.fromString(
                                                        new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(result)
                                                ));
                                    })
                })
    }
}
