package lab5;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.*;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;
import akka.japi.Pair;
import akka.util.ByteString;
import org.asynchttpclient.AsyncHttpClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.lang.System.*;
import static org.asynchttpclient.Dsl.asyncHttpClient;

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

    public Flow<HttpRequest, HttpResponse, NotUsed> createRoute() throws IOException {
        Flow<HttpRequest, HttpResponse, NotUsed> flow = Flow.of(HttpRequest.class).map(
                req -> {
                    final int countInt;
                    int countInt1;
                    String url = req.getUri().query().get("testUrl").orElse("");
                    String count = req.getUri().query().get("count").orElse("");
                    try {
                        countInt1 = Integer.parseInt(count);
                    } catch (NumberFormatException e) {
                        out.println("Incorrect count value");
                        countInt1 = 0;
                    }
                    countInt = countInt1;
                    Pair<String, Integer> data = new Pair<>(url, countInt);
                    Source<Pair<String, Integer>, NotUsed> source = Source.from(Collections.singletonList(data));

                    Flow<Pair<String, Integer>, HttpResponse, NotUsed> testSink = Flow.<Pair<String, Integer>>create().map(
                            pair -> new Pair<>(HttpRequest.create().withUri(pair.first()), pair.second())
                    ).mapAsync(5, pair -> Patterns.ask(
                            storage,
                            new GetTest(new javafx.util.Pair<>(data.first(), data.second())),
                            Duration.ofMillis(3000)
                            ).thenCompose(
                            response -> {
                                if ((int) response != -1){
                                    return CompletableFuture.completedFuture((int) response);
                                }
                                return returnSource(pair);
                            }).thenCompose(
                            sum -> {
                                Patterns.ask(storage, new GetUrlTime(new javafx.util.Pair<String, javafx.util.Pair<Integer, Integer>>(data.first(), (new javafx.util.Pair<Integer, Integer>(data.second(), (int) sum)))), 5000);
                                Double avgTime = (double) sum / (double) countInt;
                                return CompletableFuture.completedFuture(HttpResponse.create().withEntity(ByteString.fromString(avgTime.toString())));
                            }
                            )
                    );
                    CompletionStage<HttpResponse> result = source.via(testSink).toMat(Sink.last(), Keep.right()).run(actorMaterializer);
                    return result.toCompletableFuture().get();
                });
        return flow;
    }


    public CompletionStage<Integer> returnSource(Pair<HttpRequest, Integer> pair){
        return Source.from(Collections.singletonList(pair)).toMat(
                Flow.<Pair<HttpRequest, Integer>>create().mapConcat(p -> Collections.nCopies(p.second(), p.first()))
                        .mapAsync(
                                1,
                                req2 -> CompletableFuture.completedFuture(System.currentTimeMillis())
                                        .thenCompose(start ->
                                                CompletableFuture.supplyAsync(
                                                        () -> {
                                                            CompletionStage<Long> whenResponse = asyncHttpClient
                                                                    .prepareGet(req2.getUri().toString())
                                                                    .execute().toCompletableFuture()
                                                                    .thenCompose(
                                                                            answer -> CompletableFuture.completedFuture(System.currentTimeMillis() - start));
                                                            return whenResponse;
                                                        }
                                                ))
                        ).toMat(getFold(), Keep.right()), Keep.right()).run(actorMaterializer);
    }

    public Sink<CompletionStage<Long>, CompletionStage<Integer>> getFold() {
        return Sink.fold(0, (ac, el) -> {
            int castEl = (int) (0 + el.toCompletableFuture().get());
            return ac + castEl;
        });
    }
}
