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
import config.AppConfig
import models.AuthenticatedRequest
import play.api.mvc._
import uk.gov.hmrc.auth.core._

import scala.concurrent.ExecutionContext

trait PvatIdentifierAction extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionRefiner[Request, AuthenticatedRequest]

class PvatAuthAction @Inject()(override val authConnector: AuthConnector,
                               override val appConfig: AppConfig,
                               override val parser: BodyParsers.Default,
                               override val authActionHelper: AuthActionHelper)
                              (override implicit val executionContext: ExecutionContext) extends PvatIdentifierAction with AuthActionBuilder {
  override lazy val continueUrl: String = appConfig.pvatLoginContinueUrl
}
