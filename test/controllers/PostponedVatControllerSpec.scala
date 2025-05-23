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
import models.DutyPaymentMethod.{CDS, CHIEF}
import models.FileFormat.Pdf
import models.FileRole.PostponedVATStatement
import models.metadata.PostponedVatStatementFileMetadata
import models.{EoriHistory, PostponedVatStatementFile}
import navigation.Navigator
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.mustBe
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.{eq => eqTo}
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.DateTimeService
import uk.gov.hmrc.auth.core.retrieve.Email
import utils.CommonTestData._
import utils.SpecBase
import utils.Utils.{hyphen, singleSpace}
import viewmodels.{PVATUrls, PostponedVatViewModel, PvEmail}
import views.helpers.Formatters
import views.html.{postponed_import_vat, postponed_import_vat_not_available}

import java.time.LocalDate
import scala.concurrent.Future

class PostponedVatControllerSpec extends SpecBase {

  "show" should {

    "display the PostponedVat page with one column(CDS) for statements" in new Setup {
      appConfigForSetup.historicStatementsEnabled = false

      val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("postponed-vat").url

      when(mockDataStoreConnector.getEmail(any))
        .thenReturn(Future.successful(Right(Email(emailValue))))

      when(mockSdesConnector.getPostponedVatStatements(eqTo(eori))(any))
        .thenReturn(Future.successful(postponedVatStatementFiles))

      when(mockSdesConnector.getPostponedVatStatements(eqTo(historicEori))(any))
        .thenReturn(Future.successful(historicPostponedVatStatementFiles))

      when(mockFinancialsApiConnector.deleteNotification(any)(any))
        .thenReturn(Future.successful(true))

      when(mockDateTimeService.systemDateTime())
        .thenReturn(date.atStartOfDay())

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some(cdsLocation)).url)
        val result  = route(app, request).value

        status(result) mustBe OK

        contentAsString(result) mustBe view(
          PostponedVatViewModel(
            currentStatements,
            hasRequestedStatements = true,
            isCdsOnly = true,
            location = Some(cdsLocation),
            urls = pvatUrls
          )(messages, mockDateTimeService)
        )(request, messages, appConfig).toString()

