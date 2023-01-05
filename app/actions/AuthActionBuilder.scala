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

package actions

import config.AppConfig
import controllers.routes
import models.AuthenticatedRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait AuthActionBuilder extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionRefiner[Request, AuthenticatedRequest] with AuthorisedFunctions {
  override val authConnector: AuthConnector
  val appConfig: AppConfig
  val parser: BodyParsers.Default
  val authActionHelper: AuthActionHelper
  val continueUrl: String
  override implicit val executionContext: ExecutionContext

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(Retrievals.allEnrolments) { allEnrolments =>
        allEnrolments.getEnrolment("HMRC-CUS-ORG").flatMap(_.getIdentifier("EORINumber")) match {
          case Some(eori) => authActionHelper.authenticatedRequest(eori.value)(request).map(Right(_))
          case None => Future.successful(Left(Redirect(routes.UnauthorisedController.onPageLoad)))
        }
    }
  } recover {
    case _: NoActiveSession =>
      Left(Redirect(appConfig.loginUrl, Map("continue_url" -> Seq(continueUrl))))
    case _: InsufficientEnrolments =>
      Left(Redirect(routes.UnauthorisedController.onPageLoad))
    case _ =>
      Left(Redirect(routes.UnauthorisedController.onPageLoad))
  }
}




