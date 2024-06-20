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
import models.NormalMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.OtherThingsToReportViewModel
import views.behaviours.CharacterCountViewBehaviours
import views.html.OtherThingsToReportView

class OtherThingsToReportViewSpec extends CharacterCountViewBehaviours with Generators {

  private val viewModel = arbitrary[OtherThingsToReportViewModel].sample.value

  override def form: Form[String] = new OtherThingsToReportFormProvider()(viewModel.requiredError)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[OtherThingsToReportView].apply(form, mrn, arrivalId, otherThingsToReportLength, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "otherThingsToReport.oldAuth",
      "otherThingsToReport.newAuth"
    )
    .sample
    .value

  behave like pageWithTitle(viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(viewModel.heading)

  behave like pageWithCharacterCount(otherThingsToReportLength)

  behave like pageWithSubmitButton("Continue")

  "when newAuth is false" - {
    val viewModelOldAuth = viewModel.copy(newAuth = false, hint = None)
    val doc = parseView(
      injector.instanceOf[OtherThingsToReportView].apply(form, mrn, arrivalId, otherThingsToReportLength, NormalMode, viewModelOldAuth)(fakeRequest, messages)
    )

    behave like pageWithoutHint(doc) //TODO: Test fails due to character count hint existing
  }

  "when newAuth is true" - {

    val hint             = "Each seal can be up to 20 characters long and include both letters and numbers."
    val viewModelNewAuth = viewModel.copy(newAuth = true, hint = Some(hint))
    val doc = parseView(
      injector.instanceOf[OtherThingsToReportView].apply(form, mrn, arrivalId, otherThingsToReportLength, NormalMode, viewModelNewAuth)(fakeRequest, messages)
    )

    behave like pageWithContent(doc, "p", "Only enter original seals affixed by an authorised consignor or replacement seals from a customs authority.")

    behave like pageWithPartialContent(doc, "p", "If any seals are broken, you must")

    behave like pageWithLink(
      doc = doc,
      id = "link",
      expectedText = "select no to using the revised unloading procedure",
      expectedHref = s"/manage-transit-movements/unloading/${viewModel.arrivalId.value}/new-auth"
    )

    behave like pageWithPartialContent(doc, "p", ". You will then need to unload the goods and report any discrepancies.")

    behave like pageWithHint(doc, viewModelNewAuth.hint.get)
  }
}
