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

import controllers.routes
import models.{ArrivalId, Mode}
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html

case class OtherThingsToReportViewModel(
  title: String,
  heading: String,
  hint: Option[String],
  additionalHtml: Option[Html],
  requiredError: String,
  onSubmitCall: Call
)

object OtherThingsToReportViewModel {

  class OtherThingsToReportViewModelProvider {

    def apply(
      arrivalId: ArrivalId,
      mode: Mode,
      newAuth: Boolean
    )(implicit messages: Messages): OtherThingsToReportViewModel = {
      val prefix = if (newAuth) "otherThingsToReport.newAuth" else "otherThingsToReport.oldAuth"

      val hint = Option.when(newAuth)(messages("otherThingsToReport.newAuth.hint"))

      val additionalHtml = Option.when(newAuth) {
        s"""
          |<p class="govuk-body">${messages("otherThingsToReport.newAuth.paragraph1")}</p>
          |<p class="govuk-body">${messages("otherThingsToReport.newAuth.paragraph2")}
          |    <a id="link" class="govuk-link" href=${routes.NewAuthYesNoController.onSubmit(arrivalId, mode)}>
          |        ${messages("otherThingsToReport.newAuth.link")}
          |    </a>.
          |    ${messages("otherThingsToReport.newAuth.paragraph3")}
          |</p>
          |""".stripMargin
      }

      new OtherThingsToReportViewModel(
        title = messages(s"$prefix.title"),
        heading = messages(s"$prefix.heading"),
        hint = hint,
        additionalHtml = additionalHtml.map(Html(_)),
        requiredError = messages(s"$prefix.error.required"),
        onSubmitCall = routes.OtherThingsToReportController.onSubmit(arrivalId, mode)
      )
    }
  }
}
