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

import forms.SelectableFormProvider
import models.reference.DocumentType
import models.{NormalMode, SelectableList}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.items.document.TypeViewModel
import views.behaviours.InputSelectViewBehaviours
import views.html.houseConsignment.index.items.document.TypeView

class TypeViewSpec extends InputSelectViewBehaviours[DocumentType] {

  private val viewModel: TypeViewModel =
    arbitrary[TypeViewModel].sample.value

  private val viewModelMaxedOutTransport: TypeViewModel =
    viewModel.copy(documents = arbitraryHouseConsignmentLevelDocumentsMaxedOutTransport.arbitrary.sample.value)

  private val viewModelMaxedOutSupport: TypeViewModel =
    viewModel.copy(documents = arbitraryHouseConsignmentLevelDocumentsMaxedOutSupport.arbitrary.sample.value)

  override def form: Form[DocumentType] = new SelectableFormProvider()(NormalMode, prefix, SelectableList(values))

  override def applyView(form: Form[DocumentType]): HtmlFormat.Appendable =
    injector
      .instanceOf[TypeView]
      .apply(
        form,
        mrn,
        arrivalId,
        NormalMode,
        NormalMode,
        NormalMode,
        values,
        viewModel,
        houseConsignmentIndex,
        itemIndex,
        documentIndex
      )(fakeRequest, messages, frontendAppConfig)

  def applyMaxedOutTransportDocsView: HtmlFormat.Appendable =
    injector
      .instanceOf[TypeView]
      .apply(
        form,
        mrn,
        arrivalId,
        NormalMode,
        NormalMode,
        NormalMode,
        values,
        viewModelMaxedOutTransport,
        houseConsignmentIndex,
        itemIndex,
        documentIndex
      )(fakeRequest, messages, frontendAppConfig)

  def applyMaxedOutSupportDocsView: HtmlFormat.Appendable =
    injector
      .instanceOf[TypeView]
      .apply(
        form,
        mrn,
        arrivalId,
        NormalMode,
        NormalMode,
        NormalMode,
        values,
        viewModelMaxedOutSupport,
        houseConsignmentIndex,
        itemIndex,
        documentIndex
      )(fakeRequest, messages, frontendAppConfig)

  implicit override val arbitraryT: Arbitrary[DocumentType] = arbitraryDocumentType

  override val prefix: String = Gen
    .oneOf(
      "houseConsignment.index.items.document.type.NormalMode",
      "houseConsignment.index.items.document.type.CheckMode"
    )
    .sample
    .value

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithSelect()

  behave like pageWithHint("Enter the document name or code.")

  behave like pageWithSubmitButton("Continue")

  "page with maxed out transport documents content" - {
    val doc = parseView(applyMaxedOutTransportDocsView)

    behave like pageWithContent(
      doc,
      "p",
      "You cannot add any more transport documents to all items. To add another, you need to remove one first. You can, however, still add a supporting document to your items."
    )
  }

  "page with maxed out support documents content" - {
    val doc = parseView(applyMaxedOutSupportDocsView)

    behave like pageWithContent(
      doc,
      "p",
      "You cannot add any more supporting documents to all items. To add another, you need to remove one first. You can, however, still add a transport document to your items."
    )
  }
}
