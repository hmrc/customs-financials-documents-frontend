/*
 * Copyright 2021 HM Revenue & Customs
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

import config.AppConfig
import models.DutyPaymentMethod.CDS
import models.FileFormat.{Csv, Pdf}
import models.FileRole.{C79Certificate, PostponedVATStatement, SecurityStatement}
import models.metadata._
import models.{FileInformation, PostponedVatStatementFile, SecurityStatementFile, VatCertificateFile}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status
import play.api.i18n.Messages
import play.api.inject
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers._
import services.{AuditingService, MetricsReporterService, SdesGatekeeperService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.SpecBase

import scala.concurrent.Future

class SdesConnectorSpec extends SpecBase {
  "HttpSdesConnector" should {
    "getSecurityStatements" should {
      "make a GET request to sdesSecurityStatementsUrl" in new Setup {
        val url = sdesSecurityStatementsUrl
        val app = application().overrides(
          inject.bind[HttpClient].toInstance(mockHttp)
        ).build()
        val sdesService = app.injector.instanceOf[SdesConnector]
        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(Future.successful(HttpResponse(Status.OK, JsArray(Nil).toString())))

        await(sdesService.getSecurityStatements(someEori)(hc))
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }

      "filter out unknown file types" in new Setup {
        val url = sdesSecurityStatementsUrl
        val app = application().overrides(
          inject.bind[HttpClient].toInstance(mockHttp)
        ).build()
        val sdesService = app.injector.instanceOf[SdesConnector]
        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(securityStatementFilesWithUnkownFileTypesSdesResponse).toString())))

        val result = await(sdesService.getSecurityStatements(someEoriWithUnknownFileTypes)(hc))
        result mustBe (securityStatementFiles)
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }

      "converts Sdes response to List[SecurityStatementFile]" in new Setup {
        val url = sdesSecurityStatementsUrl
        val numberOfStatements = securityStatementFilesSdesResponse.length
        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(securityStatementFilesSdesResponse).toString())))

        when(sdesGatekeeperServiceSpy.convertTo(any)).thenCallRealMethod()

        val app = application().overrides(
          inject.bind[HttpClient].toInstance(mockHttp),
          inject.bind[SdesGatekeeperService].toInstance(sdesGatekeeperServiceSpy)
        ).build()
        val sdesService = app.injector.instanceOf[SdesConnector]

        await(sdesService.getSecurityStatements(someEori)(hc))
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
        verify(sdesGatekeeperServiceSpy, times(numberOfStatements)).convertToSecurityStatementFile(any)
      }
    }

    "getVatCertificates" should {
      "filter out unknown file types" in new Setup {
        val url = sdesVatCertificatesUrl
        val app = application().overrides(
          inject.bind[HttpClient].toInstance(mockHttp)
        ).build()
        val sdesService = app.injector.instanceOf[SdesConnector]
        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(vatCertificateFilesWithUnknownFileTypesSdesResponse).toString())))

        val result = await(sdesService.getVatCertificates(someEoriWithUnknownFileTypes)(hc, messages))
        result mustBe (vatCertificateFiles)
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }

      "converts Sdes response to List[VatCertificateFile]" in new Setup {
        val url = sdesVatCertificatesUrl
        val numberOfStatements = vatCertificateFilesSdesResponse.length
        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(vatCertificateFilesSdesResponse).toString())))

        when(sdesGatekeeperServiceSpy.convertTo(any)).thenCallRealMethod()

        val app = application().overrides(
          inject.bind[HttpClient].toInstance(mockHttp),
          inject.bind[SdesGatekeeperService].toInstance(sdesGatekeeperServiceSpy)
        ).build()
        val sdesService = app.injector.instanceOf[SdesConnector]

        await(sdesService.getVatCertificates(someEori)(hc, messages))
        verify(sdesGatekeeperServiceSpy, times(numberOfStatements)).convertToVatCertificateFile(any)(any)
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }
    }

    "getPostponedVatStatements" should {
      "filter out unknown file types" in new Setup {
        val url = sdesPostponedVatStatementsUrl
        val app = application().overrides(
          inject.bind[HttpClient].toInstance(mockHttp)
        ).build()
        val sdesService = app.injector.instanceOf[SdesConnector]

        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(postponedVatCertificateFilesWithUnknownFileTypesSdesResponse).toString())))

        val result = await(sdesService.getPostponedVatStatements(someEoriWithUnknownFileTypes)(hc))
        result mustBe (filteredPostponedVatCertificateFiles)
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }

      "converts Sdes response to List[PostponedVatCertificateFile]" in new Setup {
        val url = sdesPostponedVatStatementsUrl
        val numberOfStatements = postponedVatCertificateFilesSdesResponse.length
        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(postponedVatCertificateFilesSdesResponse).toString())))

        when(sdesGatekeeperServiceSpy.convertTo(any)).thenCallRealMethod()

        val app = application().overrides(
          inject.bind[HttpClient].toInstance(mockHttp),
          inject.bind[SdesGatekeeperService].toInstance(sdesGatekeeperServiceSpy)
        ).build()

        val sdesService = app.injector.instanceOf[SdesConnector]

        await(sdesService.getPostponedVatStatements(someEori)(hc))
        verify(sdesGatekeeperServiceSpy, times(numberOfStatements)).convertToPostponedVatCertificateFile(any)
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }
    }
  }

  trait Setup {
    val hc: HeaderCarrier = HeaderCarrier()
    implicit val messages: Messages = stubMessages()
    val someEori = "12345678"
    val someEoriWithUnknownFileTypes = "EoriFooBar"
    val someDan = "87654321"
    val xClientId = "TheClientId"
    val xClientIdHeader = "x-client-id"
    val xSDESKey = "X-SDES-Key"
    val sdesVatCertificatesUrl = "http://localhost:9754/customs-financials-sdes-stub/files-available/list/C79Certificate"
    val sdesPostponedVatStatementsUrl = "http://localhost:9754/customs-financials-sdes-stub/files-available/list/PostponedVATStatement"
    val sdesSecurityStatementsUrl = "http://localhost:9754/customs-financials-sdes-stub/files-available/list/SecurityStatement"

    val vatCertificateFiles = List(
      VatCertificateFile("name_04", "download_url_06", 111L, VatCertificateFileMetadata(2018, 3, Pdf, C79Certificate, None), ""),
      VatCertificateFile("name_04", "download_url_05", 111L, VatCertificateFileMetadata(2018, 4, Csv, C79Certificate, None), ""),
      VatCertificateFile("name_04", "download_url_04", 111L, VatCertificateFileMetadata(2018, 4, Pdf, C79Certificate, None), ""),
      VatCertificateFile("name_03", "download_url_03", 111L, VatCertificateFileMetadata(2018, 5, Pdf, C79Certificate, None), ""),
      VatCertificateFile("name_02", "download_url_02", 111L, VatCertificateFileMetadata(2018, 6, Csv, C79Certificate, None), ""),
      VatCertificateFile("name_01", "download_url_01", 1300000L, VatCertificateFileMetadata(2018, 6, Pdf, C79Certificate, None), "")
    )

    val postponedVatCertificateFiles = List(
      PostponedVatStatementFile("name_04", "download_url_06", 111L, PostponedVatStatementFileMetadata(2018, 3, Pdf, PostponedVATStatement, CDS, None), ""),
      PostponedVatStatementFile("name_04", "download_url_05", 111L, PostponedVatStatementFileMetadata(2018, 4, Csv, PostponedVATStatement, CDS, None), ""),
      PostponedVatStatementFile("name_04", "download_url_04", 111L, PostponedVatStatementFileMetadata(2018, 4, Pdf, PostponedVATStatement, CDS, None), ""),
      PostponedVatStatementFile("name_03", "download_url_03", 111L, PostponedVatStatementFileMetadata(2018, 5, Pdf, PostponedVATStatement, CDS, None), ""),
      PostponedVatStatementFile("name_02", "download_url_02", 111L, PostponedVatStatementFileMetadata(2018, 6, Csv, PostponedVATStatement, CDS, None), ""),
      PostponedVatStatementFile("name_01", "download_url_01", 1300000L, PostponedVatStatementFileMetadata(2018, 6, Pdf, PostponedVATStatement, CDS, None), "")
    )

    val filteredPostponedVatCertificateFiles = List(
      PostponedVatStatementFile("name_04", "download_url_06", 111L, PostponedVatStatementFileMetadata(2018, 3, Pdf, PostponedVATStatement, CDS, None), ""),
      PostponedVatStatementFile("name_04", "download_url_04", 111L, PostponedVatStatementFileMetadata(2018, 4, Pdf, PostponedVATStatement, CDS, None), ""),
      PostponedVatStatementFile("name_03", "download_url_03", 111L, PostponedVatStatementFileMetadata(2018, 5, Pdf, PostponedVATStatement, CDS, None), ""),
      PostponedVatStatementFile("name_01", "download_url_01", 1300000L, PostponedVatStatementFileMetadata(2018, 6, Pdf, PostponedVATStatement, CDS, None), "")
    )

    val vatCertificateFilesSdesResponse = List(
      FileInformation("name_04", "download_url_06", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "C79Certificate")))),
      FileInformation("name_04", "download_url_05", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "4"), MetadataItem("FileType", "CSV"), MetadataItem("FileRole", "C79Certificate")))),
      FileInformation("name_04", "download_url_04", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "4"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "C79Certificate")))),
      FileInformation("name_03", "download_url_03", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "5"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "C79Certificate")))),
      FileInformation("name_02", "download_url_02", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "csv"), MetadataItem("FileRole", "C79Certificate")))),
      FileInformation("name_01", "download_url_01", 1300000L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "PDF"), MetadataItem("FileRole", "C79Certificate"))))
    )


    val postponedVatCertificateFilesSdesResponse = List(
      FileInformation("name_04", "download_url_06", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "Immediate")))),
      FileInformation("name_04", "download_url_05", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "4"), MetadataItem("FileType", "CSV"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "Immediate")))),
      FileInformation("name_04", "download_url_04", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "4"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "Immediate")))),
      FileInformation("name_03", "download_url_03", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "5"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "Immediate")))),
      FileInformation("name_02", "download_url_02", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "csv"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "Immediate")))),
      FileInformation("name_01", "download_url_01", 1300000L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "PDF"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "Immediate"))))
    )

    val vatCertificateFilesWithUnknownFileTypesSdesResponse = List(
      FileInformation("name_04", "download_url_06", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("FileType", "foo"), MetadataItem("FileRole", "C79Certificate"))))) ++
      vatCertificateFilesSdesResponse ++
      List(FileInformation("name_01", "download_url_01", 1300000L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "bar"), MetadataItem("FileRole", "C79Certificate")))))

    val postponedVatCertificateFilesWithUnknownFileTypesSdesResponse = List(
      FileInformation("name_04", "download_url_06", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("FileType", "foo"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "Immediate"))))) ++
      postponedVatCertificateFilesSdesResponse ++
      List(FileInformation("name_01", "download_url_01", 1300000L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "bar"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "Immediate")))))

    val securityStatementFiles = List(
      SecurityStatementFile("name_01", "download_url_01", 111L, SecurityStatementFileMetadata(2018, 3, 14, 2018, 3, 23, Csv, SecurityStatement, someEori, 111L, "checksum_01", None)),
      SecurityStatementFile("name_01", "download_url_01", 111L, SecurityStatementFileMetadata(2018, 3, 14, 2018, 3, 23, Pdf, SecurityStatement, someEori, 111L, "checksum_01", None))
    )

    val securityStatementFilesSdesResponse = List(
      FileInformation("name_01", "download_url_01", 111L, Metadata(List(
        MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("PeriodStartDay", "14"), MetadataItem("PeriodEndYear", "2018"),
        MetadataItem("PeriodEndMonth", "3"), MetadataItem("PeriodEndDay", "23"), MetadataItem("FileType", "CSV"), MetadataItem("FileRole", "SecurityStatement"),
        MetadataItem("eoriNumber", someEori), MetadataItem("fileSize", "111"), MetadataItem("checksum", "checksum_01"), MetadataItem("issueDate", "3/4/2018")))),
      FileInformation("name_01", "download_url_01", 111L, Metadata(List(
        MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("PeriodStartDay", "14"), MetadataItem("PeriodEndYear", "2018"),
        MetadataItem("PeriodEndMonth", "3"), MetadataItem("PeriodEndDay", "23"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "SecurityStatement"),
        MetadataItem("eoriNumber", someEori), MetadataItem("fileSize", "111"), MetadataItem("checksum", "checksum_01"), MetadataItem("issueDate", "3/4/2018"))))
    )

    val securityStatementFilesWithUnkownFileTypesSdesResponse =
      List(FileInformation("name_01", "download_url_01", 111L, Metadata(List(
        MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("PeriodStartDay", "14"), MetadataItem("PeriodEndYear", "2018"),
        MetadataItem("PeriodEndMonth", "3"), MetadataItem("PeriodEndDay", "23"), MetadataItem("FileType", "foo"), MetadataItem("FileRole", "SecurityStatement"),
        MetadataItem("eoriNumber", someEori), MetadataItem("fileSize", "111"), MetadataItem("checksum", "checksum_01"), MetadataItem("issueDate", "3/4/2018"))))) ++
        securityStatementFilesSdesResponse ++
        List(FileInformation("name_01", "download_url_01", 111L, Metadata(List(
          MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("PeriodStartDay", "14"), MetadataItem("PeriodEndYear", "2018"),
          MetadataItem("PeriodEndMonth", "3"), MetadataItem("PeriodEndDay", "23"), MetadataItem("FileType", "bar"), MetadataItem("FileRole", "SecurityStatement"),
          MetadataItem("eoriNumber", someEori), MetadataItem("fileSize", "111"), MetadataItem("checksum", "checksum_01"), MetadataItem("issueDate", "3/4/2018")))))

    val sdesGatekeeperServiceSpy = spy(new SdesGatekeeperService())
    val mockHttp = mock[HttpClient]
    val mockAppConfig = mock[AppConfig]
    val mockMetricsReporterService = mock[MetricsReporterService]
    val mockAuditingService = mock[AuditingService]
  }
}
