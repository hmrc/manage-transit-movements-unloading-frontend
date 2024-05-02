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
import navigation.houseConsignment.index.{HouseConsignmentDocumentNavigator, HouseConsignmentNavigator}
import navigation.houseConsignment.index.departureMeansOfTransport.DepartureTransportMeansNavigator
import navigation.houseConsignment.index.items.{HouseConsignmentItemNavigator, PackagesNavigator, DocumentNavigator => ItemDocumentNavigator}
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

class FakeSealNavigator(desiredRoute: Call, mode: Mode) extends SealNavigator(mode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeGoodsReferenceNavigator(desiredRoute: Call, mode: Mode) extends GoodsReferenceNavigator(mode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeDepartureTransportMeansNavigator(desiredRoute: Call) extends navigation.DepartureTransportMeansNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHouseConsignmentItemNavigator(desiredRoute: Call) extends HouseConsignmentItemNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHouseConsignmentDocumentNavigator(desiredRoute: Call) extends HouseConsignmentDocumentNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeAdditionalReferenceNavigator(desiredRoute: Call) extends AdditionalReferenceNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHCItemsAdditionalReferenceHouseConsignmentNavigator(desiredRoute: Call) extends navigation.houseConsignment.index.items.AdditionalReferenceNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHCAdditionalReferenceHouseConsignmentNavigator(desiredRoute: Call) extends navigation.houseConsignment.index.AdditionalReferenceNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakePackagesNavigator(desiredRoute: Call) extends PackagesNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHouseConsignmentNavigator(desiredRoute: Call) extends HouseConsignmentNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHouseConsignmentDepartureTransportMeansNavigator(desiredRoute: Call) extends DepartureTransportMeansNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}
