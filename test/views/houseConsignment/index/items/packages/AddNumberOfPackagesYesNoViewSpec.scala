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

package views.houseConsignment.index.items.packages

import models.NormalMode
import models.reference.PackageType
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.houseConsignment.index.items.packages.AddNumberOfPackagesYesNoView

class AddNumberOfPackagesYesNoViewSpec extends YesNoViewBehaviours {

  private val packageType = PackageType("A1", "Steel, drum")

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[AddNumberOfPackagesYesNoView]
      .apply(form, mrn, arrivalId, houseConsignmentIndex, itemIndex, packageIndex, packageType.toString, NormalMode)(fakeRequest, messages)

  override val prefix: String = "houseConsignment.index.items.packages.addNumberOfPackagesYesNo"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithHeading()

  behave like pageWithInsetText(packageType.toString)

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Continue")
}
