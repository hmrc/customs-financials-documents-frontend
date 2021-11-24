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

package actions

import connectors.DataStoreConnector
import models.{AuditEori, AuditModel, AuthenticatedRequest, SignedInUser}
import play.api.libs.json.Json
import play.api.mvc.Request
import services.AuditingService
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthActionHelper @Inject()(dataStoreConnector: DataStoreConnector,
                                 auditingService: AuditingService)(implicit executionContext: ExecutionContext) {

  val AUDIT_AUTHORISED_TRANSACTION = "View account"
  val AUDIT_EORI = "EORI"
  val AUDIT_HISTORIC_EORIS = "HISTORIC_EORI"
  val AUDIT_TYPE = "ViewAccount"

  def authenticatedRequest[A](credentials: Option[Credentials],
                              name: Option[Name],
                              email: Option[String],
                              eori: String,
                              affinityGroup: Option[AffinityGroup],
                              internalId: Option[String],
                              allEnrolments: Enrolments)(request: Request[A])(implicit hc: HeaderCarrier): Future[AuthenticatedRequest[A]] =
    for {
      allEoriHistory <- dataStoreConnector.getAllEoriHistory(eori)
      eoriHistory = allEoriHistory.filterNot(_.eori == eori)
      _ = {
        val historicEoriAuditDetails: Seq[AuditEori] = eoriHistory.map(eoriHistory => AuditEori(eoriHistory.eori, isHistoric = true))
        val eoriAuditDetails: AuditEori = AuditEori(eori, isHistoric = false)
        val eoriList = eoriAuditDetails +: historicEoriAuditDetails
        val auditEvent = AuditModel(AUDIT_TYPE, AUDIT_AUTHORISED_TRANSACTION, Json.toJson(eoriList))
        auditingService.audit(auditEvent)
      }
      cdsLoggedInUser = SignedInUser(credentials, name, email, eori, affinityGroup, internalId, allEnrolments, allEoriHistory)
    } yield AuthenticatedRequest(request, cdsLoggedInUser)

}
