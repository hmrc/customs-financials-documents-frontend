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
import models._
import play.api.Logging
import play.api.http.Status.NOT_FOUND
import services.MetricsReporterService
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataStoreConnector @Inject()(http: HttpClient,
                                   metricsReporter: MetricsReporterService,
                                   appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  def getAllEoriHistory(eori: String)(implicit hc: HeaderCarrier): Future[Seq[EoriHistory]] = {
    val dataStoreEndpoint = appConfig.customsDataStore + s"/eori/$eori/eori-history"
    val emptyEoriHistory = Seq(EoriHistory(eori, None, None))
    metricsReporter.withResponseTimeLogging("customs-data-store.get.eori-history") {
      http.GET[EoriHistoryResponse](dataStoreEndpoint).map(response => response.eoriHistory)
        .recover { case e =>
          logger.error(s"DATASTORE-E-EORI-HISTORY-ERROR: ${e.getClass.getName}")
          emptyEoriHistory
        }
    }
  }

  def getEmail(eori: String)(implicit hc: HeaderCarrier): Future[Either[EmailResponses, Email]] = {
    val dataStoreEndpoint = appConfig.customsDataStore + s"/eori/$eori/verified-email"
    metricsReporter.withResponseTimeLogging("customs-data-store.get.email") {
      http.GET[EmailResponse](dataStoreEndpoint).map {
        case EmailResponse(Some(address), _, None) => Right(Email(address))
        case EmailResponse(Some(email), _, Some(_)) => Left(UndeliverableEmail(email))
        case _ => Left(UnverifiedEmail)
      }.recover {
        case UpstreamErrorResponse(_, NOT_FOUND, _, _) => Left(UnverifiedEmail)
      }
    }
  }
}