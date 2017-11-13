lazy val `grpc-to-akka-streams` = project
  .in(file("."))
  .settings(name := "grpc-to-akka-streams")
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      library.GrpcAkkaStreamRuntime,
      library.ScalaPbRuntimeGrpc % "protobuf",
      library.GrpcNetty,
      library.NettyTcNative,
      library.GrpcStub,
      library.GrpcAuth,
      library.GrpcProtobuf,
      library.GoogleOauth2
    )
  )
  .settings(
    PB.targets in Compile in ThisBuild := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value,
      grpc.akkastreams.generators.GrpcAkkaStreamGenerator -> (sourceManaged in Compile).value
    )
  )


lazy val commonSettings = Seq(
  organization := "de.codecentric",
  scalaVersion := "2.12.3",
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding",
    "UTF-8"
  ),
  javacOptions ++= Seq(
    "-source",
    "1.8",
    "-target",
    "1.8"
  ),
  resolvers += Resolver.bintrayRepo("beyondthelines", "maven")
)

lazy val library = new {

  object Version {
    val GrpcAkkaStreamRuntime = "0.0.1"
    val ScalaPbRuntimeGrpc = "0.6.0-pre5"
    val Grpc               = "1.6.1"
    val NettyTcNative      = "2.0.3.Final"
    val GoogleOauth2       = "0.8.0"



  }

  val GrpcAkkaStreamRuntime   = "beyondthelines"          %% "grpcakkastreamruntime"          % Version.GrpcAkkaStreamRuntime
  val ScalaPbRuntimeGrpc      = "com.trueaccord.scalapb"  %% "scalapb-runtime-grpc"           % Version.ScalaPbRuntimeGrpc
  val GrpcNetty               = "io.grpc"                 % "grpc-netty"                      % Version.Grpc
  val NettyTcNative           = "io.netty"                % "netty-tcnative-boringssl-static" % Version.NettyTcNative
  val GrpcStub                = "io.grpc"                 % "grpc-stub"                       % Version.Grpc
  val GrpcAuth                = "io.grpc"                 % "grpc-auth"                       % Version.Grpc
  val GrpcProtobuf            = "io.grpc"                 % "grpc-protobuf"                   % Version.Grpc
  val GoogleOauth2            = "com.google.auth"         % "google-auth-library-oauth2-http" % Version.GoogleOauth2



}