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

package viewmodels

import models.DutyPaymentMethod.{CDS, CHIEF}
import models.{PostponedVatStatementFile, PostponedVatStatementGroup}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import services.DateTimeService
import utils.Constants.MONTHS_RANGE_ONE_TO_SIX_INCLUSIVE
import utils.Utils.emptyString
import views.helpers.Formatters
import views.html.components.linkInner
import views.html.postponed_vat.{collapsible_statement_group, download_link_pvat_statement}

case class DDRow(notAvailableMsg: String,
                 visuallyHiddenMsg: String)

case class CollapsibleStatementGroupRow(collapsiblePVATAmendedStatement: Option[HtmlFormat.Appendable] = None,
                                        collapsiblePVATStatement: Option[HtmlFormat.Appendable] = None)

case class GuidanceRow(h2Heading: HtmlFormat.Appendable,
                       link: Option[HtmlFormat.Appendable] = None,
                       paragraph: Option[HtmlFormat.Appendable] = None)

case class CurrentStatementRow(periodId: String,
                               startDateMsgKey: String,
                               cdsDDRow: Option[DDRow] = None,
                               chiefDDRow: Option[DDRow] = None,
                               collapsibleStatementGroupRows: Seq[CollapsibleStatementGroupRow] = Seq())

object CurrentStatementRow {

  def apply(statementGroup: PostponedVatStatementGroup,
            dutyPaymentMethodSource: Seq[String],
            isCdsOnly: Boolean)(implicit messages: Messages): CurrentStatementRow = {

    val startDateMsgKey = messages(Formatters.dateAsMonthAndYear(statementGroup.startDate))

    CurrentStatementRow(
      periodId = statementGroup.periodId,
      startDateMsgKey = startDateMsgKey,
      cdsDDRow = populateCdsDDRow(statementGroup),
      chiefDDRow = populateChiefDDRow(statementGroup, isCdsOnly),
      collapsibleStatementGroupRows =
        populateCollapsibleStatementGroupRows(statementGroup, dutyPaymentMethodSource, isCdsOnly)
    )
  }

  private def populateCdsDDRow(statementGroup: PostponedVatStatementGroup)
                              (implicit messages: Messages): Option[DDRow] = {
    if (statementGroup.noStatements && statementGroup.isPreviousMonthAndAfter19Th) {
      Some(
        DDRow(
          notAvailableMsg = messages("cf.common.not-available"),
          visuallyHiddenMsg = messages(
            "cf.common.not-available-screen-reader-cds",
            Formatters.dateAsMonthAndYear(statementGroup.startDate)
          ))
      )
    } else {
      None
    }
  }

  private def populateChiefDDRow(statementGroup: PostponedVatStatementGroup,
                                 isCdsOnly: Boolean)
                                (implicit messages: Messages): Option[DDRow] = {
    if (statementGroup.noStatements && statementGroup.isPreviousMonthAndAfter19Th && !isCdsOnly) {
      Some(
        DDRow(
          notAvailableMsg = messages("cf.common.not-available"),
          visuallyHiddenMsg = messages(
            "cf.common.not-available-screen-reader-chief",
            Formatters.dateAsMonthAndYear(statementGroup.startDate)
          )
        )
      )
    } else {
      None
    }
  }

  private def populateCollapsibleStatementGroupRows(statementGroup: PostponedVatStatementGroup,
                                                    dutyPaymentMethodSource: Seq[String],
                                                    isCdsOnly: Boolean)
                                                   (implicit messages: Messages): Seq[CollapsibleStatementGroupRow] = {

    if (statementGroup.noStatements) {
      Seq()
    } else {
      dutyPaymentMethodSource.map {
        paymentSource =>
          val linkInner = new linkInner()
          val downloadLinkPvatStatement = new download_link_pvat_statement(linkInner)

          CollapsibleStatementGroupRow(
            collapsibleStatementGroupForAmendedPVAT(statementGroup, isCdsOnly, paymentSource, downloadLinkPvatStatement),
            collapsibleStatementGroupForPVAT(statementGroup, isCdsOnly, paymentSource, downloadLinkPvatStatement)
          )
      }
    }
  }

