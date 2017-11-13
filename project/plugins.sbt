addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.9")

libraryDependencies ++= Seq(
"com.trueaccord.scalapb" %% "compilerplugin"          % "0.6.0-pre5",
"beyondthelines"         %% "grpcakkastreamgenerator" % "0.0.1"
)

resolvers += Resolver.bintrayRepo("beyondthelines", "maven")
