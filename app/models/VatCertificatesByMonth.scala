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

import models.FileFormat.{Csv, Pdf}
import play.api.i18n.Messages
import views.helpers.Formatters

import java.time.LocalDate

case class VatCertificatesByMonth(date: LocalDate,
                                  files: Seq[VatCertificateFile])(implicit messages: Messages)
  extends Ordered[VatCertificatesByMonth] {

  val formattedMonth: String = Formatters.dateAsMonth(date)
  val formattedMonthYear: String = Formatters.dateAsMonthAndYear(date)

  val pdf: Option[VatCertificateFile] = files.find(_.fileFormat == Pdf)
  val csv: Option[VatCertificateFile] = files.find(_.fileFormat == Csv)

  override def compare(that: VatCertificatesByMonth): Int = date.compareTo(that.date)
}
