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

package views.helpers

import play.api.i18n.Messages

import java.time.LocalDate

trait DateFormatters {
  def dateAsMonth(date: LocalDate)(implicit messages: Messages): String = messages(s"month.${date.getMonthValue}")

  def dateAsDayMonthAndYear(date: LocalDate)(implicit messages: Messages): String =
    s"${date.getDayOfMonth} ${dateAsMonth(date)} ${date.getYear}"

  def dateAsMonthAndYear(date: LocalDate)(implicit messages: Messages): String = s"${dateAsMonth(date)} ${date.getYear}"
}

trait FileFormatters {

  private val kbMin = 1000
  private val mbMin = 1000000

  def fileSize(size: Long): String = size match {
    case kb: Long if kbMin until mbMin contains kb => s"${kb / kbMin}KB"
    case mb if mb >= mbMin => f"${mb / 1000000.0}%.1fMB"
    case _ => "1KB"
  }
}

object Formatters extends DateFormatters with FileFormatters
