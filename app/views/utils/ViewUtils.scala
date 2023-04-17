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

package views.utils

import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.Input
import uk.gov.hmrc.hmrcfrontend.views.implicits.RichErrorSummarySupport

import java.time.LocalDate

object ViewUtils {

  def breadCrumbTitle(title: String, mainContent: Html)(implicit messages: Messages): String =
    (if (mainContent.body.contains("govuk-error-summary")) s"${messages("error.title.prefix")} " else "") +
      s"$title - ${messages("site.service_name")} - GOV.UK"

  def errorClass(error: Option[FormError], dateArg: String): String =
    error.fold("") {
      e =>
        if (e.args.contains(dateArg) || e.args.isEmpty) {
          "govuk-input--error"
        } else {
          ""
        }
    }

  // TODO refactor this maybe? Going to need this for every ViewModel type going forward

  implicit class RadiosImplicits(radios: Radios)(implicit messages: Messages) extends RichRadiosSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): Radios =
      caption match {
        case Some(value) => radios.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => radios.withHeading(Text(heading))
      }

    def withLegend(legend: String, legendIsVisible: Boolean = true): Radios = {
      val labelClass = if (legendIsVisible) "govuk-fieldset__legend--m" else "govuk-visually-hidden govuk-!-display-inline"
      radios.copy(
        fieldset = Some(Fieldset(legend = Some(Legend(content = Text(legend), classes = labelClass, isPageHeading = false))))
      )
    }
  }

  implicit class CharacterCountImplicits(characterCount: CharacterCount)(implicit messages: Messages) extends RichCharacterCountSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): CharacterCount =
      caption match {
        case Some(value) => characterCount.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => characterCount.withHeading(Text(heading))
      }
  }

  implicit class ErrorSummaryImplicits(errorSummary: ErrorSummary)(implicit messages: Messages) extends RichErrorSummarySupport {

    def withDateErrorMapping(form: Form[LocalDate], fieldName: String): ErrorSummary = {
      val args = Seq("day", "month", "year")
      val arg  = form.errors.flatMap(_.args).find(args.contains).getOrElse(args.head)
      errorSummary.withFormErrorsAsText(form, mapping = Map(fieldName -> s"${fieldName}_$arg"))
    }
  }

  implicit class SelectImplicits(select: Select)(implicit messages: Messages) extends RichSelectSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): Select =
      caption match {
        case Some(value) => select.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => select.withHeading(Text(heading))
      }
  }

  implicit class InputImplicits(input: Input)(implicit messages: Messages) extends RichInputSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): Input =
      caption match {
        case Some(value) => input.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => input.withHeading(Text(heading))
      }
  }

  implicit class StringImplicits(string: String) {
    def toParagraph: Html = Html(s"""<p class="govuk-body">$string</p>""")
  }

}
