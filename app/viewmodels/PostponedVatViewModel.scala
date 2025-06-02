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
import utils.Utils._
import views.helpers.Formatters
import views.html.components._
import views.html.postponed_vat.{collapsible_statement_group, current_statement_row, download_link_pvat_statement}
import common.GuidanceRow

case class PvEmail(emailAddress: String, emailAddressHref: String)

case class PVATUrls(
  customsFinancialsHomePageUrl: String,
  requestStatementsUrl: String,
  pvEmail: PvEmail,
  viewVatAccountSupportLink: String,
  serviceUnavailableUrl: Option[String] = None
)

case class DDRow(notAvailableMsg: String, visuallyHiddenMsg: String)

case class CollapsibleStatementGroupRow(
  collapsiblePVATAmendedStatement: Option[HtmlFormat.Appendable] = None,
  collapsiblePVATStatement: Option[HtmlFormat.Appendable] = None
)

case class CurrentStatementRow(
  periodId: String,
  startDateMsgKey: String,
  cdsDDRow: Option[DDRow] = None,
  chiefDDRow: Option[DDRow] = None,
  collapsibleStatementGroupRows: Seq[CollapsibleStatementGroupRow] = Seq()
)

case class CurrentStatementsSection(
  currentStatementRows: Seq[HtmlFormat.Appendable] = Seq(),
  noStatementMsg: Option[HtmlFormat.Appendable] = None
)

case class PostponedVatViewModel(
  pageTitle: String,
  backLink: Option[String] = None,
  pageH1Heading: HtmlFormat.Appendable,
  statementsAvailableGuidance: HtmlFormat.Appendable,
  statementH2Heading: HtmlFormat.Appendable,
  requestedStatements: Option[HtmlFormat.Appendable] = None,
  currentStatements: CurrentStatementsSection,
  statOlderThanSixMonthsGuidance: GuidanceRow,
  chiefDeclarationGuidance: GuidanceRow,
  helpAndSupportGuidance: GuidanceRow
)

object PostponedVatViewModel {
  def apply(
    files: Seq[PostponedVatStatementFile],
    hasRequestedStatements: Boolean,
    isCdsOnly: Boolean,
    location: Option[String],
    urls: PVATUrls
  )(implicit messages: Messages, dateTimeService: DateTimeService): PostponedVatViewModel = {

    val statementGroupList: Seq[PostponedVatStatementGroup] = statementGroups(files)
    val bckLinkUrl                                          = location.map(_ => urls.customsFinancialsHomePageUrl)

    PostponedVatViewModel(
      pageTitle = messages("cf.account.pvat.title"),
      backLink = bckLinkUrl,
      pageH1Heading = populatePageHeading,
      statementsAvailableGuidance = populateStatementsAvailableGuidance,
      statementH2Heading = populateStatementH2Heading,
      requestedStatements = populateRequestedStatements(hasRequestedStatements, urls.requestStatementsUrl),
      currentStatements = populateCurrentStatements(statementGroupList, isCdsOnly),
      statOlderThanSixMonthsGuidance = populateOlderThanSixMonthsGuidance(urls.serviceUnavailableUrl),
      chiefDeclarationGuidance = populateChiefDeclarationGuidance(urls.pvEmail),
      helpAndSupportGuidance = populateHelpAndSupportGuidance(urls.viewVatAccountSupportLink)
    )
  }

  private def statementGroups(
    files: Seq[PostponedVatStatementFile]
  )(implicit messages: Messages, dateTimeService: DateTimeService): Seq[PostponedVatStatementGroup] = {

    val response: Seq[PostponedVatStatementGroup] =
      files
        .groupBy(_.monthAndYear)
        .map { case (month, filesForMonth) =>
          PostponedVatStatementGroup(month, filesForMonth)
        }
        .toList

    val monthList =
      MONTHS_RANGE_ONE_TO_SIX_INCLUSIVE.map(n => dateTimeService.systemDateTime().toLocalDate.minusMonths(n))

    monthList
      .map { date =>
        response.find(_.startDate.getMonth == date.getMonth).getOrElse(PostponedVatStatementGroup(date, Seq.empty))
      }
      .toList
      .sorted
      .reverse
  }

