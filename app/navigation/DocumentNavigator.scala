/*
 * Copyright 2024 HM Revenue & Customs
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

package navigation

import com.google.inject.Singleton
import models.DocType.{Support, Transport}
import models.reference.DocumentType
import models._
import pages._
import pages.documents._
import play.api.mvc.Call

@Singleton
class DocumentNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case TypePage(documentIndex)                          => ua => Some(controllers.documents.routes.DocumentReferenceNumberController.onPageLoad(ua.id, NormalMode, documentIndex))
    case DocumentReferenceNumberPage(documentIndex)       => ua => documentReferenceNumberRoute(ua, ua.id, NormalMode, documentIndex)
    case AddAdditionalInformationYesNoPage(documentIndex) => ua => addAdditionalInformationYesNoRoute(ua, ua.id, NormalMode, documentIndex)
    case AdditionalInformationPage(_)                     => ua => Some(controllers.documents.routes.AddAnotherDocumentController.onPageLoad(ua.id, NormalMode))
  }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AdditionalInformationPage(_)   => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case DocumentReferenceNumberPage(_) => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case TypePage(_)                    => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
  }

  def documentReferenceNumberRoute(ua: UserAnswers, arrivalId: ArrivalId, mode: Mode, documentIndex: Index): Option[Call] =
    ua.get(TypePage(documentIndex)) match {
      case Some(DocumentType(Support, _, _)) =>
        Some(controllers.documents.routes.AddAdditionalInformationYesNoController.onPageLoad(arrivalId, mode, documentIndex))
      case Some(DocumentType(Transport, _, _)) => Some(controllers.documents.routes.AddAnotherDocumentController.onPageLoad(ua.id, NormalMode))
      case _                                   => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }

  def addAdditionalInformationYesNoRoute(ua: UserAnswers, arrivalId: ArrivalId, mode: Mode, documentIndex: Index): Option[Call] =
    ua.get(AddAdditionalInformationYesNoPage(documentIndex)) match {
      case Some(true)  => Some(controllers.documents.routes.AdditionalInformationController.onPageLoad(arrivalId, mode, documentIndex))
      case Some(false) => Some(controllers.documents.routes.AddAnotherDocumentController.onPageLoad(ua.id, NormalMode))
      case _           => Some(controllers.routes.SessionExpiredController.onPageLoad())
    }
}
