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

import config.AppConfig
import connectors.{FinancialsApiConnector, SdesConnector}
import models.FileFormat.Pdf
import models.FileRole.SecurityStatement
import models.metadata.SecurityStatementFileMetadata
import models.{EoriHistory, SecurityStatementFile, SecurityStatementsByPeriod, SecurityStatementsForEori}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.Helpers._
import play.api.{Application, inject}
import utils.SpecBase
import viewmodels.SecurityStatementsViewModel
import views.html.securities.{security_statements, security_statements_not_available}

import java.time.LocalDate
import scala.concurrent.Future

class SecuritiesControllerSpec extends SpecBase {

  "showSecurityStatements" should {

    "render the page correctly on successful responses" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.SecuritiesController.showSecurityStatements.url)
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe
          view(SecurityStatementsViewModel(Seq(securityStatementsForEori)))(request, messages(app), appConfig).toString()
      }
    }

    "redirect to security statements unavailable if a problem occurs" in {
      val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
      val mockSdesConnector: SdesConnector = mock[SdesConnector]

      when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
        .thenReturn(Future.successful(true))
      when(mockSdesConnector.getSecurityStatements(eqTo("testEori1"))(any))
        .thenReturn(Future.failed(new RuntimeException("Something went wrong")))

      val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory("testEori1", None, None))

      val app: Application = application(eoriHistory).overrides(
        inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
        inject.bind[SdesConnector].toInstance(mockSdesConnector)
      ).build()

      running(app) {
        val request = fakeRequest(GET, routes.SecuritiesController.showSecurityStatements.url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SecuritiesController.statementsUnavailablePage.url
      }
    }
  }

  "statementsUnavailablePage" should {
    "render correctly" in {
      val app: Application = application().build()
      val appConfig = app.injector.instanceOf[AppConfig]
      val unavailableView = app.injector.instanceOf[security_statements_not_available]

      running(app) {
        val request = fakeRequest(GET, routes.SecuritiesController.statementsUnavailablePage.url)
        val result = route(app, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe unavailableView()(request, messages(app), appConfig).toString()
      }
    }
  }

  trait Setup {
    val mockFinancialsApiConnector: FinancialsApiConnector = mock[FinancialsApiConnector]
    val mockSdesConnector: SdesConnector = mock[SdesConnector]
    val date: LocalDate = LocalDate.now().withDayOfMonth(28)
    val securityStatementFile: SecurityStatementFile =
      SecurityStatementFile("statementfile_00", "download_url_00", 99L,
        SecurityStatementFileMetadata(date.minusMonths(1).getYear,
          date.minusMonths(1).getMonthValue,
          28,
          date.getYear,
          date.getMonthValue,
          28,
          Pdf,
          SecurityStatement,
          "testEori1",
          500L,
          "0000000",
          None))

    val eoriHistory: Seq[EoriHistory] = Seq(EoriHistory("testEori1", None, None))

    val statementsByPeriod: SecurityStatementsByPeriod =
      SecurityStatementsByPeriod(date.minusMonths(1), date, Seq(securityStatementFile))

    val securityStatementsForEori: SecurityStatementsForEori =
      SecurityStatementsForEori(EoriHistory("testEori1", None, None),  Seq(statementsByPeriod), Seq.empty)

    when(mockFinancialsApiConnector.deleteNotification(any, any)(any))
      .thenReturn(Future.successful(true))
    when(mockSdesConnector.getSecurityStatements(eqTo("testEori1"))(any))
      .thenReturn(Future.successful(Seq(securityStatementFile)))
    val app: Application = application(eoriHistory).overrides(
      inject.bind[FinancialsApiConnector].toInstance(mockFinancialsApiConnector),
      inject.bind[SdesConnector].toInstance(mockSdesConnector)
    ).build()

    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val view: security_statements = app.injector.instanceOf[security_statements]
  }
}
