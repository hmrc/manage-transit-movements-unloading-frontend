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
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputSelectViewBehaviours
import views.html.houseConsignment.index.items.document.TypeView

class TypeViewSpec extends InputSelectViewBehaviours[DocumentType] {

  override def form: Form[DocumentType] = new SelectableFormProvider()(NormalMode, prefix, SelectableList(values))

  override def applyView(form: Form[DocumentType]): HtmlFormat.Appendable =
    injector.instanceOf[TypeView].apply(form, mrn, arrivalId, NormalMode, values, houseConsignmentIndex, itemIndex, documentIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[DocumentType] = arbitraryDocumentType

  override val prefix: String = "houseConsignment.index.items.document.type"

  behave like pageWithTitle(houseConsignmentIndex.display, itemIndex.display)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(houseConsignmentIndex.display, itemIndex.display)

  behave like pageWithSelect()

  behave like pageWithHint("Enter the document name or code.")

  behave like pageWithSubmitButton("Continue")
}
