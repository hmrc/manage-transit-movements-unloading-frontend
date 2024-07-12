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
import viewModels.UnloadingGuidanceViewModel
import views.behaviours.ViewBehaviours
import views.html.UnloadingGuidanceView

class UnloadingGuidanceViewSpec extends ViewBehaviours {

  private val viewModel = new UnloadingGuidanceViewModel(false, true)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[UnloadingGuidanceView].apply(mrn, arrivalId, messageId, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = "unloadingGuidance.notNewAuth"

  behave like pageWithTitle(false, true)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(false, true)

  behave like pageWithContent(
    "p",
    s"When unloading, check that the goods match the unloading permission for Movement Reference Number (MRN) $mrn. Take note of any discrepancies as you will need to include them in your unloading remarks."
  )

  behave like pageWithLink(
    id = "download",
    expectedText = "Download the Unloading Permission PDF",
    expectedHref = s"http://localhost:9485/manage-transit-movements/${arrivalId.value}/unloading-permission-document/$messageId"
  )

  behave like pageWithSubmitButton("Continue")
}

class UnloadingGuidanceViewSpecNewAuthGoodsTooLargeSpec extends ViewBehaviours {

  private val viewModel = new UnloadingGuidanceViewModel(true, true)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[UnloadingGuidanceView].apply(mrn, arrivalId, messageId, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = "unloadingGuidance.newAuth.goodsTooLargeYes"

  behave like pageWithTitle(true, true)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(true, true)

  behave like pageWithContent(
    "p",
    s"When checking the goods, make sure that the goods match the unloading permission for Movement Reference Number (MRN) $mrn. Take note of any discrepancies as you will need to include them in your unloading remarks."
  )

  behave like pageWithLink(
    id = "download",
    expectedText = "Download the Unloading Permission PDF",
    expectedHref = s"http://localhost:9485/manage-transit-movements/${arrivalId.value}/unloading-permission-document/$messageId"
  )

  behave like pageWithSubmitButton("Continue")
}

class UnloadingGuidanceViewSpecNewAuthGoodsNotTooLargeSpec extends ViewBehaviours {

  private val viewModel = new UnloadingGuidanceViewModel(true, false)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[UnloadingGuidanceView].apply(mrn, arrivalId, messageId, NormalMode, viewModel)(fakeRequest, messages)

  override val prefix: String = "unloadingGuidance.newAuth.goodsTooLargeNo"

  behave like pageWithTitle(true, false)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading(true, true)

  behave like pageWithContent(
    "p",
    s"If you suspect any discrepancies, you must select no to using the revised unloading procedure. You will then need to unload the goods and report any discrepancies."
  )

  behave like pageWithLink(
    id = "download",
    expectedText = "Download the Unloading Permission PDF",
    expectedHref = s"http://localhost:9485/manage-transit-movements/${arrivalId.value}/unloading-permission-document/$messageId"
  )

  behave like pageWithLink(
    id = "download1",
    expectedText = "select no to using the revised unloading procedure.",
    expectedHref = s"/manage-transit-movements/unloading/${arrivalId.value}/revised-unloading-procedure"
  )
  behave like pageWithSubmitButton("Continue")
}
