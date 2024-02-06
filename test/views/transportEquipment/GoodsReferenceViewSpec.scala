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

package views.transportEquipment

import forms.SelectableFormProvider
import generated.{ConsignmentType05, GoodsReferenceType02, TransportEquipmentType05}
import models.reference.Item
import models.{Index, NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.transportEquipment.GoodsReferenceViewModel
import views.behaviours.InputSelectViewBehaviours
import views.html.houseConsignment.index.items.GoodsReferenceView

class GoodsReferenceViewSpec extends InputSelectViewBehaviours[Item] {

  implicit override val arbitraryT: Arbitrary[Item] = arbitraryItem

  override lazy val values: Seq[Item] = Seq(Item(123, "seq1"))

  val ie043Answers = emptyUserAnswers.ie043Data.copy(Consignment =
    Some(
      ConsignmentType05(
        TransportEquipment = Seq(TransportEquipmentType05("seq1", None, 0, Nil, Seq(GoodsReferenceType02("seq1", 123)))),
        containerIndicator = arbitraryFlag.arbitrary.sample.get
      )
    )
  )
  override def form: Form[Item] = new SelectableFormProvider()(NormalMode, prefix, SelectableList(values))
  private val viewModel         = GoodsReferenceViewModel(emptyUserAnswers.copy(ie043Data = ie043Answers), None)

  override def applyView(form: Form[Item]): HtmlFormat.Appendable =
    injector.instanceOf[GoodsReferenceView].apply(form, arrivalId, Index(0), mrn, viewModel, NormalMode)(fakeRequest, messages)

  override val prefix: String = "transport.equipment.selectItems"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithSubmitButton("Continue")
}
