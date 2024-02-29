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
import models.{ArrivalId, Mode, UserAnswers}
import pages.departureMeansOfTransport.{TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import pages.sections.TransportMeansListSection
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.answersHelpers.RichOptionalJsArray
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherDepartureMeansOfTransportViewModel(listItems: Seq[ListItem], onSubmitCall: Call) extends AddAnotherViewModel {
  override val prefix: String = "departureMeansOfTransport.addAnotherDepartureMeansOfTransport"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxDepartureMeansOfTransport

  override def maxLimitLabel(implicit messages: Messages): String = messages(s"$prefix.maxLimit.label")
}

object AddAnotherDepartureMeansOfTransportViewModel {

  class AddAnotherDepartureMeansOfTransportViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, mode: Mode)(implicit messages: Messages): AddAnotherDepartureMeansOfTransportViewModel = {

      val listItems = userAnswers
        .get(TransportMeansListSection)
        .mapWithIndex {
          case (_, index, displayIndex) =>
            def prefix(increment: Int) = messages("departureMeansOfTransportPrefix.prefix", increment)

            val name =
              (userAnswers.get(TransportMeansIdentificationPage(index)), userAnswers.get(VehicleIdentificationNumberPage(index))) match {
                case (Some(identification), Some(identificationNumber)) =>
                  s"${prefix(displayIndex.display)} - $identification - $identificationNumber"
                case (Some(identification), None) =>
                  s"${prefix(displayIndex.display)} - $identification"
                case (None, Some(identificationNumber)) =>
                  s"${prefix(displayIndex.display)} - $identificationNumber"
                case _ =>
                  s"${prefix(displayIndex.display)}"
              }

            ListItem(
              name = name,
              changeUrl = Some(Call("GET", "#").url), //TODO: To be added later
              removeUrl = Some(Call("GET", "#").url) //TODO: To be added later
            )
        }

      new AddAnotherDepartureMeansOfTransportViewModel(
        listItems,
        onSubmitCall = controllers.departureMeansOfTransport.routes.AddAnotherDepartureMeansOfTransportController.onSubmit(arrivalId, mode)
      )
    }
  }
}
