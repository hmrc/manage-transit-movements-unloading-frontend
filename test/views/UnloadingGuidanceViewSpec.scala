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
import views.html.UnloadingGuidanceView

class UnloadingGuidanceViewSpec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[UnloadingGuidanceView].apply(mrn, arrivalId, messageId)(fakeRequest, messages)

  override val prefix: String = "unloadingGuidance"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(mrn.toString)

  behave like pageWithHeading()

  behave like pageWithContent("p", s"When unloading, check that the goods match the Unloading Permission for Movement Reference Number (MRN) $mrn.")
  behave like pageWithPartialContent("p", "or further guidance,")

  behave like pageWithLink(
    id = "download",
    expectedText = "Download the Unloading Permission PDF",
    expectedHref = s"http://localhost:9485/manage-transit-movements/${arrivalId.value}/unloading-permission-document/$messageId"
  )

  behave like pageWithLinkAsButton("Continue", controllers.routes.UnloadingTypeController.onPageLoad(arrivalId, NormalMode).url)
}
