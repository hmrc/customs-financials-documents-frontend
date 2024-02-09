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
import utils.CommonTestData.{
  DOWNLOAD_URL_00, EORI_NUMBER, FIVE_MONTHS, FOUR_MONTHS, MONTH_7, ONE_MONTH, SIZE_111L,
  STAT_FILE_NAME_01, STAT_FILE_NAME_02, STAT_FILE_NAME_03, STAT_FILE_NAME_04, THREE_MONTHS, TWO_MONTHS
}
import utils.SpecBase
import viewmodels.PostponedVatViewModel
import views.html.postponed_import_vat

import java.time.{LocalDate, LocalDateTime}

class PostponedImportVatSpec extends SpecBase {

  "PostponedImportVatView" should {

    "display the correct title and guidance" in new Setup {
      when(mockDateTimeService.systemDateTime()).thenReturn(LocalDateTime.now())

      val serviceUnavailbleUrl: Option[String] = Option("service_unavailable_url")
      val view: Document = Jsoup.parse(
        app.injector.instanceOf[postponed_import_vat].apply(
          EORI_NUMBER,
          PostponedVatViewModel(postponedVatStatementFiles),
          hasRequestedStatements = true,
          cdsOnly = true,
          Option("some_url"),
          serviceUnavailbleUrl).body)

      running(app) {
        view.title() mustBe s"${messages(app)("cf.account.pvat.title")} - ${messages(app)("service.name")} - GOV.UK"
        view.getElementById("notification-panel").html() must not be empty
        view.getElementById("main-content").html() must not contain "h2"
        view.getElementsByTag("dl").size() must be > 0
        view.getElementsByTag("dt").size() must be > 0
        view.getElementsByTag("dd").size() must be > 0
        view.getElementById("pvat.support.heading").html() must not be empty
        view.getElementById("pvat.support.message").html() must not be empty
        view.html().contains(serviceUnavailbleUrl)
      }
    }

    "not display CHIEF doc column when there are no CHIEF doc" in new Setup {
      when(mockDateTimeService.systemDateTime()).thenReturn(LocalDateTime.now())

      val view: Document = Jsoup.parse(
        app.injector.instanceOf[postponed_import_vat].apply(
          EORI_NUMBER,
          PostponedVatViewModel(postponedVatStatementFiles),
          hasRequestedStatements = true,
          cdsOnly = true,
          Option("some_url")).body)

      val expectedSize = 7

      running(app) {
        view.html().contains(messages(app)("cf.account.pvat.download-link", "CDS", "PDF", "111KB"))
        view.html() must not contain messages(app)("cf.account.pvat.download-link", "CHIEF", "PDF", "111KB")
        view.getElementsByTag("dd").size() must be(expectedSize)
      }
    }
  }

  trait Setup {
    val date: LocalDate = LocalDate.now()

    val postVatStatMetaData1: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(MONTH_7).getMonthValue, Csv, PostponedVATStatement, CDS, None)

    val postVatStatMetaData2: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(MONTH_7).getMonthValue, Pdf, PostponedVATStatement, CDS, None)

    val postVatStatMetaData3: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(FOUR_MONTHS).getMonthValue, Pdf, PostponedVATStatement, CDS, None)

    val postVatStatMetaData4: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(FIVE_MONTHS).getMonthValue, Csv, PostponedVATStatement, CDS, None)

    val postVatStatMetaData5: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(FIVE_MONTHS).getMonthValue, Pdf, PostponedVATStatement, CDS, None)

    val postVatStatMetaData6: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(TWO_MONTHS).getMonthValue, Pdf, PostponedVATAmendedStatement, CDS, None)

    val postVatStatMetaData7: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(TWO_MONTHS).getMonthValue, Csv, PostponedVATAmendedStatement, CDS, None)

    val postVatStatMetaData8: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(THREE_MONTHS).getMonthValue, Pdf, PostponedVATStatement, CDS, None)

    val postVatStatMetaData9: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(TWO_MONTHS).getMonthValue, Pdf, PostponedVATStatement, CDS, None)

    val postVatStatMetaData10: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(ONE_MONTH).getMonthValue, Csv, PostponedVATStatement, CDS, None)

    val postVatStatMetaData11: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(ONE_MONTH).getMonthValue, Pdf, PostponedVATStatement, CDS, None)

    val postVatStatMetaData12: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(date.getYear,
      date.minusMonths(TWO_MONTHS).getMonthValue, Csv, PostponedVATStatement, CDS, None)

    val postponedVatStatementFiles: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile(STAT_FILE_NAME_04, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData1, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_04, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData2, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_03, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData3, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_02, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData4, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_01, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData5, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_04, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData6, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_02, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData7, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_04, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData8, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_03, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData9, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_03, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData10, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_03, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData11, EORI_NUMBER),
      PostponedVatStatementFile(STAT_FILE_NAME_02, DOWNLOAD_URL_00, SIZE_111L, postVatStatMetaData12, EORI_NUMBER)
    )

    implicit val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val app: Application = application().overrides(
      inject.bind[DateTimeService].toInstance(mockDateTimeService)
    ).build()

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")
  }
}
