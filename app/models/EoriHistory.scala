/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, _}

import scala.util.{Failure, Success, Try}


case class EoriHistory(eori: String,
                       validFrom: Option[LocalDate],
                       validUntil: Option[LocalDate]) {

  val isHistoricEori: Boolean = validUntil.isDefined

  private def obfuscateEori(eori: String): String = {
    val lengthToReveal = 4
    List.fill(eori.length - lengthToReveal)("*").mkString("") + eori.takeRight(lengthToReveal)
  }

  def toLoggable():String = {
    s"EoriHistory(Eori: ${obfuscateEori(eori)}, validFrom: $validFrom, validUntil: $validUntil)"
  }

}

//TODO cleanup
object EoriHistory {
  val logger = Logger(this.getClass)

  implicit val eoriHistoryFormat:Reads[EoriHistory] = (
    (JsPath \ "eori").read[String] and
      (JsPath \ "validFrom").readNullable[String].map(asDate) and
      (JsPath \ "validUntil").readNullable[String].map(asDate)
    )(EoriHistory.apply _ )

  implicit val eoriHistoryWrites: Writes[EoriHistory] = (o: EoriHistory) => {
    Json.obj(
      "eori" -> o.eori,
      "validFrom" -> o.validFrom.map(_.toString),
      "validUntil" -> o.validUntil.map(_.toString)
    )
  }

  private def asDate(maybeDate: Option[String]): Option[LocalDate] = {
    maybeDate.flatMap(dateString => Try(LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)) match {
      case Success(date) => Some(date)
      case Failure(_) =>
        Try(LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)) match {
          case Success(dateTime) => Some(dateTime.toLocalDate)
          case Failure(ex) =>
            logger.error(ex.getMessage, ex)
            None
        }
    })
  }

}