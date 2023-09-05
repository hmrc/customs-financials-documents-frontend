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
import models.FileRole.C79Certificate
import models.{EoriHistory, VatCertificatesByMonth, VatCertificatesForEori}
import navigation.Navigator
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Logger, LoggerLike}
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.DateUtils._
import viewmodels.VatViewModel
import views.html.import_vat.{import_vat, import_vat_not_available}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VatController @Inject()(val authenticate: IdentifierAction,
                              val resolveSessionId: SessionIdAction,
                              val sdesConnector: SdesConnector,
                              dateTimeService: DateTimeService,
                              financialsApiConnector: FinancialsApiConnector,
                              checkEmailIsVerified: EmailAction,
                              importVatView: import_vat,
                              importVatNotAvailableView: import_vat_not_available,
                              navigator: Navigator,
                              implicit val mcc: MessagesControllerComponents)
                             (implicit val appConfig: AppConfig, val errorHandler: ErrorHandler, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  val log: LoggerLike = Logger(this.getClass)

  def showVatAccount(): Action[AnyContent] = (authenticate andThen checkEmailIsVerified andThen resolveSessionId) async { implicit req =>
    financialsApiConnector.deleteNotification(req.eori, C79Certificate)

    (for {
      allCertificates <- Future.sequence(req.allEoriHistory.map(getCertificates(_)))
      viewModel = VatViewModel(allCertificates.sorted)

      historicUrl = if(appConfig.historicStatementsEnabled) {
      appConfig.historicRequestUrl(C79Certificate)
    } else {
        routes.ServiceUnavailableController.onPageLoad(navigator.importVatPageId).url
      }
    } yield Ok(importVatView(
      viewModel,
      Some(historicUrl)))
      ).recover {
      case e =>
        log.error(s"Unable to retrieve VAT certificates :${e.getMessage}")
        Redirect(routes.VatController.certificatesUnavailablePage())
    }
  }

  def certificatesUnavailablePage(): Action[AnyContent] = authenticate andThen checkEmailIsVerified async { implicit req =>

    val historicUrl = if(appConfig.historicStatementsEnabled) {
      appConfig.historicRequestUrl(C79Certificate)
    } else {
      routes.ServiceUnavailableController.onPageLoad(navigator.importVatNotAvailablePageId).url
    }

    Future.successful(Ok(importVatNotAvailableView(
      Some(historicUrl))))
  }

  private def getCertificates(historicEori: EoriHistory)(implicit req: AuthenticatedRequestWithSessionId[_]): Future[VatCertificatesForEori] = {
    val certificates = sdesConnector.getVatCertificates(historicEori.eori)
      .map(_.groupBy(_.monthAndYear).map { case (month, filesForMonth) => VatCertificatesByMonth(month, filesForMonth) }.toList)
      .map(_.partition(_.files.exists(_.metadata.statementRequestId.isEmpty)))
      .map { case (current, requested) => VatCertificatesForEori(historicEori, current, requested) }
    populateEmptyMonths(certificates)
  }

  private def populateEmptyMonths(certificates: Future[VatCertificatesForEori])(implicit messages: Messages) = {
    for {
      certs <- certificates
      monthList = (1 to 6).map(n => dateTimeService.systemDateTime().toLocalDate.minusMonths(n))
      populatedEmptyMonth: Seq[VatCertificatesByMonth] = monthList.map {
        date => certs.currentCertificates.find(_.date.getMonth == date.getMonth).getOrElse(VatCertificatesByMonth(date, Seq.empty))
      }
      response = certs.copy(currentCertificates = dropImmediatePreviousMonthCertIfUnavailable(populatedEmptyMonth))
    } yield response
  }

  /**
   * Drops the immediate previous month's cert if the day for the current date is before 15th day
   * of the month otherwise unchanged certs are returned
   *
   * @param currentCerts Seq[VatCertificatesByMonth] Certs populated for last 6 months
   * @return Seq[VatCertificatesByMonth] Updated Certs as per the date check
   */
  private def dropImmediatePreviousMonthCertIfUnavailable(currentCerts: Seq[VatCertificatesByMonth]): Seq[VatCertificatesByMonth] =
    if (isDayBefore15ThDayOfTheMonth(dateTimeService.systemDateTime().toLocalDate) && currentCerts.head.files.isEmpty) {
      currentCerts.drop(1)
    } else
      currentCerts
}
