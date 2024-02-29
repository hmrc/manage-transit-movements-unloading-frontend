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

package views.departureMeansOfTransport

import models.departureTransportMeans.TransportMeansIdentification
import models.{NormalMode, TransportMeans}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.RemoveDepartureMeansOfTransportYesNoView

class RemoveDepartureMeansOfTransportYesNoViewSpec extends YesNoViewBehaviours {

  val identificationType: TransportMeansIdentification = TransportMeansIdentification("80", "European vessel identification number (ENI Code)")

  val identificationNumber: Option[String] = Some("1234")

  val insetText: String = TransportMeans(identificationType.description, identificationNumber).asString
  val mode              = NormalMode

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveDepartureMeansOfTransportYesNoView]
      .apply(form, mrn, arrivalId, transportMeansIndex, insetText, mode)(fakeRequest, messages)

  override val prefix: String = "departureMeansOfTransport.index.removeDepartureMeansOfTransportYesNo"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: $mrn")

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithInsetText(s"${identificationType.description} - ${identificationNumber.get}")

  behave like pageWithSubmitButton("Continue")
}
