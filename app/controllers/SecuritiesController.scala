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

import actions.{AuthenticatedRequestWithSessionId, EmailAction, IdentifierAction, SessionIdAction}
import config.{AppConfig, ErrorHandler}
import connectors.{FinancialsApiConnector, SdesConnector}
import models.FileRole.SecurityStatement
import models.{EoriHistory, SecurityStatementFile, SecurityStatementsByPeriod, SecurityStatementsForEori}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.DateUtils.isDateInLastSixMonths
import viewmodels.SecurityStatementsViewModel
import views.html.securities.{security_statements, security_statements_not_available}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SecuritiesController @Inject()(authenticate: IdentifierAction,
                                     resolveSessionId: SessionIdAction,
                                     sdesConnector: SdesConnector,
                                     dateTimeService: DateTimeService,
                                     checkEmailIsVerified: EmailAction,
                                     financialsApiConnector: FinancialsApiConnector,
                                     securityStatementsView: security_statements,
                                     securityStatementsNotAvailableView: security_statements_not_available,
                                     mcc: MessagesControllerComponents
                                    )(implicit val appConfig: AppConfig, val errorHandler: ErrorHandler, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def showSecurityStatements(): Action[AnyContent] = (authenticate andThen checkEmailIsVerified andThen resolveSessionId) async { implicit req =>
    financialsApiConnector.deleteNotification(req.eori, SecurityStatement)
    (for {
      allStatements <- Future.sequence(req.allEoriHistory.map(getStatements))
      securityStatementsModel = SecurityStatementsViewModel(allStatements.sorted)
    } yield Ok(securityStatementsView(securityStatementsModel))
      ).recover { case _ => Redirect(routes.SecuritiesController.statementsUnavailablePage()) }
  }

  def statementsUnavailablePage(): Action[AnyContent] = authenticate andThen checkEmailIsVerified async { implicit req =>
    Future.successful(Ok(securityStatementsNotAvailableView()))
  }

  private def getStatements(historicEori: EoriHistory)(implicit req: AuthenticatedRequestWithSessionId[_]): Future[SecurityStatementsForEori] = {
    sdesConnector.getSecurityStatements(historicEori.eori)
      .map(ssf => groupByMonthDescending(securityStatFilesInLastSixMonths(ssf)))
      .map(_.partition(_.files.exists(_.metadata.statementRequestId.isEmpty)))
      .map {
        case (current, requested) => SecurityStatementsForEori(historicEori, current, requested)
      }
  }

  private def securityStatFilesInLastSixMonths(securityStatementFiles: Seq[SecurityStatementFile]): Seq[SecurityStatementFile] =
    securityStatementFiles.filter(
      stf => isDateInLastSixMonths(stf.startDate, dateTimeService.systemDateTime().toLocalDate)
    )

  private def groupByMonthDescending(securityStatementFiles: Seq[SecurityStatementFile]): Seq[SecurityStatementsByPeriod] = {
    securityStatementFiles.groupBy(file => (file.startDate, file.endDate)).map {
      case ((startDate, endDate), filesForMonth) => SecurityStatementsByPeriod(startDate, endDate, filesForMonth)
    }.toList.sorted.reverse
  }

}
