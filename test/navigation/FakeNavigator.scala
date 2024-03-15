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

package navigation

import models.{Mode, UserAnswers}
import navigation.houseConsignment.index.items.{DocumentNavigator => ItemDocumentNavigator}
import pages._
import play.api.mvc.Call

class FakeNavigator(desiredRoute: Call) extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    desiredRoute

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case _ =>
      _ => Some(desiredRoute)
  }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case _ =>
      _ => Some(desiredRoute)
  }
}

class FakeDocumentNavigator(desiredRoute: Call) extends DocumentNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeNavigation(desiredRoute: Call) extends Navigation {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeItemDocumentNavigator(desiredRoute: Call) extends ItemDocumentNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeTransportEquipmentNavigator(desiredRoute: Call) extends TransportEquipmentNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeDepartureTransportMeansNavigator(desiredRoute: Call) extends DepartureTransportMeansNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeConsignmentItemNavigator(desiredRoute: Call) extends ConsignmentItemNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeItemNavigator(desiredRoute: Call) extends ItemNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}
