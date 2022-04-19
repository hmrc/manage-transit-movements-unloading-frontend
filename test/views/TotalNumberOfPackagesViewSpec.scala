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

import forms.TotalNumberOfPackagesFormProvider
import models.NormalMode
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.TotalNumberOfPackagesView

class TotalNumberOfPackagesViewSpec extends InputTextViewBehaviours[Int] {

  override def form: Form[Int] = new TotalNumberOfPackagesFormProvider()()

  override def applyView(form: Form[Int]): HtmlFormat.Appendable =
    injector.instanceOf[TotalNumberOfPackagesView].apply(form, arrivalId, mrn, NormalMode)(fakeRequest, messages)

  override val prefix: String = "totalNumberOfPackages"

  implicit override val arbitraryT: Arbitrary[Int] = Arbitrary(Gen.oneOf(1 to 100))

  behave like pageWithBackLink

  behave like pageWithHeading()

  behave like pageWithCaption(mrn.toString)

  behave like pageWithoutHint

  behave like pageWithInputText(Some(InputSize.Width10), Some("numeric"), Some("[0-9]*"))

  behave like pageWithSubmitButton("Continue")
}
