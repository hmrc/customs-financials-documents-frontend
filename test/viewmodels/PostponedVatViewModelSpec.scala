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
import models.FileFormat.{Csv, Pdf}
import models.FileRole.PostponedVATStatement
import models.metadata.PostponedVatStatementFileMetadata
import models.{PostponedVatStatementFile, PostponedVatStatementGroup}
import org.scalatest.matchers.must.Matchers.mustBe
import org.mockito.Mockito.when
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import services.DateTimeService
import utils.CommonTestData.*
import utils.SpecBase
import utils.Utils.{emptyString, h1Component, h2Component, linkComponent, pComponent}
import views.helpers.Formatters
import views.html.components.*
import views.html.postponed_vat.{collapsible_statement_group, current_statement_row, download_link_pvat_statement}
import common.GuidanceRow
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application

import java.time.{LocalDate, LocalDateTime}

class PostponedVatViewModelSpec extends SpecBase with GuiceOneAppPerSuite {

  "CurrentStatementRow.apply" should {

    "produce CurrentStatementRow with correct contents" when {

      "isCdsOnly is true and PostponedVatStatementGroup has statements" in new Setup {

        val isCdsOnly = true

        val pvatStatementGroup: PostponedVatStatementGroup =
          PostponedVatStatementGroup(dateOfPreviousMonthAndAfter19th, certificateFiles)

        val collapStatGroupRowForSourceCDS: CollapsibleStatementGroupRow =
          CollapsibleStatementGroupRow(
            collapsiblePVATAmendedStatement = None,
            collapsiblePVATStatement = Some(
              new collapsible_statement_group(downloadLinkPvatStatement).apply(
                pvatStatementGroup.collectFiles(amended = false, CDS),
                "cf.account.pvat.download-link",
                "cf.account.pvat.aria.download-link",
                Some("cf.common.not-available"),
                CDS,
                Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate),
                isCdsOnly
              )
            )
          )

        val collapStatGroupRowForSourceCHIEF: CollapsibleStatementGroupRow =
          CollapsibleStatementGroupRow(
            collapsiblePVATAmendedStatement = None,
            collapsiblePVATStatement = None
          )

        val expectedResult: CurrentStatementRow = CurrentStatementRow(
          pvatStatementGroup.periodId,
          messages(Formatters.dateAsMonthAndYear(dateOfPreviousMonthAndAfter19th)),
          cdsDDRow = None,
          chiefDDRow = None,
          collapsibleStatementGroupRows = Seq(collapStatGroupRowForSourceCDS, collapStatGroupRowForSourceCHIEF)
        )

        CurrentStatementRow(pvatStatementGroup, dutyPaymentMethodSource, isCdsOnly) mustBe expectedResult
      }

      "isCdsOnly is false and PostponedVatStatementGroup has statements" in new Setup {
        val isCdsOnly = false

        val pvatStatementGroup: PostponedVatStatementGroup =
          PostponedVatStatementGroup(dateOfPreviousMonthAndAfter19th, certificateFiles)

        val collapStatGroupRowForSourceCDS: CollapsibleStatementGroupRow =
          CollapsibleStatementGroupRow(
            collapsiblePVATAmendedStatement = None,
            collapsiblePVATStatement = Some(
              new collapsible_statement_group(downloadLinkPvatStatement).apply(
                pvatStatementGroup.collectFiles(amended = false, CDS),
                "cf.account.pvat.download-link",
                "cf.account.pvat.aria.download-link",
                Some("cf.common.not-available"),
                CDS,
                Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate),
                isCdsOnly
              )
            )
          )

        val collapStatGroupRowForSourceCHIEF: CollapsibleStatementGroupRow =
          CollapsibleStatementGroupRow(
            collapsiblePVATAmendedStatement = None,
            collapsiblePVATStatement = None
          )

        val expectedResult: CurrentStatementRow = CurrentStatementRow(
          pvatStatementGroup.periodId,
          messages(Formatters.dateAsMonthAndYear(dateOfPreviousMonthAndAfter19th)),
          cdsDDRow = None,
          chiefDDRow = None,
          collapsibleStatementGroupRows = Seq(collapStatGroupRowForSourceCDS, collapStatGroupRowForSourceCHIEF)
        )

        CurrentStatementRow(pvatStatementGroup, dutyPaymentMethodSource, isCdsOnly) mustBe expectedResult
      }

