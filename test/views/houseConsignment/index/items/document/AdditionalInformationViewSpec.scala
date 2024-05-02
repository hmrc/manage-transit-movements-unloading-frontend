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

package views.houseConsignment.index.items.document

import forms.Constants.maxAdditionalInfoLength
import forms.ItemsAdditionalInformationFormProvider
import generators.Generators
import models.NormalMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.items.document.ItemsAdditionalInformationViewModel
import views.behaviours.CharacterCountViewBehaviours
import views.html.houseConsignment.index.items.document.AdditionalInformationView

class AdditionalInformationViewSpec extends CharacterCountViewBehaviours with Generators {

  private val viewModel: ItemsAdditionalInformationViewModel =
    arbitrary[ItemsAdditionalInformationViewModel].sample.value

  override def form: Form[String] = new ItemsAdditionalInformationFormProvider()(viewModel.requiredError)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector
      .instanceOf[AdditionalInformationView]
      .apply(form, mrn, arrivalId, NormalMode, NormalMode, NormalMode, viewModel, houseConsignmentIndex, itemIndex, documentIndex)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "houseConsignment.index.items.document.additionalInformation.NormalMode",
      "houseConsignment.index.items.document.additionalInformation.CheckMode"
    )
    .sample
    .value

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithCharacterCount(maxAdditionalInfoLength)

  behave like pageWithSubmitButton("Continue")
}
