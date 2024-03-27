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

import forms.AddAnotherFormProvider
import models.Index
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.items.packages.AddAnotherPackageViewModel
import views.behaviours.ListWithActionsViewBehaviours
import views.html.houseConsignment.index.items.packages.AddAnotherPackageView

class AddAnotherPackageViewSpec extends ListWithActionsViewBehaviours {

  override def maxNumber: Int = frontendAppConfig.maxPackages

  private def formProvider(viewModel: AddAnotherPackageViewModel, houseConsignmentIndex: Index, itemIndex: Index) =
    new AddAnotherFormProvider()(viewModel.prefix, viewModel.allowMore, itemIndex, houseConsignmentIndex)

  private val viewModel            = arbitrary[AddAnotherPackageViewModel].sample.value
  private val noItemsViewModel     = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  override def form: Form[Boolean] = formProvider(notMaxedOutViewModel, houseConsignmentIndex, itemIndex)

  def applyNoItemsView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherPackageView]
      .apply(formProvider(noItemsViewModel, houseConsignmentIndex, itemIndex), mrn, arrivalId, houseConsignmentIndex, itemIndex, noItemsViewModel)(
        fakeRequest,
        messages,
        frontendAppConfig
      )

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherPackageView]
      .apply(form, mrn, arrivalId, houseConsignmentIndex, itemIndex, notMaxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyMaxedOutView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherPackageView]
      .apply(formProvider(maxedOutViewModel, houseConsignmentIndex, itemIndex), mrn, arrivalId, houseConsignmentIndex, itemIndex, maxedOutViewModel)(
        fakeRequest,
        messages,
        frontendAppConfig
      )

  override val prefix: String = "houseConsignment.index.items.packages.addAnotherPackage"

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithMoreItemsAllowed(notMaxedOutViewModel.count, itemIndex.display, houseConsignmentIndex.display)(notMaxedOutViewModel.count,
                                                                                                                     itemIndex,
                                                                                                                     houseConsignmentIndex.display
  )

  behave like pageWithItemsMaxedOut(maxedOutViewModel.count, itemIndex.display, houseConsignmentIndex.display)(maxedOutViewModel.count,
                                                                                                               itemIndex.display,
                                                                                                               houseConsignmentIndex.display
  )

  behave like pageWithSubmitButton("Continue")

  "page with no items" - {

    val doc = parseView(applyNoItemsView)

    behave like pageWithTitle(doc, s"$prefix.empty", noItemsViewModel.count, itemIndex.display, houseConsignmentIndex.display)

    behave like pageWithHeading(doc, s"$prefix.empty", noItemsViewModel.count, itemIndex.display, houseConsignmentIndex.display)

    behave like pageWithRadioItems(document = doc,
                                   legendIsHeading = false,
                                   args = Seq(noItemsViewModel.count, itemIndex.display, houseConsignmentIndex.display)
    )
  }
}
