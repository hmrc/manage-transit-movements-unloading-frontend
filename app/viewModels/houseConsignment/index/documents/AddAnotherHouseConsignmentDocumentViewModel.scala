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

package viewModels.houseConsignment.index.documents

import config.FrontendAppConfig
import controllers.houseConsignment.index.documents.routes
import models.DocType.Previous
import models.removable.Document
import models.{ArrivalId, HouseConsignmentLevelDocuments, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.houseConsignment.index.documents.TypePage
import pages.sections.houseConsignment.index.documents.DocumentsSection
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.documents.Documents
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherHouseConsignmentDocumentViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index,
  documents: HouseConsignmentLevelDocuments,
  allowMore: Boolean
) extends AddAnotherViewModel {

  override val prefix: String = "houseConsignment.index.document.addAnotherDocument"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxSupportingDocumentsHouseConsignment + config.maxTransportDocumentsHouseConsignment

  def maxLimitLabel(houseConsignmentIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.maxLimit.label", houseConsignmentIndex)

  def title(houseConsignmentIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.title", count, houseConsignmentIndex)

  def heading(houseConsignmentIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.heading", count, houseConsignmentIndex)

  def legend(houseConsignmentIndex: Index)(implicit messages: Messages): String = count match {
    case 0 => messages(s"$prefix.singular.label", houseConsignmentIndex)
    case _ => messages(s"$prefix.plural.label", houseConsignmentIndex)
  }

  def maxLimitLabelForType(houseConsignmentIndex: Index)(implicit config: FrontendAppConfig, messages: Messages): Option[String] =
    Documents.maxLimitLabelForType(documents, houseConsignmentIndex, prefix)
}

object AddAnotherHouseConsignmentDocumentViewModel {

  class AddAnotherHouseConsignmentDocumentViewModelProvider {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, houseConsignmentIndex: Index, mode: Mode)(implicit
      config: FrontendAppConfig
    ): AddAnotherHouseConsignmentDocumentViewModel = {

      val documents: Option[JsArray] = userAnswers.get(DocumentsSection(houseConsignmentIndex))

      val listItems: Seq[ListItem] = documents.mapWithIndex {
        case (_, index) =>
          userAnswers.get(TypePage(houseConsignmentIndex, index)).map(_.`type`).flatMap {
            case Previous =>
              None
            case _ =>
              Document(userAnswers, houseConsignmentIndex, index).map {
                document =>
                  ListItem(
                    name = document.forAddAnotherDisplay,
                    changeUrl = None,
                    removeUrl = Some("#") //TODO add the remove page here
                  )
              }
          }
      }.flatten

      val houseConsignmentLevelDocuments = HouseConsignmentLevelDocuments.apply(userAnswers, houseConsignmentIndex, None)

      new AddAnotherHouseConsignmentDocumentViewModel(
        listItems = listItems,
        onSubmitCall = routes.AddAnotherDocumentController.onSubmit(arrivalId, houseConsignmentIndex, mode),
        nextIndex = documents.nextIndex,
        documents = houseConsignmentLevelDocuments,
        allowMore = houseConsignmentLevelDocuments.canAddMore
      )
    }
  }
}