        contentAsString(result).contains(serviceUnavailableUrl)
        contentAsString(result) should not include "CHIEF statement -"
      }
    }

    "display the PostponedVat page with two columns (CHIEF and CDS) for last 6 months" in new Setup {
      appConfigForSetup.historicStatementsEnabled = false

      val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("postponed-vat").url

      when(mockDataStoreConnector.getEmail(any))
        .thenReturn(Future.successful(Right(Email(emailValue))))

      when(mockSdesConnector.getPostponedVatStatements(eqTo(eori))(any))
        .thenReturn(Future.successful(postponedVatStatementFilesWithCHIEF))

      when(mockSdesConnector.getPostponedVatStatements(eqTo(historicEori))(any))
        .thenReturn(Future.successful(historicPostponedVatStatementFiles))

      when(mockFinancialsApiConnector.deleteNotification(any)(any))
        .thenReturn(Future.successful(true))

      when(mockDateTimeService.systemDateTime())
        .thenReturn(date.atStartOfDay())

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some(cdsLocation)).url)
        val result  = route(app, request).value

        status(result) mustBe OK

        contentAsString(result).contains(serviceUnavailableUrl)
        contentAsString(result).contains("CHIEF statement -")
      }
    }

    "display the PostponedVat page with one column(CDS) " +
      "and Ignore historic CHIEF statements older than 6 months" in new Setup {
        appConfigForSetup.historicStatementsEnabled = false

        val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("postponed-vat").url

        when(mockDataStoreConnector.getEmail(any))
          .thenReturn(Future.successful(Right(Email(emailValue))))

        when(mockSdesConnector.getPostponedVatStatements(eqTo(eori))(any))
          .thenReturn(Future.successful(postponedVatStatementFiles))

        when(mockSdesConnector.getPostponedVatStatements(eqTo(historicEori))(any))
          .thenReturn(Future.successful(historicPostponedVatStatementFilesWithCHIEF))

        when(mockFinancialsApiConnector.deleteNotification(any)(any))
          .thenReturn(Future.successful(true))

        when(mockDateTimeService.systemDateTime())
          .thenReturn(date.atStartOfDay())

        running(app) {
          val request = fakeRequest(GET, routes.PostponedVatController.show(Some(cdsLocation)).url)
          val result  = route(app, request).value

          status(result) mustBe OK

          contentAsString(result).contains(serviceUnavailableUrl)
          contentAsString(result) should not include "CHIEF statement -"
        }
      }

    "display the PostponedVat page with no statements text when statement is not available for the " +
      "immediate previous month and accessed after 19th of the month" in new Setup {

        val currentDate: LocalDate = LocalDate.of(date.getYear, date.getMonthValue, DAY_20)

        when(mockDataStoreConnector.getEmail(any))
          .thenReturn(Future.successful(Right(Email(emailValue))))

        when(mockSdesConnector.getPostponedVatStatements(eqTo(eori))(any))
          .thenReturn(Future.successful(postponedVatStatementFilesWithImmediateUnavailable))

        when(mockSdesConnector.getPostponedVatStatements(eqTo(historicEori))(any))
          .thenReturn(Future.successful(historicPostponedVatStatementFiles))

        when(mockFinancialsApiConnector.deleteNotification(any)(any))
          .thenReturn(Future.successful(true))

        when(mockDateTimeService.systemDateTime()).thenReturn(currentDate.atStartOfDay())

        running(app) {
          val request = fakeRequest(GET, routes.PostponedVatController.show(Some(cdsLocation)).url)
          val result  = route(app, request).value

          status(result) mustBe OK

          contentAsString(result) mustBe view(
            PostponedVatViewModel(
              postponedVatStatementFilesWithImmediateUnavailable,
              hasRequestedStatements = true,
              isCdsOnly = true,
              location = Some(cdsLocation),
              urls =
                pvatUrls.copy(serviceUnavailableUrl = Some(appConfigForSetup.historicRequestUrl(PostponedVATStatement)))
            )(messages, mockDateTimeService)
          )(request, messages, appConfig).toString()

          val doc           = Jsoup.parse(contentAsString(result))
          val periodElement = Formatters
            .dateAsMonthAndYear(date.minusMonths(ONE_MONTH))(messages)
            .replace(singleSpace, hyphen)
            .toLowerCase

          doc.getElementById(s"period-$periodElement").children().text() should include(
            messages(
              "cf.common.not-available",
              Formatters.dateAsMonth(date.minusMonths(ONE_MONTH))(messages)
            )
          )
        }
      }

    "not display the immediate previous month statement on PostponedVat page when accessed " +
      "before 20th day of the month and statement is not available" in new Setup {
        appConfigForSetup.historicStatementsEnabled = false

        val currentDate: LocalDate = LocalDate.of(date.getYear, date.getMonthValue, DAY_12)

        when(mockDataStoreConnector.getEmail(any))
          .thenReturn(Future.successful(Right(Email(emailValue))))

        when(mockSdesConnector.getPostponedVatStatements(eqTo(eori))(any))
          .thenReturn(Future.successful(postponedVatStatementFilesWithImmediateUnavailable))

        when(mockSdesConnector.getPostponedVatStatements(eqTo(historicEori))(any))
          .thenReturn(Future.successful(historicPostponedVatStatementFiles))

        when(mockFinancialsApiConnector.deleteNotification(any)(any))
          .thenReturn(Future.successful(true))

        when(mockDateTimeService.systemDateTime())
          .thenReturn(currentDate.atStartOfDay())

        running(app) {
          val request = fakeRequest(GET, routes.PostponedVatController.show(Some(cdsLocation)).url)
          val result  = route(app, request).value
          status(result) mustBe OK

          contentAsString(result) mustBe view(
            PostponedVatViewModel(
              postponedVatStatementFilesWithImmediateUnavailable,
              hasRequestedStatements = true,
              isCdsOnly = true,
              location = Some(cdsLocation),
              urls = pvatUrls
            )(messages, mockDateTimeService)
          )(request, messages, appConfig).toString()

          val doc           = Jsoup.parse(contentAsString(result))
          val periodElement = Formatters
            .dateAsMonthAndYear(date.minusMonths(ONE_MONTH))(messages)
            .replace(singleSpace, hyphen)
            .toLowerCase

          Option(doc.getElementById(s"period-$periodElement")) mustBe empty
        }
      }

    "display historic statements Url when feature is enabled" in new Setup {
      appConfigForSetup.historicStatementsEnabled = true

      val historicRequestUrl: String = appConfigForSetup.historicRequestUrl(PostponedVATStatement)
      val currentDate: LocalDate     = LocalDate.of(date.getYear, date.getMonthValue, DAY_12)

      when(mockDataStoreConnector.getEmail(any))
        .thenReturn(Future.successful(Right(Email(emailValue))))

      when(mockSdesConnector.getPostponedVatStatements(eqTo(eori))(any))
        .thenReturn(Future.successful(postponedVatStatementFilesWithImmediateUnavailable))

      when(mockSdesConnector.getPostponedVatStatements(eqTo(historicEori))(any))
        .thenReturn(Future.successful(historicPostponedVatStatementFiles))

      when(mockFinancialsApiConnector.deleteNotification(any)(any))
        .thenReturn(Future.successful(true))

      when(mockDateTimeService.systemDateTime())
        .thenReturn(currentDate.atStartOfDay())

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some(cdsLocation)).url)
        val result  = route(app, request).value
        status(result) mustBe OK

        contentAsString(result) mustBe view(
          PostponedVatViewModel(
            postponedVatStatementFilesWithImmediateUnavailable,
            hasRequestedStatements = true,
            isCdsOnly = true,
            location = Some(cdsLocation),
            urls = pvatUrls.copy(serviceUnavailableUrl = Some(historicRequestUrl))
          )(messages, mockDateTimeService)
        )(request, messages, appConfig).toString()
      }
    }
  }

  "statementsUnavailablePage" should {
    "display the view correctly" in {
      val app = applicationBuilder().build()

      val view      = instanceOf[postponed_import_vat_not_available](app)
      val navigator = instanceOf[Navigator](app)

      val serviceUnavailableUrl: String =
        routes.ServiceUnavailableController.onPageLoad(navigator.postponedVatNotAvailablePageId).url

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.statementsUnavailablePage().url)
        val result  = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe
          view(EORI_NUMBER, Some(serviceUnavailableUrl))(request, messages, appConfig).toString()
      }
    }
  }

  "show unavailable page" should {

    "when exception occurs in get postponedVAT statements api" in new Setup {

      appConfigForSetup.historicStatementsEnabled = false
      val serviceUnavailableUrl: String = routes.ServiceUnavailableController.onPageLoad("postponed-vat").url

      when(mockDataStoreConnector.getEmail(any))
        .thenReturn(Future.successful(Right(Email(emailValue))))

      when(mockSdesConnector.getPostponedVatStatements(any)(any))
        .thenReturn(Future.failed(new Exception("Unknown exception")))

      when(mockFinancialsApiConnector.deleteNotification(any)(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.PostponedVatController.show(Some(cdsLocation)).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        contentAsString(result).contains(serviceUnavailableUrl)
      }
    }
  }

  trait Setup {
    val eori: String               = "testEori1"
    val historicEori: String       = "testEori2"
    val emailValue: String         = "some@email.com"
    val cdsLocation                = "CDS"
    val statementRequestId: String = "statement-request-id"

    val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
    val mockSdesConnector: SdesConnector                   = mock[SdesConnector]
    val mockDataStoreConnector: DataStoreConnector         = mock[DataStoreConnector]
    val mockDateTimeService: DateTimeService               = mock[DateTimeService]

    val date: LocalDate = LocalDate.now()

    val postponedVatStatementFiles: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(SEVEN_MONTHS),
          monthValueOfCurrentDate(SEVEN_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(FOUR_MONTHS),
          monthValueOfCurrentDate(FOUR_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_1300000L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(FIVE_MONTHS),
          monthValueOfCurrentDate(FIVE_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(THREE_MONTHS),
          monthValueOfCurrentDate(THREE_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(TWO_MONTHS),
          monthValueOfCurrentDate(TWO_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(ONE_MONTH),
          monthValueOfCurrentDate(ONE_MONTH),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      )
    )

    val postponedVatStatementFilesWithCHIEF: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(SEVEN_MONTHS),
          monthValueOfCurrentDate(SEVEN_MONTHS),
          Pdf,
          PostponedVATStatement,
          CHIEF,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(FOUR_MONTHS),
          monthValueOfCurrentDate(FOUR_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_1300000L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(FIVE_MONTHS),
          monthValueOfCurrentDate(FIVE_MONTHS),
          Pdf,
          PostponedVATStatement,
          CHIEF,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(THREE_MONTHS),
          monthValueOfCurrentDate(THREE_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(TWO_MONTHS),
          monthValueOfCurrentDate(TWO_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(ONE_MONTH),
          monthValueOfCurrentDate(ONE_MONTH),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      )
    )

    val postponedVatStatementFilesWithImmediateUnavailable: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(SEVEN_MONTHS),
          monthValueOfCurrentDate(SEVEN_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(FOUR_MONTHS),
          monthValueOfCurrentDate(FOUR_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_1300000L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(FIVE_MONTHS),
          monthValueOfCurrentDate(FIVE_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(THREE_MONTHS),
          monthValueOfCurrentDate(THREE_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(TWO_MONTHS),
          monthValueOfCurrentDate(TWO_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      )
    )

    val currentStatements: Seq[PostponedVatStatementFile] = Seq(
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(ONE_MONTH),
          monthValueOfCurrentDate(ONE_MONTH),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(TWO_MONTHS),
          monthValueOfCurrentDate(TWO_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(THREE_MONTHS),
          monthValueOfCurrentDate(THREE_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(FOUR_MONTHS),
          monthValueOfCurrentDate(FOUR_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      ),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_1300000L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(FIVE_MONTHS),
          monthValueOfCurrentDate(FIVE_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          None
        ),
        eori
      )
    )

    val historicPostponedVatStatementFiles: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(EIGHT_MONTHS),
          monthValueOfCurrentDate(EIGHT_MONTHS),
          Pdf,
          PostponedVATStatement,
          CDS,
          Some(statementRequestId)
        ),
        historicEori
      )
    )

    val historicPostponedVatStatementFilesWithCHIEF: Seq[PostponedVatStatementFile] = List(
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_00,
        SIZE_4096L,
        PostponedVatStatementFileMetadata(
          yearValueOfCurrentDate(EIGHT_MONTHS),
          monthValueOfCurrentDate(EIGHT_MONTHS),
          Pdf,
          PostponedVATStatement,
          CHIEF,
          Some(statementRequestId)
        ),
        historicEori
      )
    )

    val app: Application =
      applicationBuilder(
        Seq(EoriHistory(historicEori, Some(date.minusYears(ONE_YEAR)), Some(date.minusMonths(SIX_MONTHS))))
      )
        .overrides(
          inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
          inject.bind[SdesConnector].toInstance(mockSdesConnector),
          inject.bind[DataStoreConnector].toInstance(mockDataStoreConnector),
          inject.bind[DateTimeService].toInstance(mockDateTimeService)
        )
        .build()

    val appConfigForSetup: AppConfig = instanceOf[AppConfig](app)

    val view: postponed_import_vat = instanceOf[postponed_import_vat](app)

    private def monthValueOfCurrentDate(monthValueToSubtract: Int): Int =
      LocalDate.now().minusMonths(monthValueToSubtract).getMonthValue

    private def yearValueOfCurrentDate(monthValueToSubtract: Int): Int =
      LocalDate.now().minusMonths(monthValueToSubtract).getYear

    val pvatUrls: PVATUrls = PVATUrls(
      customsFinancialsHomePageUrl = appConfigForSetup.customsFinancialsFrontendHomepage,
      requestStatementsUrl = appConfigForSetup.requestedStatements(PostponedVATStatement),
      pvEmail = PvEmail(appConfigForSetup.pvEmailEmailAddress, appConfigForSetup.pvEmailEmailAddressHref),
      viewVatAccountSupportLink = appConfigForSetup.viewVatAccountSupportLink,
      serviceUnavailableUrl = Some(routes.ServiceUnavailableController.onPageLoad("postponed-vat").url)
    )
  }
}
