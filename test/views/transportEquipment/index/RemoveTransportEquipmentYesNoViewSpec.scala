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

import models.{Mode, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.RemoveTransportEquipmentYesNoView

class RemoveTransportEquipmentYesNoViewSpec extends YesNoViewBehaviours {

  val mode: Mode = NormalMode

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveTransportEquipmentYesNoView]
      .apply(form, mrn, arrivalId, transportMeansIndex, mode)(fakeRequest, messages)

  override val prefix: String = "transportEquipment.index.removeTransportEquipmentYesNo"

  behave like pageWithTitle(transportEquipmentIndex.display)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: $mrn")

  behave like pageWithHeading(transportEquipmentIndex.display)

  behave like pageWithRadioItems(args = Seq(equipmentIndex.display))

  behave like pageWithSubmitButton("Continue")
}
