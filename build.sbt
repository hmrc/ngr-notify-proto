import uk.gov.hmrc.DefaultBuildSettings

val appName     = "ngr-notify-proto"
val defaultPort = 1515

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.7.1"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions += "-Wconf:src=routes/:s",
    scalacOptions += "-Wconf:msg=Flag .* set repeatedly:s",
    scalacOptions += "-Wconf:msg=Implicit parameters should be provided with a \\`using\\` clause&src=views/:s",
    scalacOptions += "-feature",
    javaOptions += "-XX:+EnableDynamicAgentLoading",
    PlayKeys.playDefaultPort := defaultPort
  )
  .settings(CodeCoverageSettings.settings *)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)