  private def populatePageHeading(implicit msgs: Messages): HtmlFormat.Appendable =
    h1Component.apply(msg = "cf.account.pvat.title", classes = "govuk-heading-xl  govuk-!-margin-bottom-6")

  private def populateStatementsAvailableGuidance(implicit msgs: Messages): HtmlFormat.Appendable =
    pComponent.apply(message = "cf.account.vat.available.statement-text", id = Some("vat-available-statement-text"))

  private def populateStatementH2Heading(implicit msgs: Messages): HtmlFormat.Appendable =
    h2Component.apply("cf.account.pvat.your-statements.heading")

  private def populateRequestedStatements(hasRequestedStatements: Boolean, requestStatementsUrl: String)(implicit
    msgs: Messages
  ): Option[HtmlFormat.Appendable] =
    if (hasRequestedStatements) {
      val requestedStatementSection = new requestedStatements(linkComponent).apply(
        url = requestStatementsUrl,
        linkMessageKey = "cf.postponed-vat.requested-statements-available-link-text"
      )

      Some(requestedStatementSection)
    } else {
      None
    }

  private def populateCurrentStatements(statementGroupList: Seq[PostponedVatStatementGroup], isCdsOnly: Boolean)(
    implicit msgs: Messages
  ): CurrentStatementsSection = {
    val noStatementMsg =
      if (statementGroupList.isEmpty) {
        Some(insetComponent.apply("cf.account.pvat.no-statements-yet"))
      } else {
        None
      }

    CurrentStatementsSection(
      currentStatementRows = populateCurrentStatementRows(statementGroupList, isCdsOnly),
      noStatementMsg = noStatementMsg
    )
  }

  private def populateOlderThanSixMonthsGuidance(
    serviceUnavailableUrl: Option[String]
  )(implicit msgs: Messages): GuidanceRow = {
    val h2Heading = h2Component.apply(
      "cf.account.pvat.older-statements.heading",
      id = Some("missing-documents-guidance-heading"),
      classes = "govuk-heading-m govuk-!-margin-top-6"
    )

    val link = linkComponent.apply(
      "cf.account.pvat.older-statements.description.link",
      location = serviceUnavailableUrl.getOrElse(emptyString),
      preLinkMessage = Some("cf.account.pvat.older-statements.description.2"),
      linkSentence = true
    )

    GuidanceRow(
      h2Heading,
      Some(link)
    )
  }

  private def populateChiefDeclarationGuidance(pvEmail: PvEmail)(implicit msgs: Messages): GuidanceRow = {
    val h2Heading = h2Component.apply(
      id = Some("chief-guidance-heading"),
      msg = "cf.account.vat.chief.heading",
      classes = "govuk-heading-m govuk-!-margin-top-6"
    )

    val link = linkComponent.apply(
      pvEmail.emailAddress,
      location = pvEmail.emailAddressHref,
      preLinkMessage = Some("cf.account.pvat.older-statements.description.3"),
      linkSentence = true
    )

    GuidanceRow(
      h2Heading,
      Some(link)
    )
  }

  private def populateHelpAndSupportGuidance(
    viewVatAccountSupportLink: String
  )(implicit msgs: Messages): GuidanceRow = {

    val h2Heading = h2Component.apply(
      id = Some("pvat.support.message.heading"),
      msg = "cf.account.pvat.support.heading",
      classes = "govuk-heading-m govuk-!-margin-top-2"
    )

    val link = linkComponent.apply(
      msgs("cf.account.pvat.support.link"),
      location = viewVatAccountSupportLink,
      preLinkMessage = Some("cf.account.pvat.support.message"),
      pId = Some("pvat.support.message"),
      pClass = "govuk-body govuk-!-margin-bottom-9",
      linkSentence = true,
      openInNewTab = true
    )

    GuidanceRow(
      h2Heading,
      Some(link)
    )
  }

