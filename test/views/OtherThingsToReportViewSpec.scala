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
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.OtherThingsToReportViewModel
import views.behaviours.CharacterCountViewBehaviours
import views.html.OtherThingsToReportView

class OtherThingsToReportViewSpec extends CharacterCountViewBehaviours with Generators {

  val viewModelOldAuth: OtherThingsToReportViewModel = OtherThingsToReportViewModel(false)
  val viewModelNewAuth: OtherThingsToReportViewModel = OtherThingsToReportViewModel(true)

  override def form: Form[String] = new OtherThingsToReportFormProvider()(viewModelOldAuth.requiredError)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[OtherThingsToReportView].apply(form, mrn, arrivalId, otherThingsToReportLength, NormalMode, viewModelOldAuth)(fakeRequest, messages)

  def applyViewNewAuth: HtmlFormat.Appendable =
    injector.instanceOf[OtherThingsToReportView].apply(form, mrn, arrivalId, otherThingsToReportLength, NormalMode, viewModelNewAuth)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "otherThingsToReport.oldAuth",
      "otherThingsToReport.newAuth"
    )
    .sample
    .value

  "when old auth" - {

    behave like pageWithTitle(viewModelOldAuth.title)

    behave like pageWithBackLink()

    behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

    behave like pageWithHeading(viewModelOldAuth.heading)

    behave like pageWithCharacterCount(otherThingsToReportLength)

    behave like pageWithHint(s"You can enter up to $otherThingsToReportLength characters")

    behave like pageWithSubmitButton("Continue")
  }

  "alternative new auth content" - {

    val doc = parseView(applyViewNewAuth)

    behave like pageWithTitle(doc, viewModelNewAuth.prefix)

    behave like pageWithHeading(doc, viewModelNewAuth.prefix)

    behave like pageWithHint(doc, "Each seal can be up to 20 characters long and include both letters and numbers.")
  }
}
