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

package utils

import play.api.mvc.RequestHeader
import views.html.components.{h1, h2, inset, link, p, span}

object Utils {
  val emptyString = ""
  val hyphen = "-"
  val singleSpace = " "
  val period = "."

  private val questionMark = "?"

  val h1Component = new h1()
  val h2Component = new h2()
  val pComponent = new p()
  val linkComponent = new link()
  val insetComponent = new inset()
  val spanComponent = new span()

  def referrerUrl(platformHost: Option[String])(implicit request: RequestHeader): Option[String] =
    Some(s"${platformHost.getOrElse(emptyString)}${pathWithQueryString(request)}")

  def pathWithQueryString(request: RequestHeader): String = {
    import request._
    s"$path${if (rawQueryString.nonEmpty) questionMark else emptyString}$rawQueryString"
  }
}
