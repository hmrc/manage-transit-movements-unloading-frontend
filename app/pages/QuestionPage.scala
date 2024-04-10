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

package pages

import generated.CC043CType
import play.api.libs.json.{__, Reads}
import queries.{Gettable, Settable}

trait QuestionPage[A] extends Page with Gettable[A] with Settable[A] {

  def valueInIE043(ie043: CC043CType): Option[A] = None

  def readNullable(implicit reads: Reads[A]): CC043CType => Reads[Option[A]] = ie043 => {
    (__ \ this.toString).readNullable[A].map {
      case Some(value) if !valueInIE043(ie043).contains(value) => Some(value)
      case _                                                   => None
    }
  }
}
