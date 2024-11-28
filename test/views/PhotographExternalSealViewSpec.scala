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
import views.html.PhotographExternalSealView

import java.time.Year

class PhotographExternalSealViewSpec extends ViewBehaviours {

  private val expiryYear = Year.now().getValue + 3

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[PhotographExternalSealView].apply(mrn, arrivalId, NormalMode, expiryYear)(fakeRequest, messages)

  override val prefix: String = "photographExternalSeal"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithContent("p", "This is for evidence that the seal on the outside of the container or freight vehicle has been checked.")

  behave like pageWithContent("p", s"You must keep the photograph in your records until at least 1 January ${expiryYear.toString}.")

  behave like pageWithSubmitButton("Continue")
}
