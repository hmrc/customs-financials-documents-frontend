import uk.gov.hmrc.DefaultBuildSettings.{targetJvm, itSettings}

val appName = "customs-financials-documents-frontend"

val silencerVersion = "1.17.13"
val scala2_13_8 = "2.13.8"
val bootstrapVersion = "7.22.0"

val testDirectory = "test"
val scalaStyleConfigFile = "scalastyle-config.xml"
val testScalaStyleConfigFile = "test-scalastyle-config.xml"

Global / lintUnusedKeysOnLoad := false

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala2_13_8

lazy val scalastyleSettings = Seq(scalastyleConfig := baseDirectory.value / scalaStyleConfigFile,
  (Test / scalastyleConfig) := baseDirectory.value / testDirectory / testScalaStyleConfigFile)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % Test))

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .settings(scoverageSettings *)
  .settings(PlayKeys.playDefaultPort := 9398)
  .settings(scalastyleSettings)
  .settings(
    targetJvm                        := "jvm-11",
    scalacOptions ++= Seq(
      "-P:silencer:pathFilters=routes",
      "-P:silencer:pathFilters=target/.*",
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:patvars",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates"),
    Test / scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:patvars",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates"),
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    Assets / pipelineStages := Seq(gzip),
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )
  .configs(IntegrationTest)
  .settings(resolvers += Resolver.jcenterRepo)

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := List("<empty>"
      , "Reverse.*"
      , ".*views.*"
      , ".*(BuildInfo|Routes|testOnly).*").mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
