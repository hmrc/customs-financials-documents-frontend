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
import utils.Constants.{MONTHS_RANGE_ONE_TO_SIX_INCLUSIVE, sevenMonths}
import utils.DateUtils._
import viewmodels.ImportVatViewModel
import views.html.import_vat.{import_vat, import_vat_not_available}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VatController @Inject() (
  val authenticate: IdentifierAction,
  val resolveSessionId: SessionIdAction,
  val sdesConnector: SdesConnector,
  dateTimeService: DateTimeService,
  financialsApiConnector: FinancialsApiConnector,
  checkEmailIsVerified: EmailAction,
  importVatView: import_vat,
  importVatNotAvailableView: import_vat_not_available,
  navigator: Navigator,
  implicit val mcc: MessagesControllerComponents
)(implicit val appConfig: AppConfig, val errorHandler: ErrorHandler, ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  val log: LoggerLike = Logger(this.getClass)

  def showVatAccount(): Action[AnyContent] =
    (authenticate andThen checkEmailIsVerified andThen resolveSessionId).async { implicit req =>

      financialsApiConnector.deleteNotification(req.eori, C79Certificate)

      val historicUrl = if (appConfig.historicStatementsEnabled) {
        appConfig.historicRequestUrl(C79Certificate)
      } else {
        routes.ServiceUnavailableController.onPageLoad(navigator.importVatPageId).url
      }

      (
        for {
          allCertificates <- Future.sequence(req.allEoriHistory.map(getCertificates(_)))
          viewModel        = ImportVatViewModel(allCertificates.sorted, Some(historicUrl))
        } yield Ok(importVatView(viewModel, Some(historicUrl)))
      ).recover { case e =>
        log.error(s"Unable to retrieve VAT certificates :${e.getMessage}")
        Redirect(routes.VatController.certificatesUnavailablePage())
      }
    }

  def certificatesUnavailablePage(): Action[AnyContent] =
    authenticate andThen checkEmailIsVerified async { implicit req =>

      val historicUrl = if (appConfig.historicStatementsEnabled) {
        appConfig.historicRequestUrl(C79Certificate)
      } else {
        routes.ServiceUnavailableController.onPageLoad(navigator.importVatNotAvailablePageId).url
      }

      Future.successful(Ok(importVatNotAvailableView(Some(historicUrl))))
    }

  private def getCertificates(
    historicEori: EoriHistory
  )(implicit req: AuthenticatedRequestWithSessionId[_]): Future[VatCertificatesForEori] = {

    val certificates = sdesConnector
      .getVatCertificates(historicEori.eori)
      .map(
        _.groupBy(_.monthAndYear)
          .map { case (month, filesForMonth) =>
            VatCertificatesByMonth(month, filesForMonth)
          }
          .toList
      )
      .map(_.partition(_.files.exists(_.metadata.statementRequestId.isDefined)))
      .map { case (requested, current) =>
        val currentCertificatesLast6Months = filterLastSixMonthsStatements(current)
        VatCertificatesForEori(historicEori, currentCertificatesLast6Months, requested)
      }

    populateEmptyMonths(certificates)
  }

  private def populateEmptyMonths(
    certificates: Future[VatCertificatesForEori]
  )(implicit messages: Messages): Future[VatCertificatesForEori] =
    for {
      certs    <- certificates
      monthList =
        MONTHS_RANGE_ONE_TO_SIX_INCLUSIVE.map(n => dateTimeService.systemDateTime().toLocalDate.minusMonths(n))

      response = certs.copy(
                   currentCertificates =
                     dropImmediatePreviousMonthCertIfUnavailable(populateEmptyMonth(monthList, certs))
                 )
    } yield response

  private def populateEmptyMonth(monthList: IndexedSeq[LocalDate], certs: VatCertificatesForEori)(implicit
    messages: Messages
  ): Seq[VatCertificatesByMonth] =
    monthList.map { date =>
      certs.currentCertificates
        .find(_.date.getMonth == date.getMonth)
        .getOrElse(VatCertificatesByMonth(date, Seq.empty))
    }

  private def dropImmediatePreviousMonthCertIfUnavailable(
    currentCerts: Seq[VatCertificatesByMonth]
  ): Seq[VatCertificatesByMonth] =
    if (isDayBefore20ThDayOfTheMonth(dateTimeService.systemDateTime().toLocalDate) && currentCerts.head.files.isEmpty) {
      currentCerts.drop(1)
    } else {
      currentCerts
    }

  private def filterLastSixMonthsStatements(currentCerts: Seq[VatCertificatesByMonth]): Seq[VatCertificatesByMonth] = {

    val sevenMonthsAgo = LocalDate.now().minusMonths(sevenMonths)
    currentCerts.filter(_.date.isAfter(sevenMonthsAgo))
  }
}
