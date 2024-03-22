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
import viewModels.transportEquipment.index.ApplyAnotherItemViewModel
import views.behaviours.ListWithActionsViewBehaviours
import views.html.transportEquipment.index.ApplyAnotherItemView

class ApplyAnotherItemViewSpec extends ListWithActionsViewBehaviours {

  override def maxNumber: Int = frontendAppConfig.maxItems

  private def formProvider(viewModel: ApplyAnotherItemViewModel) =
    new AddAnotherFormProvider()(viewModel.prefix, viewModel.allowMore)

  private val viewModel            = arbitrary[ApplyAnotherItemViewModel].sample.value
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)
  private val noMoreItemsViewModel = viewModel.copy(listItems = Nil, isNumberItemsZero = true)

  override def form: Form[Boolean] = formProvider(notMaxedOutViewModel)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[ApplyAnotherItemView]
      .apply(form, mrn, arrivalId, notMaxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyMaxedOutView: HtmlFormat.Appendable =
    injector
      .instanceOf[ApplyAnotherItemView]
      .apply(formProvider(maxedOutViewModel), mrn, arrivalId, maxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  def applyNoMoreItemsView: HtmlFormat.Appendable = injector
    .instanceOf[ApplyAnotherItemView]
    .apply(formProvider(noMoreItemsViewModel), mrn, arrivalId, noMoreItemsViewModel)(fakeRequest, messages, frontendAppConfig)

  override val prefix: String = "transport.equipment.applyAnotherItem"

  behave like pageWithBackLink()

  behave like pageWithMoreItemsAllowed(notMaxedOutViewModel.count)(equipmentIndex.display)

  behave like pageWithItemsMaxedOut(maxedOutViewModel.count, equipmentIndex.display)()

  behave like pageWithSubmitButton("Continue")

  "page with no more items" - {

    val doc = parseView(applyNoMoreItemsView)
    behave like pageWithContent(doc, "p", "There are no items left to apply to this transport equipment.")

  }

}
