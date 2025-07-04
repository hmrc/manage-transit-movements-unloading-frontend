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

package views.countriesOfRouting

import models.NormalMode
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.RemoveDepartureMeansOfTransportYesNoView
import views.html.countriesOfRouting.RemoveCountryYesNoView

class RemoveCountryYesNoViewSpec extends YesNoViewBehaviours {

  private val insetText = Gen.alphaNumStr.sample.value

  private val mode = NormalMode

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveCountryYesNoView]
      .apply(form, mrn, arrivalId, index, mode, Some(insetText))(fakeRequest, messages)

  override val prefix: String = "countriesOfRouting.removeCountryYesNo"

  behave like pageWithTitle(index.display)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: $mrn")

  behave like pageWithHeading(index.display)

  behave like pageWithRadioItems(args = Seq(index.display))

  behave like pageWithInsetText(insetText)

  behave like pageWithSubmitButton("Continue")

  "when inset text undefined" - {
    val view = injector
      .instanceOf[RemoveDepartureMeansOfTransportYesNoView]
      .apply(form, mrn, arrivalId, index, mode, None)(fakeRequest, messages)
    val doc = parseView(view)

    behave like pageWithoutInsetText(doc)
  }
}
