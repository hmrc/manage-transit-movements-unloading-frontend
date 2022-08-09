/*
 * Copyright 2022 HM Revenue & Customs
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
import views.behaviours.PanelViewBehaviours
import views.html.ConfirmationView

class ConfirmationViewSpec extends PanelViewBehaviours {

  override val prefix: String = "confirmation"

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[ConfirmationView].apply(mrn)(fakeRequest, messages)

  behave like pageWithTitle()

  behave like pageWithoutBackLink

  behave like pageWithHeading()

  behave like pageWithPanel(
    body = s"for movement reference number $mrn"
  )

  behave like pageWithContent("h2", "What happens next")

  behave like pageWithLink(
    id = "manage-transit-movements",
    expectedText = "Go to arrival notifications",
    expectedHref = "http://localhost:9485/manage-transit-movements/view-arrivals"
  )

  behave like pageWithPartialContent("p", "You must wait for the goods to be released.")

  behave like pageWithPartialContent("p", "to see the latest messages")

}
