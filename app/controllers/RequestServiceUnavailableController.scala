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

package controllers

import actions.{EmailAction, IdentifierAction}
import config.AppConfig
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.request_service_unavailable

import javax.inject.Inject
import scala.concurrent.Future

class RequestServiceUnavailableController @Inject()(authenticate: IdentifierAction,
                                                    checkEmailIsVerified: EmailAction,
                                                    requestServiceUnavailableView: request_service_unavailable,
                                                    implicit val mcc: MessagesControllerComponents)
                                                   (implicit val appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport{

  def requestServiceUnavailablePage(statementType: String): Action[AnyContent]= authenticate andThen checkEmailIsVerified async { implicit req =>
    val backlink = statementType match {
      case "pvat" => Some(routes.PostponedVatController.show(Some("CDS")).url)
      case "c79" => Some(routes.VatController.showVatAccount.url)
    }
    Future.successful(Ok(requestServiceUnavailableView(backlink)))
  }
}
