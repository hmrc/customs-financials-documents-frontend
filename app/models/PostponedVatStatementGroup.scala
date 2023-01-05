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

package models

import models.DutyPaymentMethod.{CDS, CHIEF}
import models.FileRole.{PostponedVATAmendedStatement, PostponedVATStatement}
import models.metadata.PostponedVatStatementFileMetadata
import play.api.i18n.Messages
import views.helpers.Formatters

import java.time.LocalDate

case class PostponedVatStatementGroup(startDate: LocalDate, files: Seq[PostponedVatStatementFile])(implicit messages: Messages) extends Ordered[PostponedVatStatementGroup] {

  private val periodName = Formatters.dateAsMonthAndYear(startDate).replace(" ", "-").toLowerCase
  val periodId: String = s"""period-$periodName"""
  val noStatements: Boolean = Seq(CDS, CHIEF).flatMap(source => collectFiles(amended = false,source)).isEmpty

  def collectFiles(amended: Boolean, source: String): Seq[PostponedVatStatementFile] = {
    val amendedPred: PostponedVatStatementFileMetadata => Boolean = if (amended) {
      _.fileRole == PostponedVATAmendedStatement
    } else {
      _.fileRole == PostponedVATStatement
    }

    files
      .filter(f => amendedPred(f.metadata))
      .filter(_.metadata.source == source)
  }

  override def compare(that: PostponedVatStatementGroup): Int = startDate.compareTo(that.startDate)

}