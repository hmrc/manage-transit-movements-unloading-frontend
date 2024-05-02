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

package views.houseConsignment.index.documents

import forms.AddAnotherFormProvider
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.documents.AddAnotherHouseConsignmentDocumentViewModel
import views.behaviours.ListWithActionsViewBehaviours
import views.html.houseConsignment.index.documents.AddAnotherDocumentView

class AddAnotherDocumentViewSpec extends ListWithActionsViewBehaviours {

  override def maxNumber: Int = frontendAppConfig.maxTransportDocumentsHouseConsignment + frontendAppConfig.maxSupportingDocumentsHouseConsignment

  private def formProvider(viewModel: AddAnotherHouseConsignmentDocumentViewModel) =
    new AddAnotherFormProvider()(viewModel.prefix, viewModel.allowMore)

  private val viewModel            = arbitrary[AddAnotherHouseConsignmentDocumentViewModel].sample.value
  private val noItemsViewModel     = viewModel.copy(listItems = Nil, allowMore = true)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems, allowMore = true)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems, allowMore = false)

  override def form: Form[Boolean] = formProvider(notMaxedOutViewModel)

  def applyNoItemsView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherDocumentView]
      .apply(formProvider(noItemsViewModel), mrn, arrivalId, houseConsignmentIndex, noItemsViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherDocumentView]
      .apply(form, mrn, arrivalId, houseConsignmentIndex, notMaxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyMaxedOutView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherDocumentView]
      .apply(formProvider(maxedOutViewModel), mrn, arrivalId, houseConsignmentIndex, maxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override val prefix: String = "houseConsignment.index.document.addAnotherDocument"

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithMoreItemsAllowed(notMaxedOutViewModel.count, houseConsignmentIndex.display, itemIndex.display)(houseConsignmentIndex.display,
                                                                                                                     itemIndex.display
  )

  behave like pageWithItemsMaxedOut(maxedOutViewModel.count, houseConsignmentIndex.display, itemIndex.display)(houseConsignmentIndex.display, itemIndex.display)

  behave like pageWithSubmitButton("Continue")

}
