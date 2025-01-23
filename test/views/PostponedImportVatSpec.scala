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
import org.scalatest.matchers.must.Matchers.{must, mustBe}
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import play.api.{Application, inject}
import services.DateTimeService
import utils.CommonTestData._
import utils.SpecBase
import viewmodels.{PVATUrls, PostponedVatViewModel, PvEmail}
import views.html.postponed_import_vat

import org.mockito.Mockito.when

import java.time.{LocalDate, LocalDateTime}

class PostponedImportVatSpec extends SpecBase {

  "PostponedImportVatView" should {

    "display the correct title and guidance" in new Setup {
      when(mockDateTimeService.systemDateTime()).thenReturn(LocalDateTime.now())

      val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[postponed_import_vat]
          .apply(
            PostponedVatViewModel(
              postponedVatStatementFiles,
              hasRequestedStatements = true,
              isCdsOnly = true,
              Option("some_url"),
              urls = pvatUrls
            )
          )
          .body
      )

      view.title() mustBe s"${messages("cf.account.pvat.title")} - ${messages("service.name")} - GOV.UK"
      view.getElementById("main-content").html()         must not contain "h2"
      view.html().contains("cf.account.vat.available.statement-text")
      view.getElementsByTag("dl").size()                 must be > 0
      view.getElementsByTag("dt").size()                 must be > 0
      view.getElementsByTag("dd").size()                 must be > 0
      view.getElementById("pvat.support.heading").html() must not be empty
      view.getElementById("pvat.support.message").html() must not be empty
      view.html().contains(serviceUnavailableUrl)
      view.getElementById("chief-guidance-heading").html() mustBe
        messages("cf.account.vat.chief.heading")
    }

    "not display CHIEF doc column when there are no CHIEF doc" in new Setup {
      when(mockDateTimeService.systemDateTime()).thenReturn(LocalDateTime.now())

      val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[postponed_import_vat]
          .apply(
            PostponedVatViewModel(
              postponedVatStatementFiles,
              hasRequestedStatements = true,
              isCdsOnly = true,
              location = Option("some_url"),
              urls = pvatUrls.copy(serviceUnavailableUrl = None)
            )
          )
          .body
      )

      val expectedSize = 7

      view.html().contains(messages("cf.account.pvat.download-link", "CDS", "PDF", "111KB"))
      view.html()                        must not contain messages("cf.account.pvat.download-link", "CHIEF", "PDF", "111KB")
      view.getElementsByTag("dd").size() must be(expectedSize)
    }

    "display 'not available' messages correctly when no statements are present " +
      "and it is after the 19th of the previous month" in new Setup {
        when(mockDateTimeService.systemDateTime())
          .thenReturn(LocalDateTime.now().withDayOfMonth(DAY_19).minusMonths(ONE_MONTH))

        val view: Document = Jsoup.parse(
          app.injector
            .instanceOf[postponed_import_vat]
            .apply(
              PostponedVatViewModel(
                postponedVatStatementFiles,
                hasRequestedStatements = true,
                isCdsOnly = true,
                Option("some_url"),
                urls = pvatUrls.copy(serviceUnavailableUrl = None)
              )
            )
            .body
        )

        val cdsNotAvailableMessage: String = view.select("dd").text()
        cdsNotAvailableMessage must include(messages("cf.common.not-available"))
      }

    "display grouped certificates by EORI and format correctly" in new Setup {
      when(mockDateTimeService.systemDateTime()).thenReturn(LocalDateTime.now())

      val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[postponed_import_vat]
          .apply(
            PostponedVatViewModel(
              postponedVatStatementFiles,
              hasRequestedStatements = true,
              isCdsOnly = false,
              Option("some_url"),
              urls = pvatUrls.copy(serviceUnavailableUrl = None)
            )
          )
          .body
      )

      val expectedSize = 13

      view.select("dd.govuk-summary-list__actions").size() mustBe expectedSize
      view.html() must include(messages("cf.account.pvat.aria.amended-download-link"))
    }

    "handle missing files and display messages appropriately" in new Setup {
      when(mockDateTimeService.systemDateTime()).thenReturn(LocalDateTime.now())

      val view: Document = Jsoup.parse(
        app.injector
          .instanceOf[postponed_import_vat]
          .apply(
            PostponedVatViewModel(
              Seq.empty,
              hasRequestedStatements = true,
              isCdsOnly = false,
              Option("some_url"),
              urls = pvatUrls.copy(serviceUnavailableUrl = None)
            )
          )
          .body
      )

      view.html() must include(messages("cf.common.not-available"))
    }
  }

  trait Setup {
    val date: LocalDate = LocalDate.now()

    val serviceUnavailableUrl: String = "service_unavailable_url"

    val postVatStatMetaData1: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(MONTH_7).getMonthValue,
      Csv,
      PostponedVATStatement,
      CDS,
      None
    )

    val postVatStatMetaData2: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(MONTH_7).getMonthValue,
      Pdf,
      PostponedVATStatement,
      CDS,
      None
    )

    val postVatStatMetaData3: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(FOUR_MONTHS).getMonthValue,
      Pdf,
      PostponedVATStatement,
      CDS,
      None
    )

    val postVatStatMetaData4: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(FIVE_MONTHS).getMonthValue,
      Csv,
      PostponedVATStatement,
      CDS,
      None
    )

    val postVatStatMetaData5: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(FIVE_MONTHS).getMonthValue,
      Pdf,
      PostponedVATStatement,
      CDS,
      None
    )

    val postVatStatMetaData6: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(TWO_MONTHS).getMonthValue,
      Pdf,
      PostponedVATAmendedStatement,
      CDS,
      None
    )

    val postVatStatMetaData7: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(TWO_MONTHS).getMonthValue,
      Csv,
      PostponedVATAmendedStatement,
      CDS,
      None
    )

    val postVatStatMetaData8: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(THREE_MONTHS).getMonthValue,
      Pdf,
      PostponedVATStatement,
      CDS,
      None
    )

    val postVatStatMetaData9: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(TWO_MONTHS).getMonthValue,
      Pdf,
      PostponedVATStatement,
      CDS,
      None
    )

    val postVatStatMetaData10: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(ONE_MONTH).getMonthValue,
      Csv,
      PostponedVATStatement,
      CDS,
      None
    )

    val postVatStatMetaData11: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(ONE_MONTH).getMonthValue,
      Pdf,
      PostponedVATStatement,
      CDS,
      None
    )

    val postVatStatMetaData12: PostponedVatStatementFileMetadata = PostponedVatStatementFileMetadata(
      date.getYear,
      date.minusMonths(TWO_MONTHS).getMonthValue,
      Csv,
      PostponedVATStatement,
      CDS,
      None
    )

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

    val app: Application = applicationBuilder()
      .overrides(
        inject.bind[DateTimeService].toInstance(mockDateTimeService)
      )
      .build()

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val pvatUrls: PVATUrls = PVATUrls(
      customsFinancialsHomePageUrl = appConfig.customsFinancialsFrontendHomepage,
      requestStatementsUrl = appConfig.requestedStatements(PostponedVATStatement),
      pvEmail = PvEmail(appConfig.pvEmailEmailAddress, appConfig.pvEmailEmailAddressHref),
      viewVatAccountSupportLink = appConfig.viewVatAccountSupportLink,
      serviceUnavailableUrl = Some(serviceUnavailableUrl)
    )
  }
}
