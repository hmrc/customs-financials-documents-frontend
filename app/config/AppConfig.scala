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

package config

import models.FileRole

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {

  val appName: String = config.get[String]("appName")

  val timeout: Int = config.get[Int]("timeout.timeout")
  val countdown: Int = config.get[Int]("timeout.countdown")
  val helpMakeGovUkBetterUrl: String = config.get[String]("external-urls.helpMakeGovUkBetterUrl")
  val customsFinancialsFrontendHomepage: String = config.get[String]("external-urls.customsFinancialsHomepage")
  val customsDataStore: String = servicesConfig.baseUrl("customs-data-store") +
    config.get[String]("microservice.services.customs-data-store.context")

  val loginUrl: String = config.get[String]("external-urls.login")
  val loginContinueUrl: String = config.get[String]("external-urls.loginContinue")
  val registerCdsUrl: String = config.get[String]("external-urls.cdsRegisterUrl")
  val subscribeCdsUrl: String = config.get[String]("external-urls.cdsSubscribeUrl")
  val pvatLoginContinueUrl: String = config.get[String]("external-urls.pvatLoginContinue")
  val feedbackService: String = config.get[String]("microservice.services.feedback.url") + config.get[String]("microservice.services.feedback.source")
  val signOutUrl: String = config.get[String]("external-urls.signOut")
  val requestedStatements: String = config.get[String]("external-urls.requestedStatements")
  val historicRequest: String = config.get[String]("external-urls.historicRequest")

  def historicRequestUrl(fileRole: FileRole): String = {
    fileRole match {
      case FileRole.SecurityStatement => historicRequest + "adjustments"
      case FileRole.C79Certificate | FileRole.PostponedVATStatement => historicRequest + fileRole.featureName
      case _ => ""
    }
  }

  def requestedStatements(fileRole: FileRole): String = {
    fileRole match {
      case FileRole.SecurityStatement => requestedStatements + "adjustments"
      case FileRole.C79Certificate | FileRole.PostponedVATStatement => requestedStatements + fileRole.featureName
      case _ => ""
    }
  }

  val sdesApi: String = servicesConfig.baseUrl("sdes") +
    config.get[String]("microservice.services.sdes.context")

  val xClientIdHeader: String = config.get[String]("microservice.services.sdes.x-client-id")
  val fixedDateTime: Boolean = config.get[Boolean]("features.fixed-system-time")

  val customsFinancialsApi: String = servicesConfig.baseUrl("customs-financials-api") +
    config.get[String]("microservice.services.customs-financials-api.context")

  def filesUrl(fileRole: FileRole): String = s"$sdesApi/files-available/list/${fileRole.name}"
}
