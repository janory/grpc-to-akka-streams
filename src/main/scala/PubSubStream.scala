import java.util
import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Attributes, ThrottleMode}
import akka.stream.scaladsl.{Sink, Source}
import com.google.auth.oauth2.GoogleCredentials
import com.google.pubsub.v1.pubsub._
import io.grpc.ClientInterceptors
import io.grpc.auth.ClientAuthInterceptor
import io.grpc.netty.{NegotiationType, NettyChannelBuilder}

import scala.concurrent.duration._


/**
  * Created by janos on 10.11.17.
  */
object PubSubStream extends App {

  System.setProperty(
    "java.util.logging.config.file",
    "/Users/janos/Documents/workspaces/grpc-to-akka-streams/src/main/resources/logging.properties")

  implicit val system = ActorSystem("test")
  implicit val materializer = ActorMaterializer.create(system)

  val channel = NettyChannelBuilder
    .forAddress("pubsub.googleapis.com", 443)
    .negotiationType(NegotiationType.TLS)
    .build()

  val interc = new ClientAuthInterceptor(
    GoogleCredentials.getApplicationDefault.createScoped(
      util.Arrays.asList("https://www.googleapis.com/auth/pubsub")),
    Executors.newCachedThreadPool)

  private val channelWithAuth = ClientInterceptors.intercept(channel, interc)

  val stub = PubsubGrpcAkkaStream.stub(channelWithAuth)

//  private val stub = SubscriberGrpc.blockingStub(channelWithAuth)
//
//  val resp: PullResponse = stub.pull(
//    PullRequest(
//      String.format("projects/%s/subscriptions/%s",
//                    "crm360-degrees",
//                    "janos-subscription"), maxMessages = 1))
//
//  println(resp.receivedMessages)



  Source.single(StreamingPullRequest(subscription = String.format("projects/%s/subscriptions/%s",
                        "crm360-degrees",
                       "janos-subscription"), streamAckDeadlineSeconds = 10))
    .via(stub.streamingPull.addAttributes(Attributes.inputBuffer(initial = 1, max = 1)))

//    .log((resp: StreamingPullResponse) => resp.)
    .throttle(1, 100.seconds, 1, ThrottleMode.shaping)
    .map { (resp: StreamingPullResponse) =>
      println("reached")
      println(resp.receivedMessages)
    }.runWith(Sink.ignore)


}
