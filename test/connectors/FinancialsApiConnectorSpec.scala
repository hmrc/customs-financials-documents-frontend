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

package connectors

import models.FileRole
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.must.Matchers.mustBe
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers.*
import play.api.{Application, inject}
import services.MetricsReporterService
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.SpecBase
import utils.Utils.emptyString

import scala.concurrent.Future

class FinancialsApiConnectorSpec extends SpecBase {

  "delete notifications should return a boolean based on the result" in new Setup {
    when(mockMetricsReporterService.withResponseTimeLogging[HttpResponse](any)(any)(any))
      .thenAnswer((i: InvocationOnMock) => {
        i.getArgument[Future[HttpResponse]](1)
      })

    when(mockHttpClient.delete(any)(any)).thenReturn(requestBuilder)
    when(requestBuilder.execute(any, any))
      .thenReturn(Future.successful(HttpResponse(OK, emptyString)))

    running(app) {
      val result = await(connector.deleteNotification("someEori", FileRole.C79Certificate))
      result mustBe true
    }
  }

  trait Setup {
    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]
    val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    val app: Application = application().overrides(
      inject.bind[MetricsReporterService].toInstance(mockMetricsReporterService),
      inject.bind[HttpClientV2].toInstance(mockHttpClient)
    ).build()

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val connector: FinancialsApiConnector = app.injector.instanceOf[FinancialsApiConnector]
  }
}
