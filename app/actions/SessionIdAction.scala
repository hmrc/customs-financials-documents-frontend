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

import com.google.inject.Inject
import config.ErrorHandler
import models.{AuthenticatedRequest, EoriHistory}
import play.api.mvc.Results.Unauthorized
import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

case class AuthenticatedRequestWithSessionId[A](request: Request[A], eori: String, allEoriHistory: Seq[EoriHistory], sessionId: SessionId)
  extends WrappedRequest[A](request)


class SessionIdAction @Inject()()(implicit val executionContext: ExecutionContext, errorHandler: ErrorHandler)
  extends ActionRefiner[AuthenticatedRequest, AuthenticatedRequestWithSessionId] {

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, AuthenticatedRequestWithSessionId[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    hc.sessionId match {
      case None => Future.successful(Left(Unauthorized(errorHandler.unauthorized()(request))))
      case Some(sessionId) => Future.successful(
        Right(
          AuthenticatedRequestWithSessionId(request, request.eori, request.allEoriHistory, sessionId)
        )
      )
    }
  }

}
