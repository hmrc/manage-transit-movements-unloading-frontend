/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.i18n.Messages
import controllers.routes
import models.{ArrivalId, Mode}
import play.api.mvc.Call

case class OtherThingsToReportViewModel(
  newAuth: Boolean,
  prefix: String,
  title: String,
  heading: String,
  hint: Option[String],
  requiredError: String,
  arrivalId: ArrivalId,
  mode: Mode
) {

  def onSubmit(): Call = routes.OtherThingsToReportController.onSubmit(
    arrivalId,
    mode
  )

  def newAuthLink(): Call = routes.NewAuthYesNoController.onSubmit(
    arrivalId,
    mode
  )

}

object OtherThingsToReportViewModel {

  class OtherThingsToReportViewModelProvider {

    def apply(
      arrivalId: ArrivalId,
      mode: Mode,
      newAuth: Boolean
    )(implicit messages: Messages): OtherThingsToReportViewModel = {

      def prefix: String = if (newAuth) "otherThingsToReport.newAuth" else "otherThingsToReport.oldAuth"

      def title(implicit messages: Messages): String =
        messages(s"$prefix.title")

      def heading(implicit messages: Messages): String =
        messages(s"$prefix.heading")

      def hint(implicit messages: Messages): Option[String] =
        if (newAuth) Some(messages("otherThingsToReport.newAuth.hint")) else None

      def requiredError(implicit messages: Messages): String =
        messages(s"$prefix.error.required")

      new OtherThingsToReportViewModel(newAuth, prefix, title, heading, hint, requiredError, arrivalId, mode)
    }
  }
}
