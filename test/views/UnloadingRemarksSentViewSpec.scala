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

import base.SpecBase
import models.UnloadingRemarksSentViewModel
import models.reference.CustomsOffice
import play.twirl.api.HtmlFormat
import views.behaviours.PanelViewBehaviours
import views.html.UnloadingRemarksSentView

class UnloadingRemarksSentViewSpec extends PanelViewBehaviours with SpecBase {

  override val prefix: String = "unloadingRemarksSent"

  val officeOfDestination: CustomsOffice = new CustomsOffice("ABC12345", "Test", "GB", Some("+44 7760663422"))
  val noTelephone: CustomsOffice         = new CustomsOffice("ABC12345", "Test", "GB", None)
  val noName: CustomsOffice              = new CustomsOffice("ABC12345", "", "GB", Some("+44 7760663422"))
  val noNameNoTelephone: CustomsOffice   = new CustomsOffice("ABC12345", "", "GB", None)

  override def view: HtmlFormat.Appendable =
    injector
      .instanceOf[UnloadingRemarksSentView]
      .apply(mrn.value, viewModel = UnloadingRemarksSentViewModel(officeOfDestination, "GB000060"))(fakeRequest, messages)

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithPanel(
    body = s"for Movement Reference Number (MRN) $mrn"
  )

  behave like pageWithContent("h2", "What happens next")

  behave like pageWithLink(
    id = "viewArrivalId",
    expectedText = "Check the status of arrival notifications",
    expectedHref = "http://localhost:9485/manage-transit-movements/view-arrival-notifications"
  )

  behave like pageWithLink(
    id = "createArrivalId",
    expectedText = "Make another arrival notification",
    expectedHref = frontendAppConfig.arrivalsFrontendUrl
  )

  behave like pageWithContent("h2", "Before you go")

  behave like pageWithLink(
    id = "takeSurvey",
    expectedText = "Take a short survey",
    expectedHref = frontendAppConfig.feedbackUrl
  )

  "Customs office with a name and no telephone" - {
    val view = injector.instanceOf[UnloadingRemarksSentView].apply(mrn.value, UnloadingRemarksSentViewModel(noTelephone, "GB000060"))(fakeRequest, messages)

    val doc = parseView(view)

    behave like pageWithContent(
      doc,
      "p",
      s"If the goods are not released when expected or you have another problem, contact Customs at Test."
    )

  }

  "Customs office with no name and a telephone" - {
    val view = injector.instanceOf[UnloadingRemarksSentView].apply(mrn.value, UnloadingRemarksSentViewModel(noName, "GB000060"))(fakeRequest, messages)

    val doc = parseView(view)

    behave like pageWithContent(
      doc,
      "p",
      s"If the goods are not released when expected or you have another problem, contact Customs office ABC12345 on +44 7760663422."
    )

  }

  "Customs office with no name and no telephone" - {
    val view =
      injector.instanceOf[UnloadingRemarksSentView].apply(mrn.value, UnloadingRemarksSentViewModel(noNameNoTelephone, "GB000060"))(fakeRequest, messages)

    val doc = parseView(view)

    behave like pageWithContent(
      doc,
      "p",
      s"If the goods are not released when expected or you have another problem, contact Customs office GB000060."
    )

  }

}
