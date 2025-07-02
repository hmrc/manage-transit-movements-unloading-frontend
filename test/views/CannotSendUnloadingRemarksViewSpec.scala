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

import models.CannotSendUnloadingRemarksViewModel
import models.reference.CustomsOffice
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.CannotSendUnloadingRemarksView

class CannotSendUnloadingRemarksViewSpec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector
      .instanceOf[CannotSendUnloadingRemarksView]
      .apply(mrn, arrivalId, CannotSendUnloadingRemarksViewModel(CustomsOffice("id", "name", "countryId", None), "GB000060"))(fakeRequest, messages)

  override val prefix: String = "cannotSendUnloadingRemarks"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithoutHint()

  behave like pageWithContent("p",
                              s"This may be because the unloading remarks have already been sent or the office of destination has rejected the notification."
  )

  behave like pageWithLink(
    id = "viewStatus",
    expectedText = "View the status of this notification",
    expectedHref = "http://localhost:9485/manage-transit-movements/view-arrival-notifications"
  )

  behave like pageWithContent("h2", "Before you go")

  behave like pageWithLink(
    id = "takeSurvey",
    expectedText = "Take a short survey",
    expectedHref = frontendAppConfig.feedbackUrl
  )

  "Customs office with a name and no telephone" - {
    val view = injector
      .instanceOf[CannotSendUnloadingRemarksView]
      .apply(mrn, arrivalId, CannotSendUnloadingRemarksViewModel(CustomsOffice("id", "OfficeName", "countryId", None), "GB000060"))(fakeRequest, messages)

    val doc = parseView(view)

    behave like pageWithContent(
      doc,
      "p",
      s"If you have any questions, contact Customs at OfficeName."
    )

  }

  "Customs office with no name and a telephone" - {
    val view = injector
      .instanceOf[CannotSendUnloadingRemarksView]
      .apply(mrn, arrivalId, CannotSendUnloadingRemarksViewModel(CustomsOffice("id", "", "countryId", Some("12234")), "GB000060"))(fakeRequest, messages)

    val doc = parseView(view)

    behave like pageWithContent(
      doc,
      "p",
      s"If you have any questions, contact Customs office id on 12234."
    )

  }

  "Customs office with no name and no telephone" - {
    val view = injector
      .instanceOf[CannotSendUnloadingRemarksView]
      .apply(mrn, arrivalId, CannotSendUnloadingRemarksViewModel(CustomsOffice("id", "", "countryId", None), "GB000060"))(fakeRequest, messages)

    val doc = parseView(view)

    behave like pageWithContent(
      doc,
      "p",
      s"If you have any questions, contact Customs office GB000060."
    )

  }

}
