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
import config.Headers.{X_CLIENT_ID, X_SDES_KEY}
import models.FileFormat.{SdesFileFormats, filterFileFormats}
import models.FileRole.{C79Certificate, PostponedVATStatement, SecurityStatement}
import models.*
import play.api.i18n.Messages
import services.{AuditingService, MetricsReporterService, SdesGatekeeperService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2

class SdesConnector @Inject() (
  httpClient: HttpClientV2,
  appConfig: AppConfig,
  metricsReporterService: MetricsReporterService,
  sdesGatekeeperService: SdesGatekeeperService,
  auditingService: AuditingService
)(implicit executionContext: ExecutionContext) {

  import sdesGatekeeperService._

  def getVatCertificates(
    eori: String
  )(implicit hc: HeaderCarrier, messages: Messages): Future[Seq[VatCertificateFile]] = {

    val transform = convertTo[VatCertificateFile] andThen filterFileFormats(SdesFileFormats)
    auditingService.auditVatCertificates(eori)

    getSdesFiles[FileInformation, VatCertificateFile](
      appConfig.filesUrl(C79Certificate),
      eori,
      "sdes.get.import-vat-certificates",
      transform
    )
  }

  def getPostponedVatStatements(eori: String)(implicit hc: HeaderCarrier): Future[Seq[PostponedVatStatementFile]] = {

    val transform = convertTo[PostponedVatStatementFile] andThen filterFileFormats(SdesFileFormats)
    auditingService.auditPostponedVatStatements(eori)

    getSdesFiles[FileInformation, PostponedVatStatementFile](
      appConfig.filesUrl(PostponedVATStatement),
      eori,
      "sdes.get.postponed-vat-statements",
      transform
    ).map(_.filter(p => FileFormat.PvatFileFormats.contains(p.metadata.fileFormat)))
  }

  def getSecurityStatements(eori: String)(implicit hc: HeaderCarrier): Future[Seq[SecurityStatementFile]] = {

    val transform = convertTo[SecurityStatementFile] andThen filterFileFormats(SdesFileFormats)
    auditingService.auditSecurityStatements(eori)

    getSdesFiles[FileInformation, SecurityStatementFile](
      appConfig.filesUrl(SecurityStatement),
      eori,
      "sdes.get.security-statements",
      transform
    )
  }

  private def addXHeaders(hc: HeaderCarrier, key: String): HeaderCarrier =
    hc.copy(extraHeaders = hc.extraHeaders ++ Seq(X_CLIENT_ID -> appConfig.xClientIdHeader, X_SDES_KEY -> key))

  private def getSdesFiles[A, B <: SdesFile](
    urlLink: String,
    key: String,
    metricsName: String,
    transform: Seq[A] => Seq[B]
  )(implicit readSeq: HttpReads[Seq[A]], hc: HeaderCarrier): Future[Seq[B]] =
    metricsReporterService.withResponseTimeLogging(metricsName) {
      httpClient
        .get(url"$urlLink")
        .setHeader(addXHeaders(hc, key).extraHeaders: _*)
        .execute[HttpResponse]
        .map(readSeq.read("GET", urlLink, _))
        .map(transform)
    }
}
