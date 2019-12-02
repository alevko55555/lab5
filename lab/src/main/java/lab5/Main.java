package lab5;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import org.asynchttpclient.AsyncHttpClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class Main {
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        System.out.println("start!");
        ActorSystem system = ActorSystem.create("routes");
        final Http http = Http.get(system);
        ActorRef storage = system.actorOf(Props.create(ActorTestResult.class));
        final ActorMaterializer materializer =
                ActorMaterializer.create(system);
        final AsyncHttpClient asyncHttpClient = asyncHttpClient();
        final FlowWorkNode workNode = new FlowWorkNode(asyncHttpClient, system, materializer);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = Flow.of(HttpRequest.class).map(
                req -> {
                    final int countInt;
                    int countInt1;
                    String url = req.getUri().query().get("testUrl").orElse("");
                    String count = req.getUri().query().get("count").orElse("");
                    try {
                        countInt1 = Integer.parseInt(count);
                    } catch (NumberFormatException e) {
                        System.out.println("Incorrect count value");
                        countInt1 = 0;
                    }
                    countInt = countInt1;
                    Pair<String, Integer> data = new Pair<>(url, countInt);
                    Source<Pair<String, Integer>, NotUsed> source = Source.from(Collections.singletonList(data));

                    Flow<Pair<String, Integer>, HttpResponse, NotUsed> testSink = Flow.<Pair<String, Integer>>create().map(
                            pair -> new Pair<>(HttpRequest.create().withUri(pair.first()), pair.second())
                    ).mapAsync(1, pair -> Patterns.ask(
                            storage,
                            new GetTest(new javafx.util.Pair<>(data.first(), data.second())),
                            Duration.ofMillis(3000)
                            ).thenCompose(
                            response -> {
                                if ((int) response != -1) {
                                    return CompletableFuture.completedFuture(response);
                                }
                                Sink<CompletionStage<Long>, CompletionStage<Integer>> fold = Sink.fold(0, (ac, el) -> {
                                        int castEl = (int) (0 + el.toCompletableFuture().get());
                                        return ac + castEl;
                                    });
                                return Source.from(Collections.singletonList(pair)).toMat(Flow.<Pair<HttpRequest, Integer>>create().mapConcat(cp -> Collections.nCopies(cp.second(), cp.first())).mapAsync(
                                        1, req2 -> CompletableFuture.supplyAsync(
                                                () -> System.currentTimeMillis()
                                        ).thenCompose(
                                                start -> CompletableFuture.supplyAsync(
                                                        () -> {
                                                            CompletionStage<Long> responseServer = asyncHttpClient().prepareGet(req2.getUri().toString()).execute().toCompletableFuture().thenCompose(
                                                                    answer -> CompletableFuture.completedFuture(System.currentTimeMillis() - start)
                                                            );
                                                            return responseServer;
                                                        })))
                                        .toMat(fold, Keep.right()), Keep.right()).run(materializer);
                            }).thenCompose(
                            sum -> {
                                Patterns.ask(storage, new GetUrlTime(new javafx.util.Pair<String, javafx.util.Pair<Integer, Integer>>(data.first(), (new javafx.util.Pair<Integer, Integer>(data.second(), (int)sum)))), 5000);
                                Double avgTime = (double) sum / (double) countInt;
                                return CompletableFuture.completedFuture(HttpResponse.create().withEntity(ByteString.fromString(avgTime.toString())));
                            }
                            )
                    );
                    CompletionStage<HttpResponse> result = source.via(testSink).toMat(Sink.last(), Keep.right()).run(materializer);
                    return result.toCompletableFuture().get();
                }); //<вызов метода которому передаем Http, ActorSystem и ActorMaterializer>;
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", SERVER_PORT),
                materializer
        );
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> {
                    system.terminate();
                    try {
                        asyncHttpClient.close();
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }); // and shutdown when done
    }
}
