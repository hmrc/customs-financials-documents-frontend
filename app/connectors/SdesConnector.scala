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
import models.FileFormat.{SdesFileFormats, filterFileFormats}
import models.FileRole.{C79Certificate, PostponedVATStatement, SecurityStatement}
import models._
import play.api.i18n.Messages
import play.api.libs.json.Json
import services.{AuditingService, MetricsReporterService, SdesGatekeeperService}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SdesConnector @Inject()(httpClient: HttpClient,
                              appConfig: AppConfig,
                              metricsReporterService: MetricsReporterService,
                              sdesGatekeeperService: SdesGatekeeperService,
                              auditingService: AuditingService
                             )(implicit executionContext: ExecutionContext) {

  val AUDIT_VAT_CERTIFICATES_TRANSACTION = "Display VAT certificates"
  val AUDIT_POSTPONED_VAT_STATEMENTS_TRANSACTION = "Display postponed VAT statements"
  val AUDIT_SECURITY_STATEMENTS_TRANSACTION = "Display security statements"
  val AUDIT_TYPE = "SDESCALL"
  val AUDIT_VAT_CERTIFICATES = "DisplayVATCertificates"
  val AUDIT_SECURITY_STATEMENTS = "DisplaySecurityStatements"
  val AUDIT_POSTPONED_VAT_STATEMENTS = "DisplayPostponedVATStatements"

  import sdesGatekeeperService._

  def getVatCertificates(eori: String)(implicit hc: HeaderCarrier, messages: Messages): Future[Seq[VatCertificateFile]] = {
    val transform = convertTo[VatCertificateFile] andThen filterFileFormats(SdesFileFormats)
    auditingService.audit(AuditModel(AUDIT_VAT_CERTIFICATES, AUDIT_VAT_CERTIFICATES_TRANSACTION, Json.toJson(AuditEori(eori, isHistoric = false))))
    getSdesFiles[FileInformation, VatCertificateFile](appConfig.filesUrl(C79Certificate), eori, "sdes.get.import-vat-certificates", transform)
  }

  def getPostponedVatStatements(eori: String)(implicit hc: HeaderCarrier): Future[Seq[PostponedVatStatementFile]] = {
    val transform = convertTo[PostponedVatStatementFile] andThen filterFileFormats(SdesFileFormats)
    auditingService.audit(AuditModel(AUDIT_POSTPONED_VAT_STATEMENTS, AUDIT_POSTPONED_VAT_STATEMENTS_TRANSACTION, Json.toJson(AuditEori(eori, isHistoric = false))))
    getSdesFiles[FileInformation, PostponedVatStatementFile](appConfig.filesUrl(PostponedVATStatement), eori, "sdes.get.postponed-vat-statements", transform)
  }

  def getSecurityStatements(eori: String)(implicit hc: HeaderCarrier): Future[Seq[SecurityStatementFile]] = {
    val transform = convertTo[SecurityStatementFile] andThen filterFileFormats(SdesFileFormats)
    auditingService.audit(AuditModel(AUDIT_SECURITY_STATEMENTS, AUDIT_SECURITY_STATEMENTS_TRANSACTION, Json.toJson(AuditEori(eori, isHistoric = false))))
    getSdesFiles[FileInformation, SecurityStatementFile](appConfig.filesUrl(SecurityStatement), eori, "sdes.get.security-statements", transform)
  }

  def getSdesFiles[A, B <: SdesFile](url: String, key: String, metricsName: String, transform: Seq[A] => Seq[B])
                                    (implicit reads: HttpReads[HttpResponse], readSeq: HttpReads[Seq[A]]): Future[Seq[B]] = {
    metricsReporterService.withResponseTimeLogging(metricsName) {
      httpClient.GET[HttpResponse](url, headers = Seq("x-client-id" -> appConfig.xClientIdHeader, "X-SDES-Key" -> key))(reads, HeaderCarrier(), implicitly)
        .map(readSeq.read("GET", url, _))
        .map(transform)
    }
  }
}