      "PostponedVatStatementGroup has no statements, startDate is of the previous month (after 19th) " +
        "and isCdsOnly is true" in new Setup {

          val isCdsOnly = true

          when(mockDateTimeService.systemDateTime()).thenReturn(date)

          val pvatStatementGroup: PostponedVatStatementGroup =
            PostponedVatStatementGroup(dateOfPreviousMonthAndAfter19th, Seq())

          val cdsDDRow: DDRow = DDRow(
            messages("cf.common.not-available"),
            messages(
              "cf.common.not-available-screen-reader-cds",
              Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate)
            )
          )

          val expectedResult: CurrentStatementRow = CurrentStatementRow(
            pvatStatementGroup.periodId,
            messages(Formatters.dateAsMonthAndYear(dateOfPreviousMonthAndAfter19th)),
            cdsDDRow = Some(cdsDDRow),
            chiefDDRow = None,
            collapsibleStatementGroupRows = Seq()
          )

          CurrentStatementRow(pvatStatementGroup, dutyPaymentMethodSource, isCdsOnly) mustBe expectedResult
        }

      "PostponedVatStatementGroup has no statements, startDate is of the previous month (after 19th) " +
        "and isCdsOnly is false" in new Setup {

          val isCdsOnly = false

          when(mockDateTimeService.systemDateTime()).thenReturn(date)

          val pvatStatementGroup: PostponedVatStatementGroup =
            PostponedVatStatementGroup(dateOfPreviousMonthAndAfter19th, Seq())

          val cdsDDRow: DDRow = DDRow(
            messages("cf.common.not-available"),
            messages(
              "cf.common.not-available-screen-reader-cds",
              Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate)
            )
          )

          val chiefDDRow: DDRow = DDRow(
            messages("cf.common.not-available"),
            messages(
              "cf.common.not-available-screen-reader-chief",
              Formatters.dateAsMonthAndYear(pvatStatementGroup.startDate)
            )
          )

          val expectedResult: CurrentStatementRow = CurrentStatementRow(
            pvatStatementGroup.periodId,
            messages(Formatters.dateAsMonthAndYear(dateOfPreviousMonthAndAfter19th)),
            cdsDDRow = Some(cdsDDRow),
            chiefDDRow = Some(chiefDDRow),
            collapsibleStatementGroupRows = Seq()
          )

          CurrentStatementRow(pvatStatementGroup, dutyPaymentMethodSource, isCdsOnly) mustBe expectedResult
        }

    }
  }

  "PostponedVatViewModel.apply" should {

    "produce PostponedVatViewModel with correct contents" when {

      "PostponedVatStatementFile records are present" in new Setup {

        when(mockDateTimeService.systemDateTime()).thenReturn(date)

        val actualPVatModel: PostponedVatViewModel = PostponedVatViewModel(
          postponedVatCertificateFiles,
          hasRequestedStatements = true,
          isCdsOnly = true,
          Some(location),
          PVATUrls(
            customsFinancialsHomePageUrl = customsFinancialsHomePageUrl,
            requestStatementsUrl = requestedStatementsUrl,
            pvEmail = PvEmail(pvEmailEmailAddress, pvEmailEmailAddressHref),
            viewVatAccountSupportLink = viewVatAccountSupportLink,
            serviceUnavailableUrl = Some(serviceUnavailableUrl)
          )
        )

        actualPVatModel.pageTitle mustBe messages("cf.account.pvat.title")
        actualPVatModel.backLink mustBe Some(customsFinancialsHomePageUrl)

        actualPVatModel.pageH1Heading mustBe expectedHeading

        actualPVatModel.statementsAvailableGuidance mustBe expectedStatementsAvailableGuidance

        actualPVatModel.statementH2Heading mustBe expectedH2Heading

        actualPVatModel.requestedStatements mustBe expectedRequestedStatements

        actualPVatModel.currentStatements.noStatementMsg mustBe None

        val expectedResult: Seq[PostponedVatStatementGroup] =
          Seq(pVatGroup1, pVatGroup2, pVatGroup3, pVatGroup6, pVatGroup5, pVatGroup4)

        val expectedCurrentRows: Seq[HtmlFormat.Appendable] = expectedCurrentRowsValue(expectedResult)

        actualPVatModel.currentStatements.currentStatementRows mustBe expectedCurrentRows

        actualPVatModel.statOlderThanSixMonthsGuidance mustBe expectedStatOlderThanSixMonthsGuidanceRow

        actualPVatModel.chiefDeclarationGuidance mustBe expectedChiefDeclarationGuidance

        actualPVatModel.helpAndSupportGuidance mustBe expectedHelpAndSupportGuidance
      }

      "there are no PostponedVatStatementFile records" in new Setup {

        when(mockDateTimeService.systemDateTime()).thenReturn(date)

        val actualPVatModel: PostponedVatViewModel = PostponedVatViewModel(
          Seq(),
          hasRequestedStatements = true,
          isCdsOnly = true,
          Some(location),
          PVATUrls(
            customsFinancialsHomePageUrl = customsFinancialsHomePageUrl,
            requestStatementsUrl = requestedStatementsUrl,
            pvEmail = PvEmail(pvEmailEmailAddress, pvEmailEmailAddressHref),
            viewVatAccountSupportLink = viewVatAccountSupportLink,
            serviceUnavailableUrl = Some(serviceUnavailableUrl)
          )
        )

        actualPVatModel.pageTitle mustBe messages("cf.account.pvat.title")
        actualPVatModel.backLink mustBe Some(customsFinancialsHomePageUrl)

        actualPVatModel.pageH1Heading mustBe expectedHeading

        actualPVatModel.statementsAvailableGuidance mustBe expectedStatementsAvailableGuidance

        actualPVatModel.statementH2Heading mustBe expectedH2Heading

        actualPVatModel.requestedStatements mustBe expectedRequestedStatements

        actualPVatModel.currentStatements.noStatementMsg mustBe None

        actualPVatModel.statOlderThanSixMonthsGuidance mustBe expectedStatOlderThanSixMonthsGuidanceRow

        actualPVatModel.chiefDeclarationGuidance mustBe expectedChiefDeclarationGuidance

        actualPVatModel.helpAndSupportGuidance mustBe expectedHelpAndSupportGuidance
      }
    }
  }

  "CurrentStatementsSection" should {

    "populate the default object correctly" in {
      val cussrentStatSectionOb = CurrentStatementsSection()

      cussrentStatSectionOb.noStatementMsg mustBe empty
      cussrentStatSectionOb.currentStatementRows mustBe empty
    }
  }

  "PVATUrls" should {
    "populate the default value of serviceUnavailableUrl correctly" in new Setup {
      PVATUrls(
        customsFinancialsHomePageUrl = customsFinancialsHomePageUrl,
        requestStatementsUrl = requestedStatementsUrl,
        pvEmail = PvEmail(pvEmailEmailAddress, pvEmailEmailAddressHref),
        viewVatAccountSupportLink = viewVatAccountSupportLink
      ).serviceUnavailableUrl mustBe empty
    }
  }

  "CollapsibleStatementGroupRow" should {
    "populate default object with empty PVAT Statement and Amended Statement" in {
      val stateGroupRowOb = CollapsibleStatementGroupRow()

      stateGroupRowOb.collapsiblePVATStatement mustBe empty
      stateGroupRowOb.collapsiblePVATAmendedStatement mustBe empty
    }
  }

  private def expectedCurrentRowsValue(
    expectedResult: Seq[PostponedVatStatementGroup]
  )(implicit msgs: Messages): Seq[HtmlFormat.Appendable] = {
    val expectedCurrentRows: Seq[HtmlFormat.Appendable] = expectedResult
      .map { pvaStatGroup =>
        CurrentStatementRow(pvaStatGroup, Seq(CDS, CHIEF), isCdsOnly = true)
      }
      .map { currentRow =>
        val innerLink                 = new linkInner()
        val pVATDownloadLinkStatement = new download_link_pvat_statement(innerLink)
        val collapsibleStatementGroup = new collapsible_statement_group(pVATDownloadLinkStatement)

        new current_statement_row(collapsibleStatementGroup).apply(currentRow)
      }
    expectedCurrentRows
  }

  override def fakeApplication(): Application = applicationBuilder.build()

  trait Setup {
    val certificateFiles: Seq[PostponedVatStatementFile] = Seq(
      PostponedVatStatementFile(
        STAT_FILE_NAME_04,
        DOWNLOAD_URL_06,
        SIZE_111L,
        PostponedVatStatementFileMetadata(YEAR_2018, MONTH_3, Pdf, PostponedVATStatement, CDS, None),
        emptyString
      )
    )

    val periodId                        = "test_id"
    val location                        = "test_location"
    val serviceUnavailableUrl           = "unavailable_url"
    val pvEmailEmailAddress: String     = "pvaenquiries@hmrc.gov.uk"
    val pvEmailEmailAddressHref: String = "mailto:pvaenquiries@hmrc.gov.uk"
    val viewVatAccountSupportLink       = "https://accountsupport.test.com"
    val requestedStatementsUrl          = "http://localhost:9396/customs/historic-statement/requested/postponed-vat"
    val customsFinancialsHomePageUrl    = "http://localhost:9876/customs/payment-records"

    val date: LocalDateTime                        = LocalDateTime.of(YEAR_2023, MONTH_10, DAY_20, HOUR_12, MINUTES_30, SECONDS_50)
    val dateOfPreviousMonthAndAfter19th: LocalDate = date.toLocalDate.minusMonths(ONE_MONTH).withDayOfMonth(DAY_20)
    val currentDate: LocalDate                     = LocalDate.of(YEAR_2023, MONTH_10, DAY_20)

    val dutyPaymentMethodSource: Seq[String] = Seq(CDS, CHIEF)
    val linkInner                            = new linkInner()
    val downloadLinkPvatStatement            = new download_link_pvat_statement(linkInner)

    implicit val mockDateTimeService: DateTimeService = mock[DateTimeService]

    val pVatStatCsvFileForMonth6: PostponedVatStatementFile = PostponedVatStatementFile(
      STAT_FILE_NAME_02,
      DOWNLOAD_URL_02,
      SIZE_111L,
      PostponedVatStatementFileMetadata(YEAR_2018, MONTH_6, Csv, PostponedVATStatement, CDS, None),
      EORI_NUMBER
    )

    val pVatStatPdfFileForMonth6: PostponedVatStatementFile = PostponedVatStatementFile(
      STAT_FILE_NAME_01,
      DOWNLOAD_URL_01,
      SIZE_1300000L,
      PostponedVatStatementFileMetadata(YEAR_2018, MONTH_6, Pdf, PostponedVATStatement, CDS, None),
      EORI_NUMBER
    )

    val pVatStatPdfFileForMonth5: PostponedVatStatementFile = PostponedVatStatementFile(
      STAT_FILE_NAME_03,
      DOWNLOAD_URL_03,
      SIZE_111L,
      PostponedVatStatementFileMetadata(YEAR_2018, MONTH_5, Pdf, PostponedVATStatement, CDS, None),
      EORI_NUMBER
    )

    val pVatStatCsvFileForMonth4: PostponedVatStatementFile = PostponedVatStatementFile(
      STAT_FILE_NAME_04,
      DOWNLOAD_URL_05,
      SIZE_111L,
      PostponedVatStatementFileMetadata(YEAR_2018, MONTH_4, Csv, PostponedVATStatement, CDS, None),
      EORI_NUMBER
    )

    val pVatStatPdfFileForMonth4: PostponedVatStatementFile = PostponedVatStatementFile(
      STAT_FILE_NAME_04,
      DOWNLOAD_URL_04,
      SIZE_111L,
      PostponedVatStatementFileMetadata(YEAR_2018, MONTH_4, Pdf, PostponedVATStatement, CDS, None),
      EORI_NUMBER
    )

    val pVatStatPdfFileForMonth3: PostponedVatStatementFile = PostponedVatStatementFile(
      STAT_FILE_NAME_04,
      DOWNLOAD_URL_06,
      SIZE_111L,
      PostponedVatStatementFileMetadata(YEAR_2018, MONTH_3, Pdf, PostponedVATStatement, CDS, None),
      EORI_NUMBER
    )

    val postponedVatCertificateFiles: List[PostponedVatStatementFile] = List(
      pVatStatPdfFileForMonth3,
      pVatStatCsvFileForMonth4,
      pVatStatPdfFileForMonth4,
      pVatStatPdfFileForMonth5,
      pVatStatCsvFileForMonth6,
      pVatStatPdfFileForMonth6
    )

    val pVatGroup1: PostponedVatStatementGroup =
      PostponedVatStatementGroup(LocalDate.of(YEAR_2023, MONTH_9, DAY_20), Seq())

    val pVatGroup2: PostponedVatStatementGroup =
      PostponedVatStatementGroup(LocalDate.of(YEAR_2023, MONTH_8, DAY_20), Seq())

    val pVatGroup3: PostponedVatStatementGroup =
      PostponedVatStatementGroup(LocalDate.of(YEAR_2023, MONTH_7, DAY_20), Seq())

    val pVatGroup6: PostponedVatStatementGroup =
      PostponedVatStatementGroup(
        LocalDate.of(YEAR_2018, MONTH_6, DAY_1),
        Seq(pVatStatCsvFileForMonth6, pVatStatPdfFileForMonth6)
      )

    val pVatGroup5: PostponedVatStatementGroup =
      PostponedVatStatementGroup(LocalDate.of(YEAR_2018, MONTH_5, DAY_1), Seq(pVatStatPdfFileForMonth5))

    val pVatGroup4: PostponedVatStatementGroup =
      PostponedVatStatementGroup(
        LocalDate.of(YEAR_2018, MONTH_4, DAY_1),
        Seq(pVatStatCsvFileForMonth4, pVatStatPdfFileForMonth4)
      )

    val expectedHeading: HtmlFormat.Appendable =
      h1Component.apply(msg = "cf.account.pvat.title", classes = "govuk-heading-xl  govuk-!-margin-bottom-6")

    val expectedStatementsAvailableGuidance: HtmlFormat.Appendable =
      pComponent.apply(message = "cf.account.vat.available.statement-text", id = Some("vat-available-statement-text"))

    val expectedH2Heading: HtmlFormat.Appendable = h2Component.apply("cf.account.pvat.your-statements.heading")

    val expectedRequestedStatements: Option[HtmlFormat.Appendable] = Some(
      requestedStatementSection(
        requestedStatementsUrl,
        "cf.postponed-vat.requested-statements-available-link-text",
        "cf.account.detail.requested-certificates-available-text.pre",
        "cf.account.detail.requested-certificates-available-text.post"
      )
    )

    val expectedStatOlderThanSixMonthsGuidanceRow: GuidanceRow = GuidanceRow(
      h2Heading = h2Component.apply(
        "cf.account.pvat.older-statements.heading",
        id = Some("missing-documents-guidance-heading"),
        classes = "govuk-heading-m govuk-!-margin-top-6"
      ),
      link = Some(
        linkComponent.apply(
          "cf.account.pvat.older-statements.description.link",
          location = serviceUnavailableUrl,
          preLinkMessage = Some("cf.account.pvat.older-statements.description.2"),
          linkSentence = true
        )
      )
    )

    val expectedChiefDeclarationGuidance: GuidanceRow = GuidanceRow(
      h2Heading = h2Component.apply(
        id = Some("chief-guidance-heading"),
        msg = "cf.account.vat.chief.heading",
        classes = "govuk-heading-m govuk-!-margin-top-6"
      ),
      link = Some(
        linkComponent.apply(
          pvEmailEmailAddress,
          location = pvEmailEmailAddressHref,
          preLinkMessage = Some("cf.account.pvat.older-statements.description.3"),
          linkSentence = true
        )
      )
    )

    val expectedHelpAndSupportGuidance: GuidanceRow = GuidanceRow(
      h2Heading = h2Component.apply(
        id = Some("pvat.support.message.heading"),
        msg = "cf.account.pvat.support.heading",
        classes = "govuk-heading-m govuk-!-margin-top-2"
      ),
      link = Some(
        linkComponent.apply(
          messages("cf.account.pvat.support.link"),
          location = viewVatAccountSupportLink,
          preLinkMessage = Some("cf.account.pvat.support.message"),
          pId = Some("pvat.support.message"),
          pClass = "govuk-body govuk-!-margin-bottom-9",
          linkSentence = true,
          openInNewTab = true
        )
      )
    )

    protected def requestedStatementSection(
      url: String,
      linkMessageKey: String,
      preLinkMessageKey: String,
      postLinkMessageKey: String
    )(implicit msgs: Messages): HtmlFormat.Appendable =
      instanceOf[requestedStatements](app)
        .apply(url, linkMessageKey, preLinkMessageKey, postLinkMessageKey)
  }
}
