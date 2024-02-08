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

import config.AppConfig
import models.DutyPaymentMethod.CDS
import models.FileFormat.{Csv, Pdf}
import models.FileRole.{C79Certificate, PostponedVATStatement, SecurityStatement}
import models._
import models.metadata._
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers._
import play.api.{Application, inject}
import services.{AuditingService, MetricsReporterService, SdesGatekeeperService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import utils.SpecBase
import utils.Utils.emptyString

import scala.concurrent.Future

class SdesConnectorSpec extends SpecBase {

  "HttpSdesConnector" should {

    "getSecurityStatements" should {

      "make a GET request to sdesSecurityStatementsUrl" in new Setup {
        val url: String = sdesSecurityStatementsUrl
        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(Future.successful(HttpResponse(Status.OK, JsArray(Nil).toString())))

        await(sdesService.getSecurityStatements(someEori)(hc))
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }

      "filter out unknown file types" in new Setup {
        val url: String = sdesSecurityStatementsUrl
        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(
            Future.successful(
              HttpResponse(Status.OK, Json.toJson(securityStatementFilesWithUnkownFileTypesSdesResponse).toString())
            )
          )

        val result: Seq[SecurityStatementFile] =
          await(sdesService.getSecurityStatements(someEoriWithUnknownFileTypes)(hc))

        result mustBe (securityStatementFiles)
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }

      "converts Sdes response to List[SecurityStatementFile]" in new Setup {
        val url: String = sdesSecurityStatementsUrl
        val numberOfStatements: Int = securityStatementFilesSdesResponse.length

        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(
            Future.successful(HttpResponse(Status.OK, Json.toJson(securityStatementFilesSdesResponse).toString()))
          )

        when(sdesGatekeeperServiceSpy.convertTo(any)).thenCallRealMethod()

        override val app: Application = application().overrides(
          inject.bind[HttpClient].toInstance(mockHttp),
          inject.bind[SdesGatekeeperService].toInstance(sdesGatekeeperServiceSpy)
        ).build()

        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        await(sdesService.getSecurityStatements(someEori)(hc))

        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
        verify(sdesGatekeeperServiceSpy, times(numberOfStatements)).convertToSecurityStatementFile(any)
      }
    }

    "getVatCertificates" should {

      "filter out unknown file types" in new Setup {
        val url: String = sdesVatCertificatesUrl
        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(
            Future.successful(
              HttpResponse(Status.OK, Json.toJson(vatCertificateFilesWithUnknownFileTypesSdesResponse).toString())
            )
          )

        val result: Seq[VatCertificateFile] =
          await(sdesService.getVatCertificates(someEoriWithUnknownFileTypes)(hc, messages))

        result mustBe (vatCertificateFiles)
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }

      "converts Sdes response to List[VatCertificateFile]" in new Setup {
        val url: String = sdesVatCertificatesUrl
        val numberOfStatements: Int = vatCertificateFilesSdesResponse.length

        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(
            Future.successful(
              HttpResponse(Status.OK, Json.toJson(vatCertificateFilesSdesResponse).toString())
            )
          )

        when(sdesGatekeeperServiceSpy.convertTo(any)).thenCallRealMethod()

        override val app: Application = application().overrides(
          inject.bind[HttpClient].toInstance(mockHttp),
          inject.bind[SdesGatekeeperService].toInstance(sdesGatekeeperServiceSpy)
        ).build()

        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        await(sdesService.getVatCertificates(someEori)(hc, messages))

        verify(sdesGatekeeperServiceSpy, times(numberOfStatements)).convertToVatCertificateFile(any)(any)
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }
    }

    "getPostponedVatStatements" should {

      "filter out unknown file types" in new Setup {
        val url: String = sdesPostponedVatStatementsUrl
        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(
            Future.successful(
              HttpResponse(
                Status.OK,
                Json.toJson(postponedVatCertificateFilesWithUnknownFileTypesSdesResponse).toString())
            )
          )

        val result: Seq[PostponedVatStatementFile] =
          await(sdesService.getPostponedVatStatements(someEoriWithUnknownFileTypes)(hc))

        result mustBe (filteredPostponedVatCertificateFiles)
        verify(mockHttp).GET(eqTo(url), any, any)(any, any, any)
      }

      "converts Sdes response to List[PostponedVatCertificateFile]" in new Setup {
        val url: String = sdesPostponedVatStatementsUrl
        val numberOfStatements: Int = postponedVatCertificateFilesSdesResponse.length

        when[Future[HttpResponse]](mockHttp.GET(eqTo(url), any, any)(any, any, any))
          .thenReturn(
            Future.successful(
              HttpResponse(Status.OK, Json.toJson(postponedVatCertificateFilesSdesResponse).toString())
            )
          )

        when(sdesGatekeeperServiceSpy.convertTo(any)).thenCallRealMethod()

        override val app: Application = application().overrides(
          inject.bind[HttpClient].toInstance(mockHttp),
          inject.bind[SdesGatekeeperService].toInstance(sdesGatekeeperServiceSpy)
        ).build()

        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

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
    val sdesPostponedVatStatementsUrl =
      "http://localhost:9754/customs-financials-sdes-stub/files-available/list/PostponedVATStatement"

    val sdesSecurityStatementsUrl =
      "http://localhost:9754/customs-financials-sdes-stub/files-available/list/SecurityStatement"

    val year2018 = 2018
    val month3 = 3
    val month4 = 4
    val month5 = 5
    val month6 = 6

    val startMonth3 = 3
    val startMonth4 = 4
    val startMonth5 = 5
    val startMonth6 = 6

    val day14 = 14
    val day23 = 23

    val fileName1 = "name_01"
    val fileName2 = "name_02"
    val fileName3 = "name_03"
    val fileName4 = "name_04"

    val downloadUrl1 = "download_url_01"
    val downloadUrl2 = "download_url_02"
    val downloadUrl3 = "download_url_03"
    val downloadUrl4 = "download_url_04"
    val downloadUrl5 = "download_url_05"
    val downloadUrl6 = "download_url_06"

    val checkSum01Str: String = "checksum_01"

    val periodStartYear: MetadataItem = MetadataItem("PeriodStartYear", "2018")
    val periodStartMonth: MetadataItem = MetadataItem("PeriodStartMonth", "3")
    val periodStartMonth4: MetadataItem = MetadataItem("PeriodStartMonth", "4")
    val periodStartMonth5: MetadataItem = MetadataItem("PeriodStartMonth", "5")
    val periodStartMonth6: MetadataItem = MetadataItem("PeriodStartMonth", "6")
    val periodStartDay: MetadataItem = MetadataItem("PeriodStartDay", "14")
    val periodEndYear: MetadataItem = MetadataItem("PeriodEndYear", "2018")
    val periodEndMonth: MetadataItem = MetadataItem("PeriodEndMonth", "3")
    val periodEndDay: MetadataItem = MetadataItem("PeriodEndDay", "23")
    val fileType: MetadataItem = MetadataItem("FileType", "bar")
    val fileTypePdf: MetadataItem = MetadataItem("FileType", "pdf")
    val fileTypeCsv: MetadataItem = MetadataItem("FileType", "CSV")
    val fileTypeFoo: MetadataItem = MetadataItem("FileType", "foo")
    val fileRole: MetadataItem = MetadataItem("FileRole", "SecurityStatement")
    val fileRolePVATStatement: MetadataItem = MetadataItem("FileRole", "PostponedVATStatement")
    val fileRoleC79Cert: MetadataItem = MetadataItem("FileRole", "C79Certificate")
    val eoriNumber: MetadataItem = MetadataItem("eoriNumber", someEori)
    val fileSize: MetadataItem = MetadataItem("fileSize", "111")
    val checksum: MetadataItem = MetadataItem("checksum", checkSum01Str)
    val issueDate: MetadataItem = MetadataItem("issueDate", "3/4/2018")
    val dutyPaymentMethod: MetadataItem = MetadataItem("DutyPaymentMethod", "Immediate")

    val size = 111L
    val size1300000 = 1300000L

    val vatCertificateFiles: List[VatCertificateFile] = List(
      VatCertificateFile(
        fileName4,
        downloadUrl6,
        size,
        getVatCertificateFileMetadata(), emptyString),
      VatCertificateFile(
        fileName4,
        downloadUrl5,
        size,
        getVatCertificateFileMetadata(periodStartMonth = month4, fileFormat = Csv),
        emptyString),
      VatCertificateFile(
        fileName4,
        downloadUrl4,
        size,
        VatCertificateFileMetadata(year2018, startMonth4, Pdf, C79Certificate, None),
        emptyString),
      VatCertificateFile(
        fileName3,
        downloadUrl3,
        size,
        VatCertificateFileMetadata(year2018, startMonth5, Pdf, C79Certificate, None),
        emptyString),
      VatCertificateFile(
        fileName2,
        downloadUrl2,
        size,
        VatCertificateFileMetadata(year2018, startMonth6, Csv, C79Certificate, None),
        emptyString),
      VatCertificateFile(
        fileName1,
        downloadUrl1,
        size1300000,
        VatCertificateFileMetadata(year2018, startMonth6, Pdf, C79Certificate, None),
        emptyString)
    )

    val postponedVatCertificateFiles: List[PostponedVatStatementFile] = List(
      PostponedVatStatementFile(
        fileName4,
        downloadUrl6,
        size,
        PostponedVatStatementFileMetadata(year2018, startMonth3, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        fileName4,
        downloadUrl5,
        size,
        PostponedVatStatementFileMetadata(year2018, startMonth4, Csv, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        fileName4,
        downloadUrl4,
        size,
        PostponedVatStatementFileMetadata(year2018, startMonth4, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        fileName3,
        downloadUrl3,
        size,
        PostponedVatStatementFileMetadata(year2018, startMonth5, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        fileName2,
        downloadUrl2,
        size,
        PostponedVatStatementFileMetadata(year2018, startMonth6, Csv, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        fileName1,
        downloadUrl1,
        size1300000,
        PostponedVatStatementFileMetadata(year2018, startMonth6, Pdf, PostponedVATStatement, CDS, None),
        emptyString)
    )

    val filteredPostponedVatCertificateFiles: List[PostponedVatStatementFile] = List(
      PostponedVatStatementFile(
        fileName4,
        downloadUrl6,
        size,
        PostponedVatStatementFileMetadata(year2018, startMonth3, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        fileName4,
        downloadUrl4,
        size,
        PostponedVatStatementFileMetadata(year2018, startMonth4, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        fileName3,
        downloadUrl3,
        size,
        PostponedVatStatementFileMetadata(year2018, startMonth5, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        fileName1,
        downloadUrl1,
        size1300000,
        PostponedVatStatementFileMetadata(year2018, startMonth6, Pdf, PostponedVATStatement, CDS, None),
        emptyString)
    )

    val vatCertificateFilesSdesResponse: List[FileInformation] = List(
      FileInformation(
        fileName4,
        downloadUrl6,
        size,
        Metadata(List(periodStartYear, periodStartMonth, fileTypePdf, fileRoleC79Cert))
      ),
      FileInformation(
        fileName4,
        downloadUrl5,
        size,
        Metadata(List(periodStartYear, periodStartMonth4, fileTypeCsv, fileRoleC79Cert))
      ),
      FileInformation(
        fileName4,
        downloadUrl4,
        size,
        Metadata(List(periodStartYear, periodStartMonth4, fileTypePdf, fileRoleC79Cert))
      ),
      FileInformation(
        fileName3,
        downloadUrl3,
        size,
        Metadata(List(periodStartYear, periodStartMonth5, fileTypePdf, fileRoleC79Cert))
      ),
      FileInformation(
        fileName2,
        downloadUrl2,
        size,
        Metadata(List(periodStartYear, periodStartMonth6, fileTypeCsv, fileRoleC79Cert))
      ),
      FileInformation(
        fileName1,
        downloadUrl1,
        size1300000,
        Metadata(List(periodStartYear, periodStartMonth6, fileTypePdf, fileRoleC79Cert))
      )
    )

    val postponedVatCertificateFilesSdesResponse: List[FileInformation] = List(
      FileInformation(
        fileName4,
        downloadUrl6,
        size,
        Metadata(List(periodStartYear, periodStartMonth, fileTypePdf, fileRolePVATStatement, dutyPaymentMethod))
      ),
      FileInformation(
        fileName4,
        downloadUrl5,
        size,
        Metadata(List(periodStartYear, periodStartMonth4, fileTypeCsv, fileRolePVATStatement, dutyPaymentMethod))
      ),
      FileInformation(
        fileName4,
        downloadUrl4,
        size,
        Metadata(List(periodStartYear, periodStartMonth4, fileTypePdf, fileRolePVATStatement, dutyPaymentMethod))
      ),
      FileInformation(
        fileName3,
        downloadUrl3,
        size,
        Metadata(List(periodStartYear, periodStartMonth5, fileTypePdf, fileRolePVATStatement, dutyPaymentMethod))
      ),
      FileInformation(
        fileName2,
        downloadUrl2,
        size,
        Metadata(List(periodStartYear, periodStartMonth6, fileTypeCsv, fileRolePVATStatement, dutyPaymentMethod))
      ),
      FileInformation(
        fileName1,
        downloadUrl1,
        size1300000,
        Metadata(List(periodStartYear, periodStartMonth6, fileTypePdf, fileRolePVATStatement, dutyPaymentMethod))
      )
    )

    val vatCertificateFilesWithUnknownFileTypesSdesResponse: List[FileInformation] =
      List(FileInformation(
        fileName4,
        downloadUrl6,
        size,
        Metadata(List(periodStartYear, periodStartMonth, fileTypeFoo, fileRoleC79Cert))
      )) ++ vatCertificateFilesSdesResponse ++
      List(FileInformation(
        fileName1,
        downloadUrl1,
        size1300000,
        Metadata(List(periodStartYear, periodStartMonth6, fileType, fileRoleC79Cert)))
      )

    val postponedVatCertificateFilesWithUnknownFileTypesSdesResponse: List[FileInformation] =
      List(
        FileInformation(
          fileName4,
          downloadUrl6,
          size,
          Metadata(List(periodStartYear, periodStartMonth, fileTypeFoo, fileRolePVATStatement, dutyPaymentMethod))
        )
      ) ++ postponedVatCertificateFilesSdesResponse ++
        List(
          FileInformation(
            fileName1,
            downloadUrl1,
            size1300000,
            Metadata(List(periodStartYear, periodStartMonth6, fileType, fileRolePVATStatement, dutyPaymentMethod)))
        )

    val securityStatementFiles: List[SecurityStatementFile] =
      List(
        SecurityStatementFile(
          fileName1,
          downloadUrl1,
          size,
          SecurityStatementFileMetadata(
            year2018,
            startMonth3,
            day14,
            year2018,
            startMonth3,
            day23,
            Csv,
            SecurityStatement, someEori, size, checkSum01Str, None)
        ),
        SecurityStatementFile(
          fileName1,
          downloadUrl1,
          size,
          SecurityStatementFileMetadata(
            year2018,
            startMonth3,
            day14,
            year2018,
            startMonth3,
            day23,
            Pdf,
            SecurityStatement, someEori, size, checkSum01Str, None)
        )
      )

    val securityStatementFilesSdesResponse: List[FileInformation] =
      List(
        FileInformation(
          fileName1,
          downloadUrl1,
          size,
          Metadata(List(periodStartYear, periodStartMonth, periodStartDay, periodEndYear, periodEndMonth, periodEndDay,
            fileTypeCsv, fileRole, eoriNumber, fileSize, checksum, issueDate))
        ),
        FileInformation(
          fileName1,
          downloadUrl1,
          size,
          Metadata(List(periodStartYear, periodStartMonth, periodStartDay, periodEndYear, periodEndMonth, periodEndDay,
            fileTypePdf, fileRole, eoriNumber, fileSize, checksum, issueDate))
        )
      )

    val securityStatementFilesWithUnkownFileTypesSdesResponse: List[FileInformation] =
      List(
        FileInformation(
          fileName1,
          downloadUrl1,
          size,
          Metadata(List(periodStartYear, periodStartMonth, periodStartDay, periodEndYear, periodEndMonth, periodEndDay,
            fileTypeFoo, fileRole, eoriNumber, fileSize, checksum, issueDate))
        )
      ) ++ securityStatementFilesSdesResponse ++
        List(
          FileInformation(
            fileName1,
            downloadUrl1,
            size,
            Metadata(List(periodStartYear, periodStartMonth, periodStartDay, periodEndYear, periodEndMonth, periodEndDay,
              fileType, fileRole, eoriNumber, fileSize, checksum, issueDate))
          )
        )

    val sdesGatekeeperServiceSpy: SdesGatekeeperService = spy(new SdesGatekeeperService())
    val mockHttp: HttpClient = mock[HttpClient]
    val mockAppConfig: AppConfig = mock[AppConfig]
    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]
    val mockAuditingService: AuditingService = mock[AuditingService]

    val app: Application = application().overrides(inject.bind[HttpClient].toInstance(mockHttp)).build()

    private def getVatCertificateFileMetadata(periodStartYear: Int = year2018,
                                              periodStartMonth: Int = month3,
                                              fileFormat: FileFormat = Pdf,
                                              fileRole: FileRole = C79Certificate,
                                              statementRequestId: Option[String] = None) =
      VatCertificateFileMetadata(periodStartYear, periodStartMonth, fileFormat, fileRole, statementRequestId)
  }
}
