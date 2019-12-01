package lab5;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.*;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.collection.IntObjectHashMap;
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
                    String url = req.
                    String count = req.getUri().query().get("count").orElse("");
                    Integer countInt = Integer.parseInt(count);

                })
                .mapAsync()
                .map(httpresponse -> HttpResponse.create()
                        .withStatus(StatusCodes.OK)
                        .withEntity(ContentTypes.APPLICATION_JSON, ByteString.fromString(
                                new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(httpresponse)
                        )));
    }
}