  private def populateCurrentStatementRows(statementGroupList: Seq[PostponedVatStatementGroup], isCdsOnly: Boolean)(
    implicit msgs: Messages
  ): Seq[HtmlFormat.Appendable] = {

    val filteredPVATStatGroups = statementGroupList.filter(statementGroup =>
      !statementGroup.noStatements || statementGroup.isPreviousMonthAndAfter19Th
    )

    filteredPVATStatGroups.map { statementGroup =>

      val currentStatementRow =
        CurrentStatementRow(statementGroup, dutyPaymentMethodSource = Seq(CDS, CHIEF), isCdsOnly)

      val innerLink                 = new linkInner()
      val pVATDownloadLinkStatement = new download_link_pvat_statement(innerLink)
      val collapsibleStatementGroup = new collapsible_statement_group(pVATDownloadLinkStatement)

      new current_statement_row(collapsibleStatementGroup).apply(currentStatementRow)
    }
  }

}

object CurrentStatementRow {

  def apply(statementGroup: PostponedVatStatementGroup, dutyPaymentMethodSource: Seq[String], isCdsOnly: Boolean)(
    implicit messages: Messages
  ): CurrentStatementRow = {

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

  private def populateCdsDDRow(statementGroup: PostponedVatStatementGroup)(implicit messages: Messages): Option[DDRow] =
    if (statementGroup.noStatements && statementGroup.isPreviousMonthAndAfter19Th) {
      Some(
        DDRow(
          notAvailableMsg = messages("cf.common.not-available"),
          visuallyHiddenMsg = messages(
            "cf.common.not-available-screen-reader-cds",
            Formatters.dateAsMonthAndYear(statementGroup.startDate)
          )
        )
      )
    } else {
      None
    }

  private def populateChiefDDRow(statementGroup: PostponedVatStatementGroup, isCdsOnly: Boolean)(implicit
    messages: Messages
  ): Option[DDRow] =
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

  private def populateCollapsibleStatementGroupRows(
    statementGroup: PostponedVatStatementGroup,
    dutyPaymentMethodSource: Seq[String],
    isCdsOnly: Boolean
  )(implicit messages: Messages): Seq[CollapsibleStatementGroupRow] =
    if (statementGroup.noStatements) {
      Seq()
    } else {
      dutyPaymentMethodSource.map { paymentSource =>
        val linkInner                 = new linkInner()
        val downloadLinkPvatStatement = new download_link_pvat_statement(linkInner)

        CollapsibleStatementGroupRow(
          collapsibleStatementGroupForAmendedPVAT(statementGroup, isCdsOnly, paymentSource, downloadLinkPvatStatement),
          collapsibleStatementGroupForPVAT(statementGroup, isCdsOnly, paymentSource, downloadLinkPvatStatement)
        )
      }
    }

  private def collapsibleStatementGroupForPVAT(
    statementGroup: PostponedVatStatementGroup,
    isCdsOnly: Boolean,
    paymentSource: String,
    downloadLinkPvatStatement: download_link_pvat_statement
  )(implicit messages: Messages): Option[HtmlFormat.Appendable] = {

    val originalSubStringForMsgKey = if (statementGroup.collectFiles(amended = true, paymentSource).isEmpty) {
      emptyString
    } else {
      "original."
    }

    if (statementGroup.collectFiles(amended = false, paymentSource).nonEmpty) {
      Some(
        new collapsible_statement_group(downloadLinkPvatStatement).apply(
          statementGroup.collectFiles(amended = false, paymentSource),
          s"cf.account.pvat.${originalSubStringForMsgKey}download-link",
          s"cf.account.pvat.aria.${originalSubStringForMsgKey}download-link",
          Some("cf.common.not-available"),
          paymentSource,
          Formatters.dateAsMonthAndYear(statementGroup.startDate),
          isCdsOnly
        )
      )
    } else {
      None
    }
  }

  private def collapsibleStatementGroupForAmendedPVAT(
    statementGroup: PostponedVatStatementGroup,
    isCdsOnly: Boolean,
    paymentSource: String,
    downloadLinkPvatStatement: download_link_pvat_statement
  )(implicit messages: Messages): Option[HtmlFormat.Appendable] =
    if (statementGroup.collectFiles(amended = true, paymentSource).nonEmpty) {
      Some(
        new collapsible_statement_group(downloadLinkPvatStatement)
          .apply(
            statementGroup.collectFiles(amended = true, paymentSource),
            "cf.account.pvat.amended-download-link",
            "cf.account.pvat.aria.amended-download-link",
            None,
            paymentSource,
            Formatters.dateAsMonthAndYear(statementGroup.startDate),
            isCdsOnly
          )
      )
    } else {
      None
    }
}
