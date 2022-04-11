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

package utils

import play.api.data.FormError
import play.api.i18n.Messages

object DateErrorFormatter {

  def formatArgs(args: Seq[Any])(implicit messages: Messages): Seq[String] = {
    val dateArgs = Seq("day", "month", "year")
    args.map(
      arg => if (dateArgs.contains(arg)) messages(s"date.$arg").toLowerCase else arg.toString
    )
  }

  def addErrorClass(error: Option[FormError], dateArg: String): String =
    if (error.isDefined) {
      if (error.get.args.contains(dateArg) || error.get.args.isEmpty) {
        s"govuk-input--error"
      } else {
        ""
      }
    } else {
      ""
    }

}
