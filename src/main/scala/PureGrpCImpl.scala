import java.util
import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.auth.oauth2.GoogleCredentials
import com.google.pubsub.v1.pubsub.{StreamingPullRequest, StreamingPullResponse, SubscriberGrpc}
import io.grpc.ClientInterceptors
import io.grpc.auth.ClientAuthInterceptor
import io.grpc.netty.{NegotiationType, NettyChannelBuilder}
import io.grpc.stub.{ClientCallStreamObserver, StreamObserver}

/**
  * Created by janos on 11.11.17.
  */
object PureGrpCImpl extends App {


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

  private val subscriberStub = SubscriberGrpc.stub(channelWithAuth)


  private val toServer: ClientCallStreamObserver[StreamingPullRequest] = subscriberStub.streamingPull(new StreamObserver[StreamingPullResponse] {

    override def onError(t: Throwable): Unit = t.printStackTrace()

    override def onCompleted(): Unit = println("completed!")

    override def onNext(value: StreamingPullResponse): Unit = {

      println(value.receivedMessages)
//      toServer.request(1)
      //      Thread.sleep(5000)
    }

  }).asInstanceOf[ClientCallStreamObserver[StreamingPullRequest]]

  toServer.onNext(StreamingPullRequest(subscription = String.format("projects/%s/subscriptions/%s",
  "crm360-degrees",
  "janos-subscription"), streamAckDeadlineSeconds = 10))

// var i = 0
//
//  new Thread(() => {
//    while (i < 4) {
//      i = i + 1
//      toServer.request(1)
//    }
//  }).start()

  //  toServer.onCompleted()

}
