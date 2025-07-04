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

package viewModels.countriesOfRouting

import config.FrontendAppConfig
import controllers.countriesOfRouting.routes
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.countriesOfRouting.CountryOfRoutingPage
import pages.sections.CountriesOfRoutingSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherCountryViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index
) extends AddAnotherViewModel {
  override val prefix: String = "countriesOfRouting.addAnotherCountry"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxCountriesOfRouting
}

object AddAnotherCountryViewModel {

  class AddAnotherCountryViewModelProvider {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, mode: Mode)(implicit messages: Messages): AddAnotherCountryViewModel = {

      val array = userAnswers.get(CountriesOfRoutingSection)

      val listItems = array
        .flatMapWithIndex {
          case (_, index) =>
            userAnswers.get(CountryOfRoutingPage(index)).map {
              country =>
                ListItem(
                  name = country.toString,
                  changeUrl = None,
                  removeUrl = Some(routes.RemoveCountryYesNoController.onPageLoad(arrivalId, mode, index).url)
                )
            }
        }

      new AddAnotherCountryViewModel(
        listItems,
        onSubmitCall = routes.AddAnotherCountryController.onSubmit(arrivalId, mode),
        nextIndex = array.nextIndex
      )
    }
  }
}
