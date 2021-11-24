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

package controllers

import config.AppConfig
import play.api.mvc._
import play.api.{Logger, LoggerLike}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LogoutController @Inject()(override val authConnector: AuthConnector, mcc: MessagesControllerComponents)
                                (implicit val appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with AuthorisedFunctions {
  val feedbackLink: String = appConfig.feedbackService
  val log: LoggerLike = Logger(this.getClass)

  def logout: Action[AnyContent] = Action { implicit request =>
    Redirect(appConfig.signOutUrl, Map("continue" -> Seq(feedbackLink)))
  }

  def logoutNoSurvey: Action[AnyContent] = Action { implicit request =>
    Redirect(appConfig.signOutUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))
  }
}
