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

import actions.{EmailAction, PvatIdentifierAction, SessionIdAction}
import config.{AppConfig, ErrorHandler}
import connectors.{FinancialsApiConnector, SdesConnector}
import models.DutyPaymentMethod.CHIEF
import models.FileRole.PostponedVATStatement
import models.PostponedVatStatementFile
import navigation.Navigator
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Logger, LoggerLike}
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.Constants.MONTHS_RANGE_ONE_TO_SIX_INCLUSIVE
import viewmodels.{PVATUrls, PostponedVatViewModel, PvEmail}
import views.html.{postponed_import_vat, postponed_import_vat_not_available}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostponedVatController @Inject()(val authenticate: PvatIdentifierAction,
                                       val resolveSessionId: SessionIdAction,
                                       implicit val dateTimeService: DateTimeService,
                                       postponedImportVatView: postponed_import_vat,
                                       postponedImportVatNotAvailableView: postponed_import_vat_not_available,
                                       financialsApiConnector: FinancialsApiConnector,
                                       checkEmailIsVerified: EmailAction,
                                       sdesConnector: SdesConnector,
                                       navigator: Navigator,
                                       implicit val mcc: MessagesControllerComponents)
                                      (implicit val appConfig: AppConfig,
                                       val errorHandler: ErrorHandler,
                                       ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  val log: LoggerLike = Logger(this.getClass)

  def show(location: Option[String]): Action[AnyContent] =
    (authenticate andThen checkEmailIsVerified andThen resolveSessionId) async { implicit req =>

      financialsApiConnector.deleteNotification(req.eori, PostponedVATStatement)

      (
        for {
          postponedVatStatements <- sdesConnector.getPostponedVatStatements(req.eori)
          filteredHistoricEoris = req.allEoriHistory.filterNot(_.eori == req.eori)
          historicPostponedVatStatements <- Future.sequence(
            filteredHistoricEoris.map { eoriHistory =>
              sdesConnector.getPostponedVatStatements(eoriHistory.eori)
            }
          ).map(_.flatten)
        } yield {

          val allPostponedVatStatements: Seq[PostponedVatStatementFile] =
            postponedVatStatements ++ historicPostponedVatStatements

          val currentStatements: Seq[PostponedVatStatementFile] = filterLastSixMonthsStatements(postponedVatStatements)

          val historicUrl = if (appConfig.historicStatementsEnabled) {
            appConfig.historicRequestUrl(PostponedVATStatement)
          } else {
            routes.ServiceUnavailableController.onPageLoad(navigator.postponedVatPageId).url
          }

          Ok(postponedImportVatView(PostponedVatViewModel(
            currentStatements,
            allPostponedVatStatements.exists(statement => statement.metadata.statementRequestId.nonEmpty),
            currentStatements.count(_.metadata.source != CHIEF) == currentStatements.size,
            location,
            populatePVATUrls(historicUrl)))
          )
        }
        ).recover {
        case _ => Redirect(routes.PostponedVatController.statementsUnavailablePage())
      }
    }

  def statementsUnavailablePage(): Action[AnyContent] =
    authenticate andThen checkEmailIsVerified async { implicit req =>
      Future.successful(Ok(postponedImportVatNotAvailableView(
        req.eori,
        Some(routes.ServiceUnavailableController.onPageLoad(navigator.postponedVatNotAvailablePageId).url))
      ))
    }

  private def filterLastSixMonthsStatements(files: Seq[PostponedVatStatementFile]): Seq[PostponedVatStatementFile] = {
    val monthList = MONTHS_RANGE_ONE_TO_SIX_INCLUSIVE.map(n => dateTimeService.systemDateTime().toLocalDate.minusMonths(n))

    monthList.flatMap {
      date =>
        files.find(file =>
          file.monthAndYear.getYear == date.getYear && file.monthAndYear.getMonth == date.getMonth).toSeq
    }
  }

  private def populatePVATUrls(historicUrl: String): PVATUrls = {
    PVATUrls(
      customsFinancialsHomePageUrl = appConfig.customsFinancialsFrontendHomepage,
      requestStatementsUrl = appConfig.requestedStatements(PostponedVATStatement),
      pvEmail = PvEmail(appConfig.pvEmailEmailAddress, appConfig.pvEmailEmailAddressHref),
      viewVatAccountSupportLink = appConfig.viewVatAccountSupportLink,
      serviceUnavailableUrl = Some(historicUrl)
    )
  }
}
