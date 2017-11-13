override def streamingPull: Flow[com.google.pubsub.v1.pubsub.StreamingPullRequest, com.google.pubsub.v1.pubsub.StreamingPullResponse, NotUsed] = {

        class CustomGrpcGraphStage[I, O](operator: GrpcOperator[I, O]) extends GrpcGraphStage[I, O](operator: GrpcOperator[I, O]) {
        override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
        new GraphStageLogic(shape) {
        val outObs = new StreamObserver[O] {
        override def onError(t: Throwable) = fail(out, t)
        override def onCompleted() =
        getAsyncCallback((_: Unit) => complete(out)).invoke(())
        override def onNext(value: O) =
        getAsyncCallback((value: O) => emit(out, value)).invoke(value)
        }
        val inObs = operator(outObs)
        setHandler(in, new InHandler {
        override def onPush(): Unit = {
        val input = grab(in)
        inObs.onNext(input)
        pull(in)
        }
        override def onUpstreamFinish(): Unit = println("i won't stop the stream!")
        override def onUpstreamFailure(t: Throwable): Unit = inObs.onError(t)
        })
        setHandler(out, new OutHandler {
        override def onPull(): Unit = ()
        })
        override def preStart(): Unit = pull(in)
        }
        }
        }

        Flow.fromGraph(new CustomGrpcGraphStage[com.google.pubsub.v1.pubsub.StreamingPullRequest, com.google.pubsub.v1.pubsub.StreamingPullResponse](outputObserver => {
        val e: StreamObserver[StreamingPullRequest] = ClientCalls.asyncBidiStreamingCall(
        channel.newCall(METHOD_STREAMING_PULL, options),
        outputObserver)
        e
        }
        ))
        }