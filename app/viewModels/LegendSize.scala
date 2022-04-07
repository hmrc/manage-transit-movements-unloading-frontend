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

package viewModels

import scala.language.implicitConversions

sealed abstract class LegendSize(val className: String) {

  override val toString: String = className

}

object LegendSize {
  case object XL extends LegendSize("govuk-fieldset__legend--xl")
  case object L extends LegendSize("govuk-fieldset__legend--l")
  case object M extends LegendSize("govuk-fieldset__legend--m")
  case object S extends LegendSize("govuk-fieldset__legend--s")

  implicit def sizeToOptionString(size: LegendSize): Option[String] = Some(size.toString)
  implicit def sizeToString(size: LegendSize): String               = size.toString
}
