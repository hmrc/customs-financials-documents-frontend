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

import actions.{PvatIdentifierAction, SessionIdAction}
import config.{AppConfig, ErrorHandler}
import connectors.{FinancialsApiConnector, SdesConnector}
import models.DutyPaymentMethod.CHIEF
import models.FileRole.PostponedVATStatement
import models.{FileFormat, PostponedVatStatementFile}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Logger, LoggerLike}
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
 sdesConnector: SdesConnector,
 implicit val mcc: MessagesControllerComponents)(implicit val appConfig: AppConfig, val errorHandler: ErrorHandler, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  val log: LoggerLike = Logger(this.getClass)

  def show(location: Option[String]): Action[AnyContent] = (authenticate andThen resolveSessionId) async { implicit req =>

  //TODO cleanup
  val currentEori = req.user.eori
    val filteredHistoricEoris = req.user.allEoriHistory.filterNot(_.eori == currentEori)
    financialsApiConnector.deleteNotification(currentEori, PostponedVATStatement)

    def filterOnlyPvatFiles(files: Seq[PostponedVatStatementFile]): Seq[PostponedVatStatementFile] = {
      files.filter { p => FileFormat.PvatFileFormats.contains(p.metadata.fileFormat) }
    }

    def eventualPvatStatements: Future[Seq[PostponedVatStatementFile]] = {
      sdesConnector.getPostponedVatStatements(currentEori).map { a =>
        log.info("POSTPONEDVATSTATEMENTS" + a.toString())
        filterOnlyPvatFiles(a)
      }
    }

    def eventualHistoricPvatStatements: Future[Seq[PostponedVatStatementFile]] = {
      Future.sequence(filteredHistoricEoris.map { eoriHistory =>
        sdesConnector.getPostponedVatStatements(eoriHistory.eori).map { response =>
          filterOnlyPvatFiles(response)
        }
      }).map(_.flatten)
    }

    for {
      pVatStatements <- eventualPvatStatements
      historicPVatStatements <- eventualHistoricPvatStatements
    } yield {
      val allPVatStatements = pVatStatements ++ historicPVatStatements
      val allPvatStatementsCount = allPVatStatements.size
      val cdsCount= allPVatStatements.count(_.metadata.source != CHIEF)
      val cdsOnly = cdsCount == allPvatStatementsCount
      val hasRequestedStatements = !(allPVatStatements.filter(statement => statement.metadata.statementRequestId != None)).isEmpty
      log.info(s"postponed vat statements displayed TOTAL: ${allPvatStatementsCount}, hasRequestedStatements: $hasRequestedStatements, CDS: $cdsCount CHIEF: ${allPvatStatementsCount - cdsCount}")
      Ok(postponedImportVatView(currentEori, PostponedVatViewModel(allPVatStatements), hasRequestedStatements, cdsOnly, location))
    }
  }

}


