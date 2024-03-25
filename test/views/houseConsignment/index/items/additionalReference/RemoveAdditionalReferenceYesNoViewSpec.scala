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

package views.houseConsignment.index.items.additionalReference

import models.{Mode, NormalMode}
import org.scalacheck.Gen
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.houseConsignment.index.items.additionalReference.RemoveAdditionalReferenceYesNoView

class RemoveAdditionalReferenceYesNoViewSpec extends YesNoViewBehaviours {
  private val insetText = Gen.alphaNumStr.sample.value

  val mode: Mode = NormalMode

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[RemoveAdditionalReferenceYesNoView]
      .apply(form, mrn, arrivalId, mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex, Some(insetText))(fakeRequest, messages)

  override val prefix: String = "houseConsignment.index.items.additionalReference.removeAdditionalReferenceYesNo"

  behave like pageWithTitle(additionalReferenceIndex.display)

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: $mrn")

  behave like pageWithHeading(additionalReferenceIndex.display)

  behave like pageWithRadioItems(args = Seq(additionalReferenceIndex.display))

  behave like pageWithInsetText(insetText)

  behave like pageWithSubmitButton("Continue")

  "when inset text undefined" - {
    val view = injector
      .instanceOf[RemoveAdditionalReferenceYesNoView]
      .apply(form, mrn, arrivalId, mode, houseConsignmentIndex, itemIndex, additionalReferenceIndex, None)(fakeRequest, messages)
    val doc = parseView(view)

    behave like pageWithoutInsetText(doc)
  }
}
