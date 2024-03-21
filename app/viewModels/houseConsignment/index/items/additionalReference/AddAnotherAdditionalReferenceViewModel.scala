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

package viewModels.houseConsignment.index.items.additionalReference

import config.FrontendAppConfig
import controllers.houseConsignment.index.items.additionalReference.routes
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.sections.additionalReference.AdditionalReferencesSection
import play.api.libs.json.JsArray
import play.api.mvc.Call
import sttp.model.Method.GET
import viewModels.{AddAnotherViewModel, ListItem, ModeViewModelProvider}

import javax.inject.Inject

case class AddAnotherAdditionalReferenceViewModel(listItems: Seq[ListItem], onSubmitCall: Call, nextIndex: Index) extends AddAnotherViewModel {
  override val prefix: String                                    = "houseConsignment.index.items.additionalReference.addAnotherAdditionalReference"
  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxAdditionalReferences

}

object AddAnotherAdditionalReferenceViewModel {

  class AddAnotherAdditionalReferenceViewModelProvider {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, mode: Mode): AddAnotherAdditionalReferenceViewModel = {

      val array = userAnswers.get(AdditionalReferencesSection)

      val listItems = array
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, i) =>
            val additionalReferenceIndex = Index(i)
            val number                   = userAnswers.get(AdditionalReferenceNumberPage(additionalReferenceIndex)).getOrElse("")
            val numberString             = if (number.nonEmpty) s"- $number" else ""
            userAnswers.get(AdditionalReferenceTypePage(additionalReferenceIndex)).map {
              `type` =>
                ListItem(
                  name = s"${`type`.value} $numberString",
                  changeUrl = None,
                  // TODO: Update once remove controller ready
                  removeUrl = Some(Call("GET", "#").url)
                )
            }
        }
        .toSeq

      new AddAnotherAdditionalReferenceViewModel(
        listItems,
        onSubmitCall = controllers.additionalReference.index.routes.AddAnotherAdditionalReferenceController.onSubmit(arrivalId, mode),
        nextIndex = array.nextIndex
      )
    }

  }
}
