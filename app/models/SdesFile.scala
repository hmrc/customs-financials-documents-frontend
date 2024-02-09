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

import models.metadata.{PostponedVatStatementFileMetadata, SdesFileMetadata, SecurityStatementFileMetadata, VatCertificateFileMetadata}
import play.api.i18n.Messages
import views.helpers.Formatters

import java.time.LocalDate

trait SdesFile {
  def metadata: SdesFileMetadata

  def downloadURL: String

  val fileFormat: FileFormat = metadata.fileFormat
  val monthAndYear: LocalDate = LocalDate.of(metadata.periodStartYear, metadata.periodStartMonth, 1)
}

case class SecurityStatementFile(filename: String,
                                 downloadURL: String,
                                 size: Long,
                                 metadata: SecurityStatementFileMetadata
                                ) extends Ordered[SecurityStatementFile] with SdesFile {

  val startDate: LocalDate = LocalDate.of(metadata.periodStartYear, metadata.periodStartMonth, metadata.periodStartDay)
  val endDate: LocalDate = LocalDate.of(metadata.periodEndYear, metadata.periodEndMonth, metadata.periodEndDay)
  val formattedSize: String = Formatters.fileSize(metadata.fileSize)

  def compare(that: SecurityStatementFile): Int = startDate.compareTo(that.startDate)
}

case class VatCertificateFile(filename: String,
                              downloadURL: String,
                              size: Long,
                              metadata: VatCertificateFileMetadata,
                              eori: String)
                             (implicit messages: Messages) extends Ordered[VatCertificateFile] with SdesFile {

  val formattedSize: String = Formatters.fileSize(size)
  val formattedMonth: String = Formatters.dateAsMonth(monthAndYear)

  def compare(that: VatCertificateFile): Int = that.metadata.fileFormat.compare(metadata.fileFormat)
}

case class PostponedVatStatementFile(filename: String,
                                     downloadURL: String,
                                     size: Long,
                                     metadata: PostponedVatStatementFileMetadata,
                                     eori: String) extends Ordered[PostponedVatStatementFile] with SdesFile {

  def compare(that: PostponedVatStatementFile): Int = that.metadata.fileFormat.compare(metadata.fileFormat)
}
