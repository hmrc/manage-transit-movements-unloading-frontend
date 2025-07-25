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

package views.transportEquipment.index

import forms.SelectableFormProvider.GoodsReferenceTypeFormProvider
import generated._
import models.reference.GoodsReference
import models.{Index, NormalMode, SelectableList}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputSelectViewBehaviours
import views.html.transportEquipment.index.GoodsReferenceView

class GoodsReferenceViewSpec extends InputSelectViewBehaviours[GoodsReference] {

  implicit override val arbitraryT: Arbitrary[GoodsReference] = arbitraryGoodsReference

  override lazy val values: Seq[GoodsReference] = Seq(GoodsReference(123, "Description 1"))

  val ie043Answers: CC043CType = emptyUserAnswers.ie043Data.copy(Consignment =
    Some(
      ConsignmentType05(
        TransportEquipment = Seq(TransportEquipmentType03(1, None, 0, Nil, Seq(GoodsReferenceType01(1, 123)))),
        containerIndicator = arbitraryFlag.arbitrary.sample.get
      )
    )
  )

  private val formProvider = new GoodsReferenceTypeFormProvider()

  override val field: String = formProvider.field

  override def form: Form[GoodsReference] = formProvider(NormalMode, prefix, SelectableList(values))

  override def applyView(form: Form[GoodsReference]): HtmlFormat.Appendable =
    injector
      .instanceOf[GoodsReferenceView]
      .apply(form, arrivalId, Index(0), itemIndex, mrn, values, NormalMode, NormalMode)(fakeRequest, messages)

  override val prefix: String = "transport.equipment.selectItems"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithSelect()

  behave like pageWithSubmitButton("Continue")
}
