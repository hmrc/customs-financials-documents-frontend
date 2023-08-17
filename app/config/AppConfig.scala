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

package config

import models.FileRole
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {

  lazy val appName: String = config.get[String]("appName")

  lazy val timeout: Int = config.get[Int]("timeout.timeout")
  lazy val countdown: Int = config.get[Int]("timeout.countdown")
  lazy val helpMakeGovUkBetterUrl: String = config.get[String]("external-urls.helpMakeGovUkBetterUrl")
  lazy val customsFinancialsFrontendHomepage: String = config.get[String]("external-urls.customsFinancialsHomepage")
  lazy val customsDataStore: String = servicesConfig.baseUrl("customs-data-store") +
    config.get[String]("microservice.services.customs-data-store.context")

  lazy val viewVatAccountSupportLink: String = config.get[String]("external-urls.viewVatAccountSupportLink")

  lazy val loginUrl: String = config.get[String]("external-urls.login")
  lazy val loginContinueUrl: String = config.get[String]("external-urls.loginContinue")
  lazy val registerCdsUrl: String = config.get[String]("external-urls.cdsRegisterUrl")
  lazy val subscribeCdsUrl: String = config.get[String]("external-urls.cdsSubscribeUrl")
  lazy val pvatLoginContinueUrl: String = config.get[String]("external-urls.pvatLoginContinue")
  lazy val feedbackService: String = config.get[String]("microservice.services.feedback.url") + config.get[String]("microservice.services.feedback.source")
  lazy val signOutUrl: String = config.get[String]("external-urls.signOut")
  lazy val requestedStatements: String = config.get[String]("external-urls.requestedStatements")
  lazy val historicRequest: String = config.get[String]("external-urls.historicRequest")
  lazy val emailFrontendUrl: String = config.get[String]("external-urls.verifyYourEmailUrl")
  lazy val pvEmailEmailAddress: String = config.get[String]("external-urls.pvEmailEmailAddressUrl")


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

  lazy val sdesApi: String = servicesConfig.baseUrl("sdes") +
    config.get[String]("microservice.services.sdes.context")

  lazy val xClientIdHeader: String = config.get[String]("microservice.services.sdes.x-client-id")
  lazy val fixedDateTime: Boolean = config.get[Boolean]("features.fixed-system-time")

  lazy val customsFinancialsApi: String = servicesConfig.baseUrl("customs-financials-api") +
    config.get[String]("microservice.services.customs-financials-api.context")

  def filesUrl(fileRole: FileRole): String = s"$sdesApi/files-available/list/${fileRole.name}"
}
