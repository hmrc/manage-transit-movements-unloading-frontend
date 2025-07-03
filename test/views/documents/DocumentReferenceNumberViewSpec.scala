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

package views.documents

import forms.DocumentReferenceNumberFormProvider
import models.NormalMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.documents.DocumentReferenceNumberViewModel
import views.behaviours.InputTextViewBehaviours
import views.html.documents.DocumentReferenceNumberView

class DocumentReferenceNumberViewSpec extends InputTextViewBehaviours[String] {

  private val viewModel: DocumentReferenceNumberViewModel =
    arbitrary[DocumentReferenceNumberViewModel].sample.value

  override def form: Form[String] = new DocumentReferenceNumberFormProvider()(viewModel.requiredError)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[DocumentReferenceNumberView].apply(form, mrn, arrivalId, NormalMode, viewModel, documentIndex)(fakeRequest, messages)

  override val prefix: String = Gen
    .oneOf(
      "document.referenceNumber.NormalMode",
      "document.referenceNumber.CheckMode"
    )
    .sample
    .value

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHint("This can be up to 70 characters long and include letters, numbers and full stops.")

  behave like pageWithInputText()

  behave like pageWithSubmitButton("Continue")
}
