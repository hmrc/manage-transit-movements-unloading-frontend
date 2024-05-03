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

package views.houseConsignment.index.departureMeansOfTransport

import forms.AddAnotherFormProvider
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.houseConsignment.index.departureTransportMeans.AddAnotherDepartureMeansOfTransportViewModel
import views.behaviours.ListWithActionsViewBehaviours
import views.html.houseConsignment.index.departureMeansOfTransport.AddAnotherDepartureMeansOfTransportView

class AddAnotherDepartureMeansOfTransportViewSpec extends ListWithActionsViewBehaviours {

  override def maxNumber: Int = frontendAppConfig.maxDepartureMeansOfTransport

  private def formProvider(viewModel: AddAnotherDepartureMeansOfTransportViewModel) =
    new AddAnotherFormProvider()(viewModel.prefix, viewModel.allowMore)

  private val viewModel            = arbitrary[AddAnotherDepartureMeansOfTransportViewModel].sample.value
  private val noItemsViewModel     = viewModel.copy(listItems = Nil)
  private val notMaxedOutViewModel = viewModel.copy(listItems = listItems)
  private val maxedOutViewModel    = viewModel.copy(listItems = maxedOutListItems)

  override def form: Form[Boolean] = formProvider(notMaxedOutViewModel)

  def applyNoItemsView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherDepartureMeansOfTransportView]
      .apply(formProvider(noItemsViewModel), mrn, arrivalId, noItemsViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherDepartureMeansOfTransportView]
      .apply(form, mrn, arrivalId, notMaxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override def applyMaxedOutView: HtmlFormat.Appendable =
    injector
      .instanceOf[AddAnotherDepartureMeansOfTransportView]
      .apply(formProvider(maxedOutViewModel), mrn, arrivalId, maxedOutViewModel)(fakeRequest, messages, frontendAppConfig)

  override val prefix: String = "houseConsignment.index.departureMeansOfTransport.addAnotherDepartureMeansOfTransport"

  behave like pageWithBackLink()

  behave like pageWithCaption(s"This notification is MRN: ${mrn.toString}")

  behave like pageWithMoreItemsAllowed(notMaxedOutViewModel.count, houseConsignmentIndex)(notMaxedOutViewModel.count, houseConsignmentIndex)

  behave like pageWithItemsMaxedOut(maxedOutViewModel.count, houseConsignmentIndex)(maxedOutViewModel.count, houseConsignmentIndex)

  behave like pageWithSubmitButton("Continue")

  "page with no items" - {

    val doc = parseView(applyNoItemsView)

    behave like pageWithTitle(doc, s"$prefix.empty", noItemsViewModel.count, houseConsignmentIndex)

    behave like pageWithHeading(doc, s"$prefix.empty", noItemsViewModel.count, houseConsignmentIndex)

    behave like pageWithRadioItems(document = doc, legendIsHeading = false, args = Seq(noItemsViewModel.count, houseConsignmentIndex))
  }
}
