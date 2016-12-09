import java.util.Properties

val Scalaversion = "2.11.8"
val Scalatraversion = "2.4.1"
val ScalaLoggingVersion = "3.1.0"
val Log4JVersion = "2.6"
val JacksonVersion = "2.7.4"
val Jettyversion = "9.2.10.v20150310"
val AwsSdkversion = "1.10.26"
val ScalaTestVersion = "2.2.6"
val MockitoVersion = "1.10.19"

val appProperties = settingKey[Properties]("The application properties")

appProperties := {
  val prop = new Properties()
  IO.load(prop, new File("build.properties"))
  prop
}

lazy val commonSettings = Seq(
  organization := appProperties.value.getProperty("NDLAOrganization"),
  version := appProperties.value.getProperty("NDLAComponentVersion"),
  scalaVersion := Scalaversion
)

lazy val oembed_proxy = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "oembed-proxy",
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    scalacOptions := Seq("-target:jvm-1.7"),
    libraryDependencies ++= Seq(
      "ndla" %% "network" % "0.6",
      "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
      "org.apache.logging.log4j" % "log4j-api" % Log4JVersion,
      "org.apache.logging.log4j" % "log4j-core" % Log4JVersion,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % Log4JVersion,
      "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % JacksonVersion,
      "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
      "org.scalatra" %% "scalatra" % Scalatraversion,
      "org.scalatra" %% "scalatra-scalatest" % Scalatraversion % "test",
      "org.eclipse.jetty" % "jetty-webapp" % Jettyversion % "container;compile",
      "org.eclipse.jetty" % "jetty-plus" % Jettyversion % "container",
      "javax.servlet" % "javax.servlet-api" % "3.1.0" % "container;provided;test",
      "org.scalatra" %% "scalatra-json" % Scalatraversion,
      "org.json4s"   %% "json4s-native" % "3.3.0",
      "org.scalatra" %% "scalatra-swagger"  % Scalatraversion,
      "org.scalaj" %% "scalaj-http" % "1.1.5",
      "com.netaporter" %% "scala-uri" % "0.4.12",
      "org.scalatest" % "scalatest_2.11" % ScalaTestVersion % "test",
      "org.mockito" % "mockito-all" % MockitoVersion % "test")
  ).enablePlugins(DockerPlugin).enablePlugins(GitVersioning).enablePlugins(JettyPlugin)

assemblyJarName in assembly := "oembed-proxy.jar"
mainClass in assembly := Some("no.ndla.oembedproxy.JettyLauncher")
assemblyMergeStrategy in assembly := {
  case "mime.types" => MergeStrategy.filterDistinctLines
  case PathList("org", "joda", "convert", "ToString.class")  => MergeStrategy.first
  case PathList("org", "joda", "convert", "FromString.class")  => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// Don't run Integration tests in default run
testOptions in Test += Tests.Argument("-l", "no.ndla.IntegrationTest")

// Make the docker task depend on the assembly task, which generates a fat JAR file
docker <<= (docker dependsOn assembly)

dockerfile in docker := {
  val artifact = (assemblyOutputPath in assembly).value
  val artifactTargetPath = s"/app/${artifact.name}"
  new Dockerfile {
    from("java")

    add(artifact, artifactTargetPath)
    entryPoint("java", "-Dorg.scalatra.environment=production", "-jar", artifactTargetPath)
  }
}

imageNames in docker := Seq(
  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some(System.getProperty("docker.tag", "SNAPSHOT")))
)

resolvers ++= scala.util.Properties.envOrNone("NDLA_RELEASES").map(repo => "Release Sonatype Nexus Repository Manager" at repo).toSeq