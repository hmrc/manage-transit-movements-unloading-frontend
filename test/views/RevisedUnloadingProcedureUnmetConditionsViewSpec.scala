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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.RevisedUnloadingProcedureUnmetConditionsView

class RevisedUnloadingProcedureUnmetConditionsViewSpec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[RevisedUnloadingProcedureUnmetConditionsView].apply(mrn, arrivalId)(fakeRequest, messages)

  override val prefix: String = "revisedUnloadingProcedureUnmetConditions"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithContent("p", "This is because the movement does not meet all conditions for using it.")

  behave like pageWithContent("p", "You must continue your unloading remarks without using the revised unloading procedure.")

  behave like pageWithSubmitButton("Continue")
}
