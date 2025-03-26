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

import play.api.data.FormError
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*
import uk.gov.hmrc.govukfrontend.views.implicits.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.ErrorLink
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.Input
import uk.gov.hmrc.hmrcfrontend.views.implicits.RichDateInputSupport

object ViewUtils {

  def breadCrumbTitle(title: String, mainContent: Html)(implicit messages: Messages): String =
    (if (mainContent.body.contains("govuk-error-summary")) s"${messages("error.title.prefix")} " else "") +
      s"$title - ${messages("site.title.prefix")} - ${messages("site.service_name")} - GOV.UK"

  def errorClass(errors: Seq[FormError], dateArg: String): String =
    if (errors.flatMap(_.args).contains(dateArg)) "govuk-input--error" else ""

  implicit class RadiosImplicits(radios: Radios)(implicit messages: Messages) extends RichRadiosSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): Radios =
      caption match {
        case Some(value) => radios.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => radios.withHeading(Text(heading))
      }

    def withLegend(legend: String, legendIsVisible: Boolean = true): Radios =
      radios.copy(
        fieldset = Some(Fieldset().withLegend(legend, legendIsVisible))
      )
  }

  implicit class FieldsetImplicits(fieldset: Fieldset) {

    def withLegend(legend: String, legendIsVisible: Boolean): Fieldset = {
      val labelClass = if (legendIsVisible) "govuk-fieldset__legend--m" else "govuk-visually-hidden govuk-!-display-inline"
      fieldset.copy(legend = Some(Legend(content = Text(legend), classes = labelClass, isPageHeading = false)))
    }
  }

  implicit class CharacterCountImplicits(characterCount: CharacterCount)(implicit messages: Messages) extends RichCharacterCountSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): CharacterCount =
      caption match {
        case Some(value) => characterCount.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => characterCount.withHeading(Text(heading))
      }
  }

  implicit class DateTimeRichFormErrors(formErrors: Seq[FormError])(implicit messages: Messages) {

    def toErrorLinks: Seq[ErrorLink] =
      formErrors
        .distinctBy(_.message)
        .map {
          formError =>
            ErrorLink(
              href = Some(s"#${formError.key}"),
              content = messages(formError.message, formError.args*).toText
            )
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

  implicit class StringsImplicits(strings: Seq[String]) {
    def toHtml: Html = HtmlFormat.fill(strings.map(_.toParagraph))
  }

  implicit class DateInputImplicits(dateInput: DateInput)(implicit messages: Messages) extends RichDateInputSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): DateInput =
      caption match {
        case Some(value) => dateInput.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => dateInput.withHeading(Text(heading))
      }

    def withVisuallyHiddenLegend(legend: String): DateInput =
      dateInput.copy(fieldset = Some(Fieldset(legend = Some(Legend(content = Text(legend), isPageHeading = false, classes = "govuk-visually-hidden")))))
  }

}
