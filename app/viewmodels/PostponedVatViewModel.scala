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

import models.{PostponedVatStatementFile, PostponedVatStatementGroup}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import services.DateTimeService
import utils.Constants.MONTHS_RANGE_ONE_TO_SIX_INCLUSIVE


case class ComponentAttributesRow(message: String,
                                  classes: String = "",
                                  id: Option[String] = None)

case class PostponedVatViewModel(pageTitle: String,
                                 backLink: Option[String] = None,
                                 pageHeading: HtmlFormat.Appendable,
                                 statementsAvailableGuidance: HtmlFormat.Appendable,
                                 requestedStatements: Option[HtmlFormat.Appendable] = None,
                                 cdsOnly: Boolean)

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