  private def collapsibleStatementGroupForPVAT(statementGroup: PostponedVatStatementGroup,
                                               isCdsOnly: Boolean,
                                               paymentSource: String,
                                               downloadLinkPvatStatement: download_link_pvat_statement)
                                              (implicit messages: Messages): Option[HtmlFormat.Appendable] = {

    val originalSubStringForMsgKey = if (statementGroup.collectFiles(amended = true, paymentSource).isEmpty) {
      emptyString
    } else {
      "original."
    }

    if (statementGroup.collectFiles(amended = false, paymentSource).nonEmpty) {
      Some(new collapsible_statement_group(downloadLinkPvatStatement).apply(
        statementGroup.collectFiles(amended = false, paymentSource),
        s"cf.account.pvat.${originalSubStringForMsgKey}download-link",
        s"cf.account.pvat.aria.${originalSubStringForMsgKey}download-link",
        Some("cf.common.not-available"),
        paymentSource,
        Formatters.dateAsMonthAndYear(statementGroup.startDate),
        isCdsOnly
      ))
    } else {
      None
    }
  }

  private def collapsibleStatementGroupForAmendedPVAT(statementGroup: PostponedVatStatementGroup,
                                                      isCdsOnly: Boolean,
                                                      paymentSource: String,
                                                      downloadLinkPvatStatement: download_link_pvat_statement)
                                                     (implicit messages: Messages): Option[HtmlFormat.Appendable] = {
    if (statementGroup.collectFiles(amended = true, paymentSource).nonEmpty) {
      Some(new collapsible_statement_group(downloadLinkPvatStatement)
        .apply(
          statementGroup.collectFiles(amended = true, paymentSource),
          "cf.account.pvat.amended-download-link",
          "cf.account.pvat.aria.amended-download-link",
          None,
          paymentSource,
          Formatters.dateAsMonthAndYear(statementGroup.startDate),
          isCdsOnly
        ))
    } else {
      None
    }
  }
}

case class CurrentStatementsSection(currentStatementRows: Seq[HtmlFormat.Appendable] = Seq.empty[HtmlFormat.Appendable],
                                    noStatementMsg: Option[HtmlFormat.Appendable] = None) {

  val source: Seq[String] = Seq(CDS, CHIEF)
}

case class PostponedVatViewModel(pageTitle: String,
                                 backLink: Option[String] = None,
                                 pageH1Heading: HtmlFormat.Appendable,
                                 statementsAvailableGuidance: HtmlFormat.Appendable,
                                 statementH2Heading: HtmlFormat.Appendable,
                                 requestedStatements: Option[HtmlFormat.Appendable] = None,
                                 currentStatements: CurrentStatementsSection,
                                 cdsOnly: Boolean,
                                 statOlderThanSixMonths: GuidanceRow,
                                 chiefDeclaration: GuidanceRow,
                                 helpAndSupport: GuidanceRow)

object PostponedVatViewModel {
  def apply(files: Seq[PostponedVatStatementFile])(implicit messages: Messages,
                                                   dateTimeService: DateTimeService): Seq[PostponedVatStatementGroup] = {
    val response: Seq[PostponedVatStatementGroup] =
      files.groupBy(_.monthAndYear).map {
        case (month, filesForMonth) => PostponedVatStatementGroup(month, filesForMonth)
      }.toList

    val monthList =
      MONTHS_RANGE_ONE_TO_SIX_INCLUSIVE.map(n => dateTimeService.systemDateTime().toLocalDate.minusMonths(n))

    monthList.map {
      date => response.find(_.startDate.getMonth == date.getMonth).getOrElse(PostponedVatStatementGroup(date, Seq.empty))
    }.toList.sorted.reverse
  }
}
