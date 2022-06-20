import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.24.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "3.21.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "5.24.0" % Test,
    "org.jsoup" % "jsoup" % "1.13.1" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.7.1" % Test
  )
}
