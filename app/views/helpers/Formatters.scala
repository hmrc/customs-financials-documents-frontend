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

import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}
import java.util.Locale

import play.api.i18n.Messages


trait DateFormatters {
  def dateAsMonth(date: LocalDate)(implicit messages: Messages): String = messages(s"month.${date.getMonthValue}")
  def dateAsDayMonthAndYear(date: LocalDate)(implicit messages: Messages): String = s"${date.getDayOfMonth} ${dateAsMonth(date)} ${date.getYear}"
  def dateAsMonthAndYear(date: LocalDate)(implicit messages: Messages): String = s"${dateAsMonth(date)} ${date.getYear}"
  def dateAsDay(date: LocalDate): String = DateTimeFormatter.ofPattern("d").format(date)

  def dateAsAbbrMonth(date: LocalDate)(implicit messages: Messages): String = messages(s"month.abbr.${date.getMonthValue}")
  def dateAsdMMMyyyy(date: LocalDate)(implicit messages: Messages): String = s"${date.getDayOfMonth} ${dateAsAbbrMonth(date)} ${date.getYear}"


  def timeAsHourMinutesWithAmPm(dateTime: LocalDateTime): String = DateTimeFormatter.ofPattern("hh:mm a").format(dateTime)

  def updatedDateTime(dateTime: LocalDateTime)(implicit messages: Messages): String = {
    Formatters.timeAsHourMinutesWithAmPm(dateTime).toLowerCase + " on " + Formatters.dateAsDayMonthAndYear(dateTime.toLocalDate)
  }

  def dateTimeAsIso8601(dateTime: LocalDateTime): String = {
    s"${DateTimeFormatter.ISO_DATE_TIME.format(dateTime.truncatedTo(ChronoUnit.SECONDS))}Z"
  }
}

trait CurrencyFormatters {
  def formatCurrencyAmount(amount: BigDecimal): String = {
    val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.UK)
    val outputDecimals = if (amount.isWhole()) 0 else 2
    numberFormat.setMaximumFractionDigits(outputDecimals)
    numberFormat.setMinimumFractionDigits(outputDecimals)
    numberFormat.format(amount)
  }

  def formatCurrencyAmountWithLeadingPlus(amount: BigDecimal): String ={
    val formattedAmount = formatCurrencyAmount(amount)
    if (amount > 0) {
      "+" + formattedAmount
    } else {
      formattedAmount
    }
  }
}


trait FileFormatters {
  def fileSize(size: Long): String = size match {
    case kb if 1000 until 1000000 contains kb => s"${kb / 1000}KB"
    case mb if mb >= 1000000 => f"${mb / 1000000.0}%.1fMB"
    case _ => "1KB"
  }
}


object Formatters extends DateFormatters with CurrencyFormatters with FileFormatters

