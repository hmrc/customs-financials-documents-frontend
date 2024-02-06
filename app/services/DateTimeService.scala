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

package services

import config.AppConfig
import utils.Constants._

import java.time._
import javax.inject.{Inject, Singleton}

@Singleton
class DateTimeService @Inject()(appConfig: AppConfig) {

  def getTimeStamp: OffsetDateTime = OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)

  def systemDateTime(): LocalDateTime = {
    if (appConfig.fixedDateTime) {
      LocalDateTime.of(
        LocalDate.of(FIXED_DATE_TIME_YEAR, FIXED_DATE_TIME_MONTH_OF_YEAR, FIXED_DATE_TIME_DAY_OF_MONTH),
        LocalTime.of(FIXED_DATE_TIME_HOUR_OF_DAY, FIXED_DATE_TIME_MINUTES_OF_HOUR)
      )
    } else {
      LocalDateTime.now()
    }
  }
}
