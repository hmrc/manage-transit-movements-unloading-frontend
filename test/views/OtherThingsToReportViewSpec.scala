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
import play.twirl.api.HtmlFormat
import viewModels.OtherThingsToReportViewModel
import viewModels.OtherThingsToReportViewModel.OtherThingsToReportViewModelProvider
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

  private val hint = "Each seal can be up to 20 characters long and include both letters and numbers."

  "when newAuth is false" - {
    val viewModel = new OtherThingsToReportViewModelProvider().apply(arrivalId, mode, newAuth = false, sealsReplaced = None)
    val doc = parseView(
      injector.instanceOf[OtherThingsToReportView].apply(form, mrn, arrivalId, otherThingsToReportLength, NormalMode, viewModel)(fakeRequest, messages)
    )

    behave like pageWithoutHint(doc, hint)
  }

  "when newAuth is true and sealsReplaced is false" - {

    val viewModel = new OtherThingsToReportViewModelProvider().apply(arrivalId, mode, newAuth = true, sealsReplaced = Some(false))
    val doc = parseView(
      injector.instanceOf[OtherThingsToReportView].apply(form, mrn, arrivalId, otherThingsToReportLength, NormalMode, viewModel)(fakeRequest, messages)
    )

    behave like pageWithContent(doc, "p", "Only enter original seals affixed by an authorised consignor.")

    behave like pageWithPartialContent(doc, "p", "If any seals are broken, you must")

    behave like pageWithLink(
      doc = doc,
      id = "link",
      expectedText = "select no to using the revised unloading procedure",
      expectedHref = s"/manage-transit-movements/unloading/$arrivalId/revised-unloading-procedure"
    )

    behave like pageWithPartialContent(doc, "p", ". You will then need to unload the goods and report any discrepancies.")

    behave like pageWithHint(doc, hint)
  }

  "when newAuth is true and sealsReplaced is true" - {

    val viewModel = new OtherThingsToReportViewModelProvider().apply(arrivalId, mode, newAuth = true, sealsReplaced = Some(true))
    val doc = parseView(
      injector.instanceOf[OtherThingsToReportView].apply(form, mrn, arrivalId, otherThingsToReportLength, NormalMode, viewModel)(fakeRequest, messages)
    )

    behave like pageWithContent(doc, "p", "Only enter original seals affixed by an authorised consignor or replacement seals from a customs authority.")

    behave like pageWithPartialContent(doc, "p", "If any seals are broken, you must")

    behave like pageWithLink(
      doc = doc,
      id = "link",
      expectedText = "select no to using the revised unloading procedure",
      expectedHref = s"/manage-transit-movements/unloading/$arrivalId/revised-unloading-procedure"
    )

    behave like pageWithPartialContent(doc, "p", ". You will then need to unload the goods and report any discrepancies.")

    behave like pageWithHint(doc, hint)
  }
}
