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

package views.houseConsignment.index

import generators.Generators
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.houseConsignment.index.RemoveHouseConsignmentYesNoView

class RemoveHouseConsignmentYesNoViewSpec extends YesNoViewBehaviours with Generators {

  private val mode = NormalMode

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveHouseConsignmentYesNoView]
      .apply(form, mrn, arrivalId, houseConsignmentIndex, mode)(fakeRequest, messages)

  override val prefix: String = "houseConsignment.index.removeHouseConsignmentYesNo"

  behave like pageWithTitle(houseConsignmentIndex.display)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(houseConsignmentIndex.display)

  behave like pageWithRadioItems(args = Seq(houseConsignmentIndex.display))

  behave like pageWithSubmitButton("Continue")

}
