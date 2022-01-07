/*
 * Copyright 2022 HM Revenue & Customs
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
import services.DateTimeService

object PostponedVatViewModel {
  def apply(files: Seq[PostponedVatStatementFile])(implicit messages: Messages, dateTimeService: DateTimeService): Seq[PostponedVatStatementGroup] = {
    val response = files.groupBy(_.monthAndYear).map { case (month, filesForMonth) => PostponedVatStatementGroup(month, filesForMonth) }.toList
    val monthList = (1 to 6).map(n => dateTimeService.systemDateTime().toLocalDate.minusMonths(n))
    monthList.map{
      date => response.find(_.startDate.getMonth == date.getMonth).getOrElse(PostponedVatStatementGroup(date, Seq.empty))
    }.toList.sorted.reverse
  }
}