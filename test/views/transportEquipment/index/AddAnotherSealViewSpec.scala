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

package views.transportEquipment.index

import forms.AddAnotherFormProvider
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.transportEquipment.index.AddAnotherSealViewModel
import views.behaviours.ListWithActionsViewBehaviours
import views.html.transportEquipment.index.AddAnotherSealView

class AddAnotherSealViewSpec extends ListWithActionsViewBehaviours {

  override def maxNumber: Int = frontendAppConfig.maxSeals

  private def formProvider(viewModel: AddAnotherSealViewModel) =
    new AddAnotherFormProvider()(viewModel.prefix, viewModel.allowMore)

  private val viewModel            = arbitrary[AddAnotherSealViewModel].sample.value
  private val noSealViewModel      = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  override def form: Form[Boolean] = formProvider(notMaxedOutViewModel)

  def applyNoSealView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherSealView]
      .apply(formProvider(noSealViewModel), mrn, arrivalId, noSealViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherSealView]
      .apply(form, mrn, arrivalId, notMaxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyMaxedOutView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherSealView]
      .apply(formProvider(maxedOutViewModel), mrn, arrivalId, maxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override val prefix: String = "transportEquipment.index.addAnotherSeal"

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithMoreItemsAllowed(notMaxedOutViewModel.count, equipmentIndex.display)(notMaxedOutViewModel.count, equipmentIndex.display)

  behave like pageWithItemsMaxedOut(maxedOutViewModel.count, equipmentIndex.display)(equipmentIndex.display)

  behave like pageWithSubmitButton("Continue")

  "page with no items" - {

    val doc = parseView(applyNoSealView)

    behave like pageWithTitle(doc, s"$prefix.empty", noSealViewModel.count, equipmentIndex.display)

    behave like pageWithHeading(doc, s"$prefix.empty", noSealViewModel.count, equipmentIndex.display)

    behave like pageWithRadioItems(document = doc, legendIsHeading = false, args = Seq(noSealViewModel.count, equipmentIndex.display))

  }
}
