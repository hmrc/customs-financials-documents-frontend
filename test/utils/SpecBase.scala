/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import actions.{IdentifierAction, PvatIdentifierAction}
import org.apache.pekko.stream.testkit.NoMaterializer
import com.codahale.metrics.MetricRegistry
import config.AppConfig
import models.EoriHistory
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.stubPlayBodyParsers
import utils.Utils.emptyString

import scala.reflect.ClassTag

trait SpecBase
    extends AnyWordSpecLike
    with MockitoSugar
    with OptionValues
    with ScalaFutures
    with Matchers
    with IntegrationPatience {

  def fakeRequest(method: String = emptyString, path: String = emptyString): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, path).withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
      .withHeaders(newHeaders = "X-Session-Id" -> "someSessionId")

  val guiceBuilderConfigValues: Seq[(String, String)] =
    Seq("play.filters.csp.nonce.enabled" -> "false", "auditing.enabled" -> "false", "metrics.enabled" -> "false")

  def applicationBuilder(allEoriHistory: Seq[EoriHistory] = Seq.empty): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction]
          .toInstance(new FakeIdentifierAction(stubPlayBodyParsers(NoMaterializer))(allEoriHistory)),
        bind[PvatIdentifierAction]
          .toInstance(new PvatFakeIdentifierAction(stubPlayBodyParsers(NoMaterializer))(allEoriHistory)),
        bind[MetricRegistry].toInstance(new FakeMetrics)
      )
      .configure(guiceBuilderConfigValues: _*)

  lazy val applicationBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder()
    .overrides(
      bind[IdentifierAction]
        .toInstance(new FakeIdentifierAction(stubPlayBodyParsers(NoMaterializer))(Seq.empty)),
      bind[PvatIdentifierAction]
        .toInstance(new PvatFakeIdentifierAction(stubPlayBodyParsers(NoMaterializer))(Seq.empty)),
      bind[MetricRegistry].toInstance(new FakeMetrics)
    )
    .configure(guiceBuilderConfigValues: _*)

  val application: Application = applicationBuilder().build()

  implicit lazy val messages: Messages = application.injector.instanceOf[MessagesApi].preferred(fakeRequest())

  implicit lazy val appConfig: AppConfig = application.injector.instanceOf[AppConfig]

  def instanceOf[T: ClassTag](app: Application): T = app.injector.instanceOf[T]
}

class FakeMetrics extends MetricRegistry {
  val defaultRegistry: MetricRegistry = new MetricRegistry
  val toJson: String                  = "{}"
}
