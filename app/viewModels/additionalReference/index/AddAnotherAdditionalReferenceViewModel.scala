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

package viewModels.additionalReference.index

import config.FrontendAppConfig
import controllers.additionalReference.index.routes
import models.removable.AdditionalReference
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.sections.additionalReference.AdditionalReferencesSection
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherAdditionalReferenceViewModel(listItems: Seq[ListItem], onSubmitCall: Call, nextIndex: Index) extends AddAnotherViewModel {
  override val prefix: String = "additionalReference.index.addAnotherAdditionalReference"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxAdditionalReferences

}

object AddAnotherAdditionalReferenceViewModel {

  class AddAnotherAdditionalReferenceViewModelProvider() {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, mode: Mode): AddAnotherAdditionalReferenceViewModel = {
      val array = userAnswers.get(AdditionalReferencesSection)

      val listItems = array
        .flatMapWithIndex {
          case (_, additionalReferenceIndex) =>
            AdditionalReference(userAnswers, additionalReferenceIndex).map {
              additionalReference =>
                ListItem(
                  name = additionalReference.forAddAnotherDisplay,
                  changeUrl = None,
                  removeUrl = Some(routes.RemoveAdditionalReferenceYesNoController.onPageLoad(arrivalId, additionalReferenceIndex, mode).url)
                )
            }
        }

      new AddAnotherAdditionalReferenceViewModel(
        listItems,
        onSubmitCall = routes.AddAnotherAdditionalReferenceController.onSubmit(arrivalId, mode),
        nextIndex = array.nextIndex
      )
    }
  }
}
