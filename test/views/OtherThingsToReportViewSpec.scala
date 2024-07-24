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

package views

import forms.Constants.otherThingsToReportLength
import forms.OtherThingsToReportFormProvider
import generators.Generators
import models.{Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.{Html, HtmlFormat}
import viewModels.OtherThingsToReportViewModel
import views.behaviours.CharacterCountViewBehaviours
import views.html.OtherThingsToReportView

class OtherThingsToReportViewSpec extends CharacterCountViewBehaviours with Generators {

  private val viewModel = arbitrary[OtherThingsToReportViewModel].sample.value

  private val mode: Mode = NormalMode

  override def form: Form[String] = new OtherThingsToReportFormProvider()(viewModel.requiredError, viewModel.maxLengthError, viewModel.invalidError)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[OtherThingsToReportView].apply(form, mrn, arrivalId, otherThingsToReportLength, mode, viewModel)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "otherThingsToReport.oldAuth",
      "otherThingsToReport.newAuthAndSealsReplaced"
    )
    .sample
    .value

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithCharacterCount(otherThingsToReportLength)

  behave like pageWithSubmitButton("Continue")

  private val hint = nonEmptyString.sample.value

  "oldAuth (without hint)" - {
    val viewModelOldAuth = viewModel.copy(hint = None)
    val doc = parseView(
      injector
        .instanceOf[OtherThingsToReportView]
        .apply(form, mrn, arrivalId, otherThingsToReportLength, NormalMode, viewModelOldAuth)(fakeRequest, messages)
    )

    behave like pageWithoutHint(doc, hint)
  }

  "newAuth (with hint and additionalHtml)" - {

    val additionalHtml = s"""
                  |<p class="govuk-body">paragraph1</p>
                  |<p class="govuk-body">paragraph2
                  |    <a id="link" class="govuk-link" href="url">
                  |        link
                  |    </a>.
                  |    paragraph3
                  |</p>
                  |""".stripMargin

    val viewModelNewAuth = viewModel.copy(hint = Some(hint), additionalHtml = Some(additionalHtml).map(Html(_)))

    val doc = parseView(
      injector.instanceOf[OtherThingsToReportView].apply(form, mrn, arrivalId, otherThingsToReportLength, NormalMode, viewModelNewAuth)(fakeRequest, messages)
    )

    behave like pageWithContent(doc, "p", "paragraph1")

    behave like pageWithPartialContent(doc, "p", "paragraph2")

    behave like pageWithLink(
      doc = doc,
      id = "link",
      expectedText = "link",
      expectedHref = "url"
    )

    behave like pageWithPartialContent(doc, "p", "paragraph3")

    behave like pageWithHint(doc, hint)
  }
}
