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
import models.UserAnswers
import navigation.Navigator
import pages._
import pages.houseConsignment.index.items.{CustomsUnionAndStatisticsCodePage, GrossWeightPage}
import pages.houseConsignment.index.items.document.{AdditionalInformationPage, DocumentReferenceNumberPage, TypePage}
import play.api.mvc.Call

@Singleton
class HouseConsignmentItemNavigator extends Navigator {

  override def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case GrossWeightPage(_, _)                   => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case CustomsUnionAndStatisticsCodePage(_, _) => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
    case TypePage(_, _, _)                       => ua => Some(controllers.routes.UnloadingFindingsController.onPageLoad(ua.id))
  }

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case _ => _ => Some(Call("GET", "#")) //TODO: Update document navigation
  }
}
