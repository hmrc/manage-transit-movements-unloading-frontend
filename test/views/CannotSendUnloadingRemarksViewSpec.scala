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

import generators.Generators
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.CannotSendUnloadingRemarksView

class CannotSendUnloadingRemarksViewSpec extends ViewBehaviours with Generators {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[CannotSendUnloadingRemarksView].apply()(fakeRequest, messages)

  override val prefix: String = "cannotSendUnloadingRemarks"

  behave like pageWithTitle()

  behave like pageWithBackLink

  behave like pageWithHeading()

  behave like pageWithPartialContent("p", "If you still need to send these unloading remarks, you must ")

  behave like pageWithLink(
    id = "contact",
    expectedText = "contact the New Computerised Transit System (NCTS) Helpdesk (opens in a new tab)",
    expectedHref = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/new-computerised-transit-system-enquiries"
  )
  behave like pageWithLink(
    id = "contactText",
    expectedText = "Back to arrival notification",
    expectedHref = "http://localhost:9485/manage-transit-movements/view-arrivals"
  )

}
