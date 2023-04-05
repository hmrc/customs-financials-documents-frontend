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

package controllers

import config.AppConfig
import connectors.{DataStoreConnector, FinancialsApiConnector, SdesConnector}
import models.DutyPaymentMethod.CDS
import models.FileFormat.{Csv, Pdf}
import models.FileRole.{PostponedVATAmendedStatement, PostponedVATStatement}
import models.metadata.PostponedVatStatementFileMetadata
import models.{EoriHistory, PostponedVatStatementFile}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.DateTimeService
import uk.gov.hmrc.auth.core.retrieve.Email
import utils.SpecBase
import viewmodels.PostponedVatViewModel
import views.html.postponed_import_vat

import java.time.LocalDate
import scala.concurrent.Future

class PostponedVatControllerSpec extends SpecBase {

  "show" should {
    "display the PostponedVat page" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some("CDS")).url)
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe view("testEori1", PostponedVatViewModel(postponedVatStatementFiles ++ historicPostponedVatStatementFiles)(messages(app), mockDateTimeService), hasRequestedStatements = false, cdsOnly = false, Some("CDS"))(request, messages(app), config).toString()
      }
    }

    "return 200 for generic request" in {
      val app = application().build()
      val request = FakeRequest(routes.PostponedVatController.show(Some("CDS")))
      running(app) {
        val result = route(app, request).value
        status(result) mustBe 200
      }
    }
  }

  trait Setup {
    val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
    val mockSdesConnector: SdesConnector = mock[SdesConnector]
    val mockDataStoreConnector: DataStoreConnector = mock[DataStoreConnector]
    val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val date: LocalDate = LocalDate.now()

    val postponedVatStatementFiles = List(
      PostponedVatStatementFile("name_04", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(7).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(7).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(4).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(5).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_01", "/some-url", 1300000L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(5).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 8192L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Pdf, PostponedVATAmendedStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 8192L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Csv, PostponedVATAmendedStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(3).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(1).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(1).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 4096L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(2).getMonthValue, Csv, PostponedVATStatement, CDS, None), "testEori1")
    )

    val historicPostponedVatStatementFiles = List(
      PostponedVatStatementFile("historic_name_03", "/some-url-historic-3", 111L, PostponedVatStatementFileMetadata(date.getYear, date.minusMonths(4).getMonthValue, Pdf, PostponedVATStatement, CDS, None), "testEori2")
    )

    when(mockDataStoreConnector.getEmail(any)(any))
      .thenReturn(Future.successful(Right(Email("some@email.com"))))
    when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori1"))(any))
      .thenReturn(Future.successful(postponedVatStatementFiles))
    when(mockSdesConnector.getPostponedVatStatements(eqTo("testEori2"))(any))
      .thenReturn(Future.successful(historicPostponedVatStatementFiles))
    when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
      .thenReturn(Future.successful(true))
    when(mockDateTimeService.systemDateTime())
      .thenReturn(date.atStartOfDay())


    val app: Application = application(Seq(EoriHistory("testEori2", Some(date.minusYears(1)), Some(date.minusMonths(6))))).overrides(
      inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
      inject.bind[SdesConnector].toInstance(mockSdesConnector),
      inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector)
    ).build()

    val view: postponed_import_vat = app.injector.instanceOf[postponed_import_vat]
    val config: AppConfig = app.injector.instanceOf[AppConfig]
  }
}
