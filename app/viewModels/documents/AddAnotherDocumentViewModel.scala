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

package viewModels.documents

import config.FrontendAppConfig
import models.DocType.{Previous, Support, Transport}
import models.{ArrivalId, ConsignmentLevelDocuments, DocType, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.documents.{DocumentReferenceNumberPage, TypePage}
import pages.sections.documents.DocumentsSection
import play.api.i18n.Messages
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherDocumentViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index,
  docTypeList: Seq[DocType],
  allowMore: Boolean
) extends AddAnotherViewModel {

  override val prefix: String = "document.addAnotherDocument"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxSupportingDocuments + config.maxSupportingDocuments

  override def maxLimitLabel(implicit messages: Messages): String = messages(s"$prefix.maxLimit.label")

  def maxLimitLabelForType(implicit config: FrontendAppConfig, messages: Messages): Option[String] = {
    val groupedByType = docTypeList.groupBy(identity)
    if (groupedByType.getOrElse(Support, Seq.empty).length == config.maxSupportingDocuments) {
      Some(messages(s"$prefix.maxLimitForType.label", Support.display.toLowerCase, Transport.display.toLowerCase))
    } else if (groupedByType.getOrElse(Transport, Seq.empty).length == config.maxTransportDocuments) {
      Some(messages(s"$prefix.maxLimitForType.label", Transport.display.toLowerCase, Support.display.toLowerCase))
    } else {
      None
    }
  }
}

object AddAnotherDocumentViewModel {

  class AddAnotherDocumentViewModelProvider {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, mode: Mode)(implicit config: FrontendAppConfig): AddAnotherDocumentViewModel = {

      val documents = userAnswers.get(DocumentsSection)

      val listItems = documents.mapWithIndex {
        case (_, index) =>
          userAnswers.get(TypePage(index)).map(_.`type`) match {
            case Some(docType) if docType != Previous =>
              Some(
                ListItem(
                  name = s"${userAnswers
                    .get(DocumentReferenceNumberPage(index))
                    .map(
                      refNo => s"$docType - $refNo"
                    )
                    .getOrElse(s"$docType")}",
                  changeUrl = Some(Call("GET", "#").url), //TODO: To be added later
                  removeUrl = Some(controllers.documents.routes.RemoveDocumentYesNoController.onPageLoad(arrivalId, mode, index).url)
                )
              )
            case _ => None
          }
      }.flatten

      val docTypeList = documents
        .mapWithIndex {
          case (_, index) =>
            userAnswers.get(TypePage(index)).map(_.`type`)
        }
        .flatten
        .filter(_ != Previous)

      val consignmentLevelDocuments = ConsignmentLevelDocuments(userAnswers)

      new AddAnotherDocumentViewModel(
        listItems,
        onSubmitCall = controllers.documents.routes.AddAnotherDocumentController.onSubmit(arrivalId, mode),
        nextIndex = documents.nextIndex,
        docTypeList,
        allowMore = consignmentLevelDocuments.canAddMore
      )
    }
  }
}