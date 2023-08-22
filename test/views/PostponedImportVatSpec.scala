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

package views

import config.AppConfig
import models.DutyPaymentMethod.CDS
import models.FileFormat.{Csv, Pdf}
import models.FileRole.{PostponedVATAmendedStatement, PostponedVATStatement}
import models.PostponedVatStatementFile
import models.metadata.PostponedVatStatementFileMetadata
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import play.api.{Application, inject}
import services.DateTimeService
import utils.SpecBase
import viewmodels.PostponedVatViewModel
import views.html.postponed_import_vat

import java.time.{LocalDate, LocalDateTime}

class PostponedImportVatSpec extends SpecBase {
  "PostponedImportVatView" should {
    "display the correct title and guidance" in new Setup {
      when(mockDateTimeService.systemDateTime()).thenReturn(LocalDateTime.now())

      val view: Document = Jsoup.parse(
        app.injector.instanceOf[postponed_import_vat].apply(
          "testEori1",
          PostponedVatViewModel(postponedVatStatementFiles),
          hasRequestedStatements = true,
          cdsOnly = true,
          Option("some_url")).body)

      running(app) {
        view.title() mustBe s"${messages(app)("cf.account.pvat.title")} - ${messages(app)("service.name")} - GOV.UK"
        view.getElementById("notification-panel").html() must not be empty
        view.getElementsByTag("dl").size() must be > 0
        view.getElementsByTag("dt").size() must be > 0
        view.getElementsByTag("dd").size() must be > 0
        view.getElementById("pvat.support.heading").html() must not be empty
        view.getElementById("pvat.support.message").html() must not be empty
      }
    }

    "not display CHIEF doc column when there are no CHIEF doc" in new Setup {
      when(mockDateTimeService.systemDateTime()).thenReturn(LocalDateTime.now())

      val view: Document = Jsoup.parse(
        app.injector.instanceOf[postponed_import_vat].apply(
          "testEori1",
          PostponedVatViewModel(postponedVatStatementFiles),
          hasRequestedStatements = true,
          cdsOnly = true,
          Option("some_url")).body)

      running(app) {
        view.html().contains(messages(app)("cf.account.pvat.download-link", "CDS", "PDF", "111KB"))
        view.html() must not contain ((messages(app)("cf.account.pvat.download-link", "CHIEF", "PDF", "111KB")))
        view.getElementsByTag("dd").size() must be(7)
      }
    }
  }

  trait Setup {
    implicit val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val app: Application = application().overrides(
      inject.bind[DateTimeService].toInstance(mockDateTimeService)
    ).build

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val date: LocalDate = LocalDate.now()

    val postVatStatMetaData1: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(7).getMonthValue, Csv, PostponedVATStatement, CDS, None)
    val postVatStatMetaData2: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(7).getMonthValue, Pdf, PostponedVATStatement, CDS, None)
    val postVatStatMetaData3: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(4).getMonthValue, Pdf, PostponedVATStatement, CDS, None)
    val postVatStatMetaData4: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(5).getMonthValue, Csv, PostponedVATStatement, CDS, None)
    val postVatStatMetaData5: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(5).getMonthValue, Pdf, PostponedVATStatement, CDS, None)
    val postVatStatMetaData6: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(2).getMonthValue, Pdf, PostponedVATAmendedStatement, CDS, None)
    val postVatStatMetaData7: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(2).getMonthValue, Csv, PostponedVATAmendedStatement, CDS, None)
    val postVatStatMetaData8: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(3).getMonthValue, Pdf, PostponedVATStatement, CDS, None)
    val postVatStatMetaData9: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(2).getMonthValue, Pdf, PostponedVATStatement, CDS, None)
    val postVatStatMetaData10: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(1).getMonthValue, Csv, PostponedVATStatement, CDS, None)
    val postVatStatMetaData11: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(1).getMonthValue, Pdf, PostponedVATStatement, CDS, None)
    val postVatStatMetaData12: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(2).getMonthValue, Csv, PostponedVATStatement, CDS, None)

    val postponedVatStatementFiles: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile("name_04", "/some-url", 111L, postVatStatMetaData1, "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 111L, postVatStatMetaData2, "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 111L, postVatStatMetaData3, "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 111L, postVatStatMetaData4, "testEori1"),
      PostponedVatStatementFile("name_01", "/some-url", 111L, postVatStatMetaData5, "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 111L, postVatStatMetaData6, "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 111L, postVatStatMetaData7, "testEori1"),
      PostponedVatStatementFile("name_04", "/some-url", 111L, postVatStatMetaData8, "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 111L, postVatStatMetaData9, "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 111L, postVatStatMetaData10, "testEori1"),
      PostponedVatStatementFile("name_03", "/some-url", 111L, postVatStatMetaData11, "testEori1"),
      PostponedVatStatementFile("name_02", "/some-url", 111L, postVatStatMetaData12, "testEori1")
    )
  }
}
