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
import models.*
import models.metadata.*
import org.scalatest.matchers.must.Matchers.mustBe
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{spy, times, verify, when}
import org.mockito.ArgumentMatchers.{eq => eqTo}
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, Json}
import play.api.test.Helpers.*
import play.api.{Application, inject}
import services.{AuditingService, MetricsReporterService, SdesGatekeeperService}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.CommonTestData.*
import utils.SpecBase
import utils.Utils.emptyString

import java.net.URL
import scala.concurrent.Future

class SdesConnectorSpec extends SpecBase {

  "HttpSdesConnector" should {

    "getSecurityStatements" should {

      "make a GET request to sdesSecurityStatementsUrl" in new Setup {

        val urlLink: URL = url"$sdesSecurityStatementsUrl"
        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any))
          .thenReturn(Future.successful(HttpResponse(Status.OK, JsArray(Nil).toString())))

        await(sdesService.getSecurityStatements(someEori)(hc))
        verify(mockHttp).get(eqTo(urlLink))(any[HeaderCarrier])
      }

      "filter out unknown file types" in new Setup {

        val urlLink: URL = url"$sdesSecurityStatementsUrl"
        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any))
          .thenReturn(Future.successful(
            HttpResponse(Status.OK, Json.toJson(securityStatementFilesWithUnkownFileTypesSdesResponse).toString())
          ))

        val result: Seq[SecurityStatementFile] =
          await(sdesService.getSecurityStatements(someEoriWithUnknownFileTypes)(hc))

        result mustBe securityStatementFiles
        verify(mockHttp).get(eqTo(urlLink))(any[HeaderCarrier])
      }

      "converts Sdes response to List[SecurityStatementFile]" in new Setup {

        val urlLink: URL = url"$sdesSecurityStatementsUrl"
        val numberOfStatements: Int = securityStatementFilesSdesResponse.length

        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any))
          .thenReturn(
            Future.successful(HttpResponse(Status.OK, Json.toJson(securityStatementFilesSdesResponse).toString()))
          )

        when(sdesGatekeeperServiceSpy.convertTo(any())).thenCallRealMethod()

        override val app: Application = application().overrides(
          inject.bind[HttpClientV2].toInstance(mockHttp),
          inject.bind[SdesGatekeeperService].toInstance(sdesGatekeeperServiceSpy)
        ).build()

        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        await(sdesService.getSecurityStatements(someEori)(hc))

        verify(mockHttp).get(eqTo(urlLink))(any[HeaderCarrier])
        verify(sdesGatekeeperServiceSpy, times(numberOfStatements)).convertToSecurityStatementFile(any)
      }
    }

    "getVatCertificates" should {

      "filter out unknown file types" in new Setup {

        val urlLink = url"$sdesVatCertificatesUrl"
        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any))
          .thenReturn(Future.successful(
            HttpResponse(Status.OK, Json.toJson(vatCertificateFilesWithUnknownFileTypesSdesResponse).toString())
          ))

        val result: Seq[VatCertificateFile] =
          await(sdesService.getVatCertificates(someEoriWithUnknownFileTypes)(hc, messages))

        result mustBe vatCertificateFiles
        verify(mockHttp).get(eqTo(urlLink))(any[HeaderCarrier])
      }

      "converts Sdes response to List[VatCertificateFile]" in new Setup {
        val urlLink: URL = url"$sdesVatCertificatesUrl"
        val numberOfStatements: Int = vatCertificateFilesSdesResponse.length

        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any))
          .thenReturn(Future.successful(
            HttpResponse(Status.OK, Json.toJson(vatCertificateFilesSdesResponse).toString())
          ))

        when(sdesGatekeeperServiceSpy.convertTo(any())).thenCallRealMethod()

        override val app: Application = application().overrides(
          inject.bind[HttpClientV2].toInstance(mockHttp),
          inject.bind[SdesGatekeeperService].toInstance(sdesGatekeeperServiceSpy)
        ).build()

        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        await(sdesService.getVatCertificates(someEori)(hc, messages))

        verify(sdesGatekeeperServiceSpy, times(numberOfStatements)).convertToVatCertificateFile(any)(any)
        verify(mockHttp).get(eqTo(urlLink))(any[HeaderCarrier])
      }
    }

    "getPostponedVatStatements" should {

      "filter out unknown file types" in new Setup {
        val urlLink: URL = url"$sdesPostponedVatStatementsUrl"
        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any))
          .thenReturn(Future.successful(
            HttpResponse(
              Status.OK,
              Json.toJson(postponedVatCertificateFilesWithUnknownFileTypesSdesResponse).toString())
          ))

        val result: Seq[PostponedVatStatementFile] =
          await(sdesService.getPostponedVatStatements(someEoriWithUnknownFileTypes)(hc))

        result mustBe filteredPostponedVatCertificateFiles
        verify(mockHttp).get(eqTo(urlLink))(any[HeaderCarrier])
      }

      "converts Sdes response to List[PostponedVatCertificateFile]" in new Setup {

        val urlLink: URL = url"$sdesPostponedVatStatementsUrl"
        val numberOfStatements: Int = postponedVatCertificateFilesSdesResponse.length

        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttp.get(any)(any)).thenReturn(requestBuilder)
        when(requestBuilder.execute(any, any))
          .thenReturn(Future.successful(
            HttpResponse(Status.OK, Json.toJson(postponedVatCertificateFilesSdesResponse).toString())
          ))

        when(sdesGatekeeperServiceSpy.convertTo(any())).thenCallRealMethod()

        override val app: Application = application().overrides(
          inject.bind[HttpClientV2].toInstance(mockHttp),
          inject.bind[SdesGatekeeperService].toInstance(sdesGatekeeperServiceSpy)
        ).build()

        val sdesService: SdesConnector = app.injector.instanceOf[SdesConnector]

        await(sdesService.getPostponedVatStatements(someEori)(hc))

        verify(sdesGatekeeperServiceSpy, times(numberOfStatements)).convertToPostponedVatCertificateFile(any)
        verify(mockHttp).get(eqTo(urlLink))(any[HeaderCarrier])
      }
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
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
    val checksum: MetadataItem = MetadataItem("checksum", CHECK_SUM_01)
    val issueDate: MetadataItem = MetadataItem("issueDate", "3/4/2018")
    val dutyPaymentMethod: MetadataItem = MetadataItem("DutyPaymentMethod", "Immediate")

    val vatCertificateFiles: List[VatCertificateFile] = List(
      VatCertificateFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        getVatCertificateFileMetadata(), emptyString),
      VatCertificateFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_05,
        SIZE_111L,
        getVatCertificateFileMetadata(periodStartMonth = MONTH_4, fileFormat = Csv),
        emptyString),
      VatCertificateFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_04,
        SIZE_111L,
        VatCertificateFileMetadata(YEAR_2018, MONTH_4, Pdf, C79Certificate, None),
        emptyString),
      VatCertificateFile(
        STAT_FILE_NAME_03,
        DOWNLOAD_URL_03,
        SIZE_111L,
        VatCertificateFileMetadata(YEAR_2018, MONTH_5, Pdf, C79Certificate, None),
        emptyString),
      VatCertificateFile(
        STAT_FILE_NAME_02,
        DOWNLOAD_URL_02,
        SIZE_111L,
        VatCertificateFileMetadata(YEAR_2018, MONTH_6, Csv, C79Certificate, None),
        emptyString),
      VatCertificateFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_01,
        SIZE_1300000L,
        VatCertificateFileMetadata(YEAR_2018, MONTH_6, Pdf, C79Certificate, None),
        emptyString)
    )

    val postponedVatCertificateFiles: List[PostponedVatStatementFile] = List(
      PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_3, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_05,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_4, Csv, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_04,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_4, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        STAT_FILE_NAME_03,
        DOWNLOAD_URL_03,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_5, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        STAT_FILE_NAME_02,
        DOWNLOAD_URL_02,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_6, Csv, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_01,
        SIZE_1300000L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_6, Pdf, PostponedVATStatement, CDS, None),
        emptyString)
    )

    val filteredPostponedVatCertificateFiles: List[PostponedVatStatementFile] = List(
      PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_3, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_04,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_4, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        STAT_FILE_NAME_03,
        DOWNLOAD_URL_03,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_5, Pdf, PostponedVATStatement, CDS, None),
        emptyString),
      PostponedVatStatementFile(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_01,
        SIZE_1300000L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_6, Pdf, PostponedVATStatement, CDS, None),
        emptyString)
    )

    val vatCertificateFilesSdesResponse: List[FileInformation] = List(
      FileInformation(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth, fileTypePdf, fileRoleC79Cert))
      ),
      FileInformation(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_05,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth4, fileTypeCsv, fileRoleC79Cert))
      ),
      FileInformation(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_04,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth4, fileTypePdf, fileRoleC79Cert))
      ),
      FileInformation(
        STAT_FILE_NAME_03,
        DOWNLOAD_URL_03,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth5, fileTypePdf, fileRoleC79Cert))
      ),
      FileInformation(
        STAT_FILE_NAME_02,
        DOWNLOAD_URL_02,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth6, fileTypeCsv, fileRoleC79Cert))
      ),
      FileInformation(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_01,
        SIZE_1300000L,
        Metadata(List(periodStartYear, periodStartMonth6, fileTypePdf, fileRoleC79Cert))
      )
    )

    val postponedVatCertificateFilesSdesResponse: List[FileInformation] = List(
      FileInformation(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth, fileTypePdf, fileRolePVATStatement, dutyPaymentMethod))
      ),
      FileInformation(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_05,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth4, fileTypeCsv, fileRolePVATStatement, dutyPaymentMethod))
      ),
      FileInformation(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_04,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth4, fileTypePdf, fileRolePVATStatement, dutyPaymentMethod))
      ),
      FileInformation(
        STAT_FILE_NAME_03,
        DOWNLOAD_URL_03,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth5, fileTypePdf, fileRolePVATStatement, dutyPaymentMethod))
      ),
      FileInformation(
        STAT_FILE_NAME_02,
        DOWNLOAD_URL_02,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth6, fileTypeCsv, fileRolePVATStatement, dutyPaymentMethod))
      ),
      FileInformation(
        STAT_FILE_NAME_01,
        DOWNLOAD_URL_01,
        SIZE_1300000L,
        Metadata(List(periodStartYear, periodStartMonth6, fileTypePdf, fileRolePVATStatement, dutyPaymentMethod))
      )
    )

    val vatCertificateFilesWithUnknownFileTypesSdesResponse: List[FileInformation] =
      List(FileInformation(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        Metadata(List(periodStartYear, periodStartMonth, fileTypeFoo, fileRoleC79Cert))
      )) ++ vatCertificateFilesSdesResponse ++
        List(FileInformation(
          STAT_FILE_NAME_01,
          DOWNLOAD_URL_01,
          SIZE_1300000L,
          Metadata(List(periodStartYear, periodStartMonth6, fileType, fileRoleC79Cert)))
        )

    val postponedVatCertificateFilesWithUnknownFileTypesSdesResponse: List[FileInformation] =
      List(
        FileInformation(
          STAT_FILE_NAME_04,
          DOWNLOAD_URL_06,
          SIZE_111L,
          Metadata(List(periodStartYear, periodStartMonth, fileTypeFoo, fileRolePVATStatement, dutyPaymentMethod))
        )
      ) ++ postponedVatCertificateFilesSdesResponse ++
        List(
          FileInformation(
            STAT_FILE_NAME_01,
            DOWNLOAD_URL_01,
            SIZE_1300000L,
            Metadata(List(periodStartYear, periodStartMonth6, fileType, fileRolePVATStatement, dutyPaymentMethod)))
        )

    val securityStatementFiles: List[SecurityStatementFile] =
      List(
        SecurityStatementFile(
          STAT_FILE_NAME_01,
          DOWNLOAD_URL_01,
          SIZE_111L,
          SecurityStatementFileMetadata(
            YEAR_2018,
            MONTH_3,
            DAY_14,
            YEAR_2018,
            MONTH_3,
            DAY_23,
            Csv,
            SecurityStatement, someEori, SIZE_111L, CHECK_SUM_01, None)
        ),
        SecurityStatementFile(
          STAT_FILE_NAME_01,
          DOWNLOAD_URL_01,
          SIZE_111L,
          SecurityStatementFileMetadata(
            YEAR_2018,
            MONTH_3,
            DAY_14,
            YEAR_2018,
            MONTH_3,
            DAY_23,
            Pdf,
            SecurityStatement, someEori, SIZE_111L, CHECK_SUM_01, None)
        )
      )

    val securityStatementFilesSdesResponse: List[FileInformation] =
      List(
        FileInformation(
          STAT_FILE_NAME_01,
          DOWNLOAD_URL_01,
          SIZE_111L,
          Metadata(List(periodStartYear, periodStartMonth, periodStartDay, periodEndYear, periodEndMonth, periodEndDay,
            fileTypeCsv, fileRole, eoriNumber, fileSize, checksum, issueDate))
        ),
        FileInformation(
          STAT_FILE_NAME_01,
          DOWNLOAD_URL_01,
          SIZE_111L,
          Metadata(List(periodStartYear, periodStartMonth, periodStartDay, periodEndYear, periodEndMonth, periodEndDay,
            fileTypePdf, fileRole, eoriNumber, fileSize, checksum, issueDate))
        )
      )

    val securityStatementFilesWithUnkownFileTypesSdesResponse: List[FileInformation] =
      List(
        FileInformation(
          STAT_FILE_NAME_01,
          DOWNLOAD_URL_01,
          SIZE_111L,
          Metadata(List(periodStartYear, periodStartMonth, periodStartDay, periodEndYear, periodEndMonth, periodEndDay,
            fileTypeFoo, fileRole, eoriNumber, fileSize, checksum, issueDate))
        )
      ) ++ securityStatementFilesSdesResponse ++
        List(
          FileInformation(
            STAT_FILE_NAME_01,
            DOWNLOAD_URL_01,
            SIZE_111L,
            Metadata(List(periodStartYear, periodStartMonth, periodStartDay, periodEndYear, periodEndMonth, periodEndDay,
              fileType, fileRole, eoriNumber, fileSize, checksum, issueDate))
          )
        )

    val sdesGatekeeperServiceSpy: SdesGatekeeperService = spy(new SdesGatekeeperService())
    val mockHttp: HttpClientV2 = mock[HttpClientV2]
    val mockAppConfig: AppConfig = mock[AppConfig]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
    val mockMetricsReporterService: MetricsReporterService = mock[MetricsReporterService]
    val mockAuditingService: AuditingService = mock[AuditingService]

    val app: Application = application().overrides(inject.bind[HttpClientV2].toInstance(mockHttp)).build()

    private def getVatCertificateFileMetadata(periodStartYear: Int = YEAR_2018,
                                              periodStartMonth: Int = MONTH_3,
                                              fileFormat: FileFormat = Pdf,
                                              fileRole: FileRole = C79Certificate,
                                              statementRequestId: Option[String] = None) =
      VatCertificateFileMetadata(periodStartYear, periodStartMonth, fileFormat, fileRole, statementRequestId)
  }
}
