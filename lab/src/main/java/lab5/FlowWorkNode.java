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
                    Pair<String, Integer> pair = new Pair<>(url, countInt);
                    return new GetTest(pair);
                })
                .mapAsync(5, pair -> Patterns.ask(storage, pair, Duration.ofSeconds(5))
                        .thenApply(o -> (MessageUrlTime)o)
                        .thenCompose(res -> {
                            Optional<GetUrlTime> resOptional = res.getUrlTimeOptional();
                            if(resOptional.isPresent()) {
                                return CompletableFuture.completedFuture(resOptional.get());
                            } else {
                                return test -> {
                                    final Sink<GetTest, CompletionStage<Integer>> testSink =
                                          Flow.of(GetTest.class)
                                                  .mapConcat(o -> Collections.nCopies(o.getNum(), o.getUrl()))
                                                  .mapAsync(5, url -> {
                                                      Instant start = Instant.now();
                                                      return asyncHttpClient.prepareGet(url).execute()
                                                              .toCompletableFuture()
                                                              .thenCompose(answer -> CompletableFuture.completedFuture(
                                                                      Duration.between(start, Instant.now()).getSeconds()
                                                          ));
                                                  })
                                                  .toMat(Sink.fold(0, Integer::sum), Keep.right());
                                    return Source.from(Collections.singleton(test))
                                            .toMat(testSink, Keep.right())
                                            .run(actorMaterializer)
                                            .thenApply(sum -> new GetUrlTime(test, sum/));
                                }
                            }
                        }))
                .map(httpresponse -> HttpResponse.create()
                        .withStatus(StatusCodes.OK)
                        .withEntity(ContentTypes.APPLICATION_JSON, ByteString.fromString(
                                new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(httpresponse)
                        )));
    }
}
