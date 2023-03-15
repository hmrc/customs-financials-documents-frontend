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

package services

import config.AppConfig
import models.{AuditEori, AuditModel, EoriHistory}
import play.api.http.HeaderNames
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditingService @Inject()(appConfig: AppConfig, auditConnector: AuditConnector)(implicit executionContext: ExecutionContext) {

  def auditVatCertificates(eori: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(AuditModel("DisplayVATCertificates", "Display VAT certificates", Json.toJson(AuditEori(eori, isHistoric = false))))

  def auditPostponedVatStatements(eori: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(AuditModel("DisplayPostponedVATStatements", "Display postponed VAT statements", Json.toJson(AuditEori(eori, isHistoric = false))))

  def auditSecurityStatements(eori: String)(implicit hc: HeaderCarrier): Future[AuditResult] =
    audit(AuditModel("DisplaySecurityStatements", "Display security statements", Json.toJson(AuditEori(eori, isHistoric = false))))

  def auditHistoricEoris(currentEori: String, allEoriHistory: Seq[EoriHistory])(implicit hc: HeaderCarrier): Future[AuditResult] = {
      val eoriHistory = allEoriHistory.filterNot(_.eori == currentEori)
      val historicEoriAuditDetails: Seq[AuditEori] = eoriHistory.map(eoriHistory => AuditEori(eoriHistory.eori, isHistoric = true))
      val eoriAuditDetails: AuditEori = AuditEori(currentEori, isHistoric = false)
      val eoriList = eoriAuditDetails +: historicEoriAuditDetails
      val auditEvent = AuditModel("ViewAccount", "View account", Json.toJson(eoriList))
      audit(auditEvent)
  }

  private def audit(auditModel: AuditModel)(implicit hc: HeaderCarrier): Future[AuditResult] = {
    val referrer: HeaderCarrier => String = _.headers(Seq(HeaderNames.REFERER)).headOption.fold("-")(_._2)
   
    val dataEvent = ExtendedDataEvent(
      auditSource = appConfig.appName,
      auditType = auditModel.auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(auditModel.transactionName, referrer(hc)),
      detail = auditModel.detail)

    auditConnector.sendExtendedEvent(dataEvent)
  }
}