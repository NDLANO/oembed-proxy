import java.util.Properties

val Scalaversion = "2.12.10"
val Scalatraversion = "2.6.5"
val ScalaLoggingVersion = "3.9.0"
val Log4JVersion = "2.11.1"
val JacksonVersion = "2.9.10.2"
val Jettyversion = "9.4.18.v20190429"
val ScalaTestVersion = "3.0.5"
val MockitoVersion = "2.23.0"
val Json4SVersion = "3.6.7"

val appProperties = settingKey[Properties]("The application properties")

appProperties := {
  val prop = new Properties()
  IO.load(prop, new File("build.properties"))
  prop
}

lazy val oembed_proxy = (project in file("."))
  .settings(
    name := "oembed-proxy",
    organization := appProperties.value.getProperty("NDLAOrganization"),
    version := appProperties.value.getProperty("NDLAComponentVersion"),
    scalaVersion := Scalaversion,
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions := Seq("-target:jvm-1.8", "-deprecation"),
    libraryDependencies ++= Seq(
      "ndla" %% "network" % "0.42",
      "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
      "org.apache.logging.log4j" % "log4j-api" % Log4JVersion,
      "org.apache.logging.log4j" % "log4j-core" % Log4JVersion,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % Log4JVersion,
      "org.scalatra" %% "scalatra" % Scalatraversion,
      "org.scalatra" %% "scalatra-scalatest" % Scalatraversion % "test",
      "org.eclipse.jetty" % "jetty-webapp" % Jettyversion % "container;compile",
      "org.eclipse.jetty" % "jetty-plus" % Jettyversion % "container",
      "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
      "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.8.11",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "container;provided;test",
      "org.scalatra" %% "scalatra-json" % Scalatraversion,
      "org.json4s" %% "json4s-native" % Json4SVersion,
      "org.scalatra" %% "scalatra-swagger" % Scalatraversion,
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "io.lemonlabs" %% "scala-uri" % "1.5.1",
      "org.jsoup" % "jsoup" % "1.11.3",
      "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
      "org.mockito" % "mockito-core" % MockitoVersion % "test"
    )
  )
  .enablePlugins(DockerPlugin)
  .enablePlugins(JettyPlugin)

assembly / assemblyJarName := "oembed-proxy.jar"
assembly / mainClass := Some("no.ndla.oembedproxy.JettyLauncher")
assembly / assemblyMergeStrategy := {
  case "module-info.class" => MergeStrategy.discard
  case "mime.types"        => MergeStrategy.filterDistinctLines
  case PathList("org", "joda", "convert", "ToString.class") =>
    MergeStrategy.first
  case PathList("org", "joda", "convert", "FromString.class") =>
    MergeStrategy.first
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

val checkfmt = taskKey[Boolean]("check for code style errors")
checkfmt := {
  val noErrorsInMainFiles = (Compile / scalafmtCheck).value
  val noErrorsInTestFiles = (Test / scalafmtCheck).value
  val noErrorsInBuildFiles = (Compile / scalafmtSbtCheck).value

  noErrorsInMainFiles && noErrorsInTestFiles && noErrorsInBuildFiles
}

Test / test := (Test / test).dependsOn(Test / checkfmt).value

val fmt = taskKey[Unit]("Automatically apply code style fixes")
fmt := {
  (Compile / scalafmt).value
  (Test / scalafmt).value
  (Compile / scalafmtSbt).value
}

// Don't run Integration tests in default run
Test / testOptions += Tests.Argument("-l", "no.ndla.IntegrationTest")

// Make the docker task depend on the assembly task, which generates a fat JAR file
docker := (docker dependsOn assembly).value

docker / dockerfile := {
  val artifact = (assembly / assemblyOutputPath).value
  val artifactTargetPath = s"/app/${artifact.name}"
  new Dockerfile {
    from("adoptopenjdk/openjdk11:alpine-slim")
    run("apk", "--no-cache", "add", "ttf-dejavu")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-Dorg.scalatra.environment=production", "-jar", artifactTargetPath)
  }
}

docker / imageNames := Seq(
  ImageName(namespace = Some(organization.value),
            repository = name.value,
            tag = Some(System.getProperty("docker.tag", "SNAPSHOT")))
)

resolvers ++= scala.util.Properties
  .envOrNone("NDLA_RELEASES")
  .map(repo => "Release Sonatype Nexus Repository Manager" at repo)
  .toSeq
