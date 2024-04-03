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

package viewModels.houseConsignment.index.items.document

import config.FrontendAppConfig
import models.DocType.{Previous, Support, Transport}
import models.{ArrivalId, DocType, HouseConsignmentLevelDocuments, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.houseConsignment.index.items.document.{DocumentReferenceNumberPage, TypePage}
import pages.sections.houseConsignment.index.items.documents.DocumentsSection
import play.api.i18n.Messages
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherHouseConsignmentDocumentViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index,
  docTypeList: Seq[DocType],
  allowMore: Boolean
) extends AddAnotherViewModel {

  override val prefix: String = "houseConsignment.index.items.document.addAnotherDocument"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxSupportingDocumentsHouseConsignment + config.maxTransportDocumentsHouseConsignment

  def maxLimitLabel(itemIndex: Index, houseConsignmentIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.maxLimit.label", houseConsignmentIndex, itemIndex)

  def title(itemIndex: Index, houseConsignmentIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.title", count, itemIndex, houseConsignmentIndex)

  def heading(itemIndex: Index, houseConsignmentIndex: Index)(implicit messages: Messages): String =
    messages(s"$prefix.$emptyOrSingularOrPlural.heading", count, itemIndex, houseConsignmentIndex)

  def legend(itemIndex: Index, houseConsignmentIndex: Index)(implicit messages: Messages): String = count match {
    case 0 => messages(s"$prefix.singular.label", itemIndex, houseConsignmentIndex)
    case _ => messages(s"$prefix.plural.label", itemIndex, houseConsignmentIndex)
  }

  def maxLimitLabelForType(itemIndex: Index, houseConsignmentIndex: Index)(implicit config: FrontendAppConfig, messages: Messages): Option[String] = {
    val groupedByType = docTypeList.groupBy(identity)
    if (groupedByType.getOrElse(Support, Seq.empty).length == config.maxSupportingDocumentsHouseConsignment) {
      Some(messages(s"$prefix.maxLimitForType.label", Support.display.toLowerCase, Transport.display.toLowerCase, houseConsignmentIndex, itemIndex))
    } else if (groupedByType.getOrElse(Transport, Seq.empty).length == config.maxTransportDocumentsHouseConsignment) {
      Some(messages(s"$prefix.maxLimitForType.label", Transport.display.toLowerCase, Support.display.toLowerCase, houseConsignmentIndex, itemIndex))
    } else {
      None
    }
  }
}

object AddAnotherHouseConsignmentDocumentViewModel {

  class AddAnotherHouseConsignmentDocumentViewModelProvider {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, houseConsignmentIndex: Index, itemsIndex: Index, mode: Mode)(implicit
      config: FrontendAppConfig
    ): AddAnotherHouseConsignmentDocumentViewModel = {

      val documents: Option[JsArray] = userAnswers.get(DocumentsSection(houseConsignmentIndex, itemsIndex))

      val listItems = documents.mapWithIndex {
        case (_, index) =>
          userAnswers.get(TypePage(houseConsignmentIndex, itemsIndex, index)) match {
            case Some(docType) if docType.`type` != Previous =>
              Some(
                ListItem(
                  name = s"${userAnswers
                    .get(DocumentReferenceNumberPage(houseConsignmentIndex, itemsIndex, index))
                    .map(
                      refNo => s"$docType - $refNo"
                    )
                    .getOrElse(s"$docType")}",
                  changeUrl = None,
                  removeUrl = Some(
                    controllers.houseConsignment.index.items.document.routes.RemoveDocumentYesNoController
                      .onPageLoad(arrivalId, mode, houseConsignmentIndex, itemsIndex, index)
                      .url
                  )
                )
              )
            case _ => None
          }
      }.flatten

      val docTypeList = documents
        .mapWithIndex {
          case (_, index) =>
            userAnswers.get(TypePage(houseConsignmentIndex, itemsIndex, index)).map(_.`type`)
        }
        .flatten
        .filter(_ != Previous)

      val houseConsignmentLevelDocuments = HouseConsignmentLevelDocuments(userAnswers, houseConsignmentIndex, itemsIndex)

      new AddAnotherHouseConsignmentDocumentViewModel(
        listItems,
        onSubmitCall =
          controllers.houseConsignment.index.items.document.routes.AddAnotherDocumentController.onSubmit(arrivalId, houseConsignmentIndex, itemsIndex, mode),
        nextIndex = documents.nextIndex,
        docTypeList,
        allowMore = houseConsignmentLevelDocuments.canAddMore
      )
    }
  }
}
