/*
 * Copyright 2022 HM Revenue & Customs
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

import actions.{EmailAction, PvatIdentifierAction, SessionIdAction}
import config.{AppConfig, ErrorHandler}
import connectors.{FinancialsApiConnector, SdesConnector}
import models.DutyPaymentMethod.CHIEF
import models.FileRole.PostponedVATStatement
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.PostponedVatViewModel
import views.html.postponed_import_vat

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class PostponedVatController @Inject()
(val authenticate: PvatIdentifierAction,
 val resolveSessionId: SessionIdAction,
 implicit val dateTimeService: DateTimeService,
 postponedImportVatView: postponed_import_vat,
 financialsApiConnector: FinancialsApiConnector,
 checkEmailIsVerified: EmailAction,
 sdesConnector: SdesConnector,
 implicit val mcc: MessagesControllerComponents)(implicit val appConfig: AppConfig, val errorHandler: ErrorHandler, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(location: Option[String]): Action[AnyContent] = (authenticate andThen checkEmailIsVerified andThen resolveSessionId) async { implicit req =>
    financialsApiConnector.deleteNotification(req.eori, PostponedVATStatement)
    for {
      postponedVatStatements <- sdesConnector.getPostponedVatStatements(req.eori)
      filteredHistoricEoris = req.allEoriHistory.filterNot(_.eori == req.eori)
      historicPostponedVatStatements <- Future.sequence(
        filteredHistoricEoris.map { eoriHistory =>
          sdesConnector.getPostponedVatStatements(eoriHistory.eori)
        }
      ).map(_.flatten)
    } yield {
      val allPostponedVatStatements = postponedVatStatements ++ historicPostponedVatStatements
      Ok(postponedImportVatView(
        req.eori,
        PostponedVatViewModel(allPostponedVatStatements),
        allPostponedVatStatements.exists(statement => statement.metadata.statementRequestId.nonEmpty),
        allPostponedVatStatements.count(_.metadata.source != CHIEF) == allPostponedVatStatements.size,
        location)
      )
    }
  }
}


