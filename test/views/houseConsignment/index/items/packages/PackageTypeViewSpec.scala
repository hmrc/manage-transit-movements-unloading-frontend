/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.SelectableFormProvider.PackageTypeFormProvider
import models.reference.PackageType
import models.{CheckMode, NormalMode, SelectableList}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.items.packages.PackageTypeViewModel
import views.behaviours.InputSelectViewBehaviours
import views.html.houseConsignment.index.items.packages.PackageTypeView

class PackageTypeViewSpec extends InputSelectViewBehaviours[PackageType] {

  private val viewModel: PackageTypeViewModel =
    arbitrary[PackageTypeViewModel].sample.value

  private val formProvider = new PackageTypeFormProvider()

  override val field: String = formProvider.field

  override def form: Form[PackageType] = formProvider.apply(NormalMode, prefix, SelectableList(values))

  override def applyView(form: Form[PackageType]): HtmlFormat.Appendable =
    injector
      .instanceOf[PackageTypeView]
      .apply(viewModel, form, mrn, arrivalId, values, NormalMode, NormalMode, NormalMode, houseConsignmentIndex, itemIndex, packageIndex)(fakeRequest, messages)

  implicit override val arbitraryT: Arbitrary[PackageType] = arbitraryPackageType

  override val prefix: String = Gen
    .oneOf(
      "houseConsignment.index.item.packageType",
      "houseConsignment.index.item.packageType.check"
    )
    .sample
    .value

  private val paragraph = "This means the packaging used to store and protect the item during transit."

  behave like pageWithTitle(text = viewModel.title)

  behave like pageWithBackLink()

  behave like pageWithHeading(text = viewModel.heading)

  behave like pageWithHint("Enter the package or code, like cylinder or CY.")

  behave like pageWithSelect()

  behave like pageWithSubmitButton("Continue")

  "when package in NormalMode" - {
    val doc =
      parseView(applyView(form))
    behave like pageWithoutContent(doc, "p", paragraph)
  }

  "when package in CheckMode" - {
    val view = injector.instanceOf[PackageTypeView]
    val doc =
      parseView(
        view
          .apply(viewModel, form, mrn, arrivalId, values, CheckMode, CheckMode, CheckMode, houseConsignmentIndex, itemIndex, packageIndex)(fakeRequest,
                                                                                                                                           messages
          )
      )
    behave like pageWithContent(doc, "p", paragraph)
  }
}
