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

package viewModels.houseConsignment.index.departureTransportMeans

import config.FrontendAppConfig
import controllers.houseConsignment.index.departureMeansOfTransport.routes
import models.removable.TransportMeans
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.sections.houseConsignment.index.departureTransportMeans.TransportMeansListSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherDepartureMeansOfTransportViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index,
  houseConsignmentIndex: Index
) extends AddAnotherViewModel {
  override val prefix: String = "houseConsignment.index.departureMeansOfTransport.addAnotherDepartureMeansOfTransport"

  override def title(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.title", count, houseConsignmentIndex.display)

  override def heading(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.heading", count, houseConsignmentIndex.display)

  override def legend(implicit messages: Messages): String = if (count > 0) {
    messages(s"$prefix.label", count, houseConsignmentIndex.display)
  } else { messages(s"$prefix.empty.label", count, houseConsignmentIndex.display) }

  override def maxLimitLabel(implicit messages: Messages): String =
    messages(s"$prefix.maxLimit.label", count, houseConsignmentIndex.display)

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxDepartureMeansOfTransport

}

object AddAnotherDepartureMeansOfTransportViewModel {

  class AddAnotherDepartureMeansOfTransportViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode)(implicit
      messages: Messages
    ): AddAnotherDepartureMeansOfTransportViewModel = {

      val array = userAnswers.get(TransportMeansListSection(houseConsignmentIndex))

      val listItems = array.flatMapWithIndex {
        case (_, transportMeansIndex) =>
          TransportMeans(userAnswers, houseConsignmentIndex, transportMeansIndex).map {
            transportMeans =>
              ListItem(
                name = transportMeans.forAddAnotherDisplay,
                changeUrl = None,
                removeUrl =
                  Some(routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(arrivalId, houseConsignmentIndex, transportMeansIndex, mode).url)
              )
          }
      }

      new AddAnotherDepartureMeansOfTransportViewModel(
        listItems,
        onSubmitCall = routes.AddAnotherDepartureMeansOfTransportController.onSubmit(arrivalId, houseConsignmentIndex, mode),
        nextIndex = array.nextIndex,
        houseConsignmentIndex
      )
    }
  }
}
