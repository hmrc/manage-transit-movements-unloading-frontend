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
import views.html.NewAuthYesNoView

class NewAuthYesNoViewSpec extends YesNoViewBehaviours {

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[NewAuthYesNoView].apply(form, mrn, arrivalId, NormalMode)(fakeRequest, messages)

  override val prefix: String = "newAuthYesNo"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithContent("p", "This allows you to make unloading remarks without physically unloading the goods.")

  behave like pageWithHint(
    "You can only use the revised unloading procedure if: " +
      "all items have the same office of destination " +
      "there are no broken seals " +
      "there are no discrepancies between the transit movement and unloading permission"
  )

  behave like pageWithList(
    "govuk-list--bullet",
    "all items have the same office of destination",
    "there are no broken seals",
    "there are no discrepancies between the transit movement and unloading permission"
  )

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Continue")
}
