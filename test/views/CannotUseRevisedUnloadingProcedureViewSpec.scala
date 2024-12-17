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
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.CannotUseRevisedUnloadingProcedureView

class CannotUseRevisedUnloadingProcedureViewSpec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[CannotUseRevisedUnloadingProcedureView].apply(mrn, arrivalId, NormalMode)(fakeRequest, messages)

  override val prefix: String = "cannotUseRevisedUnloadingProcedure"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithContent("p", "This is due to the discrepancies between the transit movement and unloading permission.")

  behave like pageWithContent("p", "Continue with your unloading remarks and report the discrepancies.")

  behave like pageWithSubmitButton("Continue")
}
