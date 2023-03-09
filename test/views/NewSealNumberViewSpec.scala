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

import forms.NewSealNumberFormProvider
import models.{Index, NormalMode}
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.InputTextViewBehaviours
import views.html.NewSealNumberView

class NewSealNumberViewSpec extends InputTextViewBehaviours[String] {

  override def form: Form[String] = new NewSealNumberFormProvider()()
  private val index: Index        = Index(0)

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[NewSealNumberView].apply(form, mrn, arrivalId, index, NormalMode)(fakeRequest, messages)

  override val prefix: String = "newSealNumber"

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaStr)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithCaption(mrn.toString)

  behave like pageWithHint("This can be up to 20 characters long and include both letters and numbers.")

  behave like pageWithInputText()

  behave like pageWithSubmitButton("Continue")
}
