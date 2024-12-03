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

import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.RevisedUnloadingProcedureConditionsYesNoView

class RevisedUnloadingProcedureConditionsYesNoViewSpec extends YesNoViewBehaviours {

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[RevisedUnloadingProcedureConditionsYesNoView].apply(form, mrn, arrivalId, NormalMode)(fakeRequest, messages)

  override val prefix: String = "revisedUnloadingProcedureConditionsYesNo"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithContent("p", "You can only use the revised unloading procedure if:")

  behave like pageWithList(
    "govuk-list--bullet",
    "there are no discrepancies between the transit movement and unloading permission",
    "there are no broken seals or they are not applicable, like goods on flatbed trucks or vehicles driven to their destination",
    "the movement started in an authorised consignor location",
    "all items have the same office of destination"
  )

  behave like pageWithContent("legend", "Does the movement meet all these conditions?")

  behave like pageWithRadioItems(legendIsHeading = false)

  behave like pageWithSubmitButton("Continue")
}
