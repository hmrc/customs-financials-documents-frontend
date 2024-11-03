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

import config.AppConfig
import models.FileRole.SecurityStatement
import models.SecurityStatementsForEori
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import views.html.components.{link, requestedStatements}

case class SecurityStatementsViewModel(statementsForAllEoris: Seq[SecurityStatementsForEori],
                                       hasRequestedStatements: Boolean,
                                       hasCurrentStatements: Boolean,
                                       requestedStatementNotification: HtmlFormat.Appendable)

object SecurityStatementsViewModel {
  def apply(statementsForAllEoris: Seq[SecurityStatementsForEori])(implicit appConfig: AppConfig,
                                                                   messages: Messages): SecurityStatementsViewModel = {

    SecurityStatementsViewModel(
      statementsForAllEoris = statementsForAllEoris,
      hasRequestedStatements = hasRequestedStatements(statementsForAllEoris),
      hasCurrentStatements = hasCurrentStatements(statementsForAllEoris),
      requestedStatementNotification = requestedStatementNotification(statementsForAllEoris))
  }

  private def requestedStatementNotification(statementsForAllEoris: Seq[SecurityStatementsForEori])
                                            (implicit appConfig: AppConfig, messages: Messages
                                            ): HtmlFormat.Appendable = {
    if (hasRequestedStatements(statementsForAllEoris)) {
      new requestedStatements(new link).apply(
        url = appConfig.requestedStatements(SecurityStatement))
    } else {
      HtmlFormat.empty
    }
  }

  private def hasRequestedStatements(statementsForAllEoris: Seq[SecurityStatementsForEori]): Boolean =
    statementsForAllEoris.exists(_.requestedStatements.nonEmpty)

  private def hasCurrentStatements(statementsForAllEoris: Seq[SecurityStatementsForEori]): Boolean =
    statementsForAllEoris.exists(_.currentStatements.nonEmpty)
}
