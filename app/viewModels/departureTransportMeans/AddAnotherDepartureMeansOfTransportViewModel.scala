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
import models.{ArrivalId, Index, Mode, UserAnswers}
import pages.departureMeansOfTransport.{TransportMeansIdentificationPage, VehicleIdentificationNumberPage}
import pages.sections.TransportMeansListSection
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import play.api.mvc.Results.Redirect
import viewModels.{AddAnotherViewModel, ListItem}
import play.api.mvc._

case class AddAnotherDepartureMeansOfTransportViewModel(userAnswers: UserAnswers, listItems: Seq[ListItem], onSubmitCall: Call) extends AddAnotherViewModel {
  override val prefix: String = "consignment.departureTransportMeans.addAnotherTransportMeans"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxDepartureMeansOfTransport

  override def maxLimitLabel(implicit messages: Messages): String = messages(s"$prefix.maxLimit.label")

  def paragraphContent(implicit messages: Messages): String =
    if (count > 0) messages(s"$prefix.addAnother") else messages(s"$prefix.add")

  def maxLimitWarningHint1(implicit messages: Messages): String = messages(s"$prefix.maxLimit.hint1")

  def maxLimitWarningHint2(implicit messages: Messages): String = messages(s"$prefix.maxLimit.hint2")

}

object AddAnotherDepartureMeansOfTransportViewModel {

  class AddAnotherDepartureMeansOfTransportViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, mode: Mode)(implicit messages: Messages): AddAnotherDepartureMeansOfTransportViewModel = {

      val listItems = userAnswers
        .get(TransportMeansListSection)
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, i) =>
            val index = Index(i)

            def departureMeansOfTransportPrefix(increment: Int) = messages("departureMeansOfTransportPrefix.prefix", increment)

            val name = for {
              identification       <- userAnswers.get(TransportMeansIdentificationPage(index))
              identificationNumber <- userAnswers.get(VehicleIdentificationNumberPage(index))
            } yield s"${departureMeansOfTransportPrefix(index.display)} - $identification - $identificationNumber"

            name.map {
              name =>
                ListItem(
                  name = name,
                  changeUrl = "",
                  removeUrl = None
                )
            }
        }
        .toSeq

      new AddAnotherDepartureMeansOfTransportViewModel(
        userAnswers,
        listItems,
        onSubmitCall = Call("GET", "#")
      )
    }
  }
}
