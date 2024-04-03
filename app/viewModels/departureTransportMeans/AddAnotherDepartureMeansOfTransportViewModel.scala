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

package viewModels.departureTransportMeans

import config.FrontendAppConfig
import models.removable.TransportMeans
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.sections.TransportMeansListSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherDepartureMeansOfTransportViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index
) extends AddAnotherViewModel {
  override val prefix: String = "departureMeansOfTransport.addAnotherDepartureMeansOfTransport"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxDepartureMeansOfTransport

  override def maxLimitLabel(implicit messages: Messages): String = messages(s"$prefix.maxLimit.label")
}

object AddAnotherDepartureMeansOfTransportViewModel {

  class AddAnotherDepartureMeansOfTransportViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, mode: Mode)(implicit messages: Messages): AddAnotherDepartureMeansOfTransportViewModel = {

      val array = userAnswers.get(TransportMeansListSection)

      val listItems = array.mapWithIndex {
        case (_, index) =>
          ListItem(
            name = TransportMeans(userAnswers, index).forAddAnotherDisplay,
            changeUrl = None,
            removeUrl = Some(controllers.departureMeansOfTransport.routes.RemoveDepartureMeansOfTransportYesNoController.onPageLoad(arrivalId, mode, index).url)
          )
      }

      new AddAnotherDepartureMeansOfTransportViewModel(
        listItems,
        onSubmitCall = controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onSubmit(arrivalId, mode),
        nextIndex = array.nextIndex
      )
    }
  }
}
