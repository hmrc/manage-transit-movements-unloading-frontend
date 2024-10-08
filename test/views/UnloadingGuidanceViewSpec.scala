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
import viewModels.{Para3, UnloadingGuidanceViewModel}
import views.behaviours.ViewBehaviours
import views.html.UnloadingGuidanceView

class UnloadingGuidanceViewSpec extends ViewBehaviours {

  private val viewModel =
    UnloadingGuidanceViewModel(
      title = "Unload the goods and note any discrepancies",
      heading = "Unload the goods and note any discrepancies",
      preLinkText = Some("preLinkText"),
      linkText = "select no to using the revised unloading procedure.",
      postLinkText = Some("postLinkText"),
      para1 = Some("para1"),
      para2 = Some(
        "When unloading, check that the goods match the unloading permission for Movement Reference Number (MRN) 19GB1234567890123. Take note of any discrepancies as you will need to include them in your unloading remarks."
      ),
      para3 = Some(Para3.apply("unloadingGuidance"))
    )

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[UnloadingGuidanceView].apply(mrn, arrivalId, messageId, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = "unloadingGuidance.notNewAuth"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithContent(
    "p",
    s"When unloading, check that the goods match the unloading permission for Movement Reference Number (MRN) $mrn. Take note of any discrepancies as you will need to include them in your unloading remarks."
  )

  behave like pageWithLink(
    id = "download",
    expectedText = "select no to using the revised unloading procedure.",
    expectedHref = s"http://localhost:9485/manage-transit-movements/${arrivalId.value}/unloading-permission-document/$messageId"
  )

  behave like pageWithSubmitButton("Continue")
}
