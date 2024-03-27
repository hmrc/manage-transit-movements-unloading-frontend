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

package navigation.houseConsignment.index.items

import com.google.inject.Singleton
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages._
import pages.houseConsignment.index.items.document.{AdditionalInformationPage, DocumentReferenceNumberPage, TypePage}
import play.api.mvc.Call

@Singleton
class DocumentNavigator extends Navigator {

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case AdditionalInformationPage(houseConsignmentIndex, itemIndex, documentIndex) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case DocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, documentIndex) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
    case TypePage(houseConsignmentIndex, index, documentIndex) =>
      ua => Some(controllers.routes.HouseConsignmentController.onPageLoad(ua.id, houseConsignmentIndex))
  }

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case TypePage(houseConsignmentIndex, index, _) =>
      ua =>
        Some(controllers.houseConsignment.index.items.document.routes.AddAnotherDocumentController.onPageLoad(ua.id, houseConsignmentIndex, index, NormalMode))
    case _ => _ => Some(Call("GET", "#")) //TODO: Update document navigation
  }
}
