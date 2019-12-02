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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.collection.IntObjectHashMap;
import akka.japi.Pair
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
        return Flow.of(HttpRequest.class).map(
                req -> {
                    int countInt = new Integer(0);
                    String url = req.getUri().query().get("testUrl").orElse("");
                    String count = req.getUri().query().get("count").orElse("");
                    try {
                        countInt = Integer.parseInt(count);
                    } catch (NumberFormatException e){
                        System.out.println("Incorrect count value");
                    }
                    Pair<String, Integer> data = new Pair<>(url, countInt);
                    Source<Pair<String, Integer>, NotUsed> source = Source.from(Collections.singletonList(data));

                    Flow<Pair<String, Integer>, HttpResponse, NotUsed> testSink = Flow.<Pair<String, Integer>>create().map(
                            pair -> new Pair<>(HttpRequest.create().withUri(pair.first()), pair.second())
                    ).mapAsync(1, a -> Patterns.ask(
                            actor,
                            new GetTest(new javafx.util.Pair<>(data.first(), data.second())),
                            Duration.ofMillis(3000)
                    ).thenCompose(
                            response -> {
                                if ((int) response != -1){
                                    return CompletableFuture.completedFuture((int) response);
                                }
                                return Source.from(Collections.singletonList())
                            }
                    ))

                }
        )
    }
}
