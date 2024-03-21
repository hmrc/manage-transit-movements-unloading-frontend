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

package views.transportEquipment.index.seals

import generators.Generators
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.transportEquipment.index.seals.RemoveSealYesNoView

class RemoveSealYesNoViewSpec extends YesNoViewBehaviours with Generators {

  private val sealIdNumber = nonEmptyString.sample.value

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[RemoveSealYesNoView].apply(form, mrn, arrivalId, NormalMode, NormalMode, equipmentIndex, sealIndex, sealIdNumber)(fakeRequest, messages)

  override val prefix: String = "transportEquipment.index.seal.removeSealYesNo"

  behave like pageWithTitle(equipmentIndex.display, sealIdNumber)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(equipmentIndex.display, sealIdNumber)

  behave like pageWithRadioItems(args = Seq(equipmentIndex.display, sealIdNumber))

  behave like pageWithSubmitButton("Continue")
}
