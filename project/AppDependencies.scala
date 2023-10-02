import sbt._

object AppDependencies {

  val bootstrapVersion = "7.22.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.21.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % Test,
    "org.jsoup" % "jsoup" % "1.16.1" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.14" % Test,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.21.0-play-28"
  )
}
