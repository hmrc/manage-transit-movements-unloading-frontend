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

import config.FrontendAppConfig
import models.{Mode, UserAnswers}
import navigation.houseConsignment.index.departureMeansOfTransport.DepartureTransportMeansNavigator
import navigation.houseConsignment.index.items.{DocumentNavigator as ItemDocumentNavigator, HouseConsignmentItemNavigator, PackagesNavigator}
import navigation.houseConsignment.index.{HouseConsignmentDocumentNavigator, HouseConsignmentNavigator}
import pages.*
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

class FakeItemDocumentNavigator(desiredRoute: Call, houseConsignmentMode: Mode, itemMode: Mode) extends ItemDocumentNavigator(houseConsignmentMode, itemMode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeTransportEquipmentNavigator(desiredRoute: Call) extends TransportEquipmentNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeSealNavigator(desiredRoute: Call, equipmentMode: Mode) extends SealNavigator(equipmentMode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeGoodsReferenceNavigator(desiredRoute: Call, equipmentMode: Mode) extends GoodsReferenceNavigator(equipmentMode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeCountryOfRoutingNavigator(desiredRoute: Call) extends CountryOfRoutingNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeDepartureTransportMeansNavigator(desiredRoute: Call) extends navigation.DepartureTransportMeansNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHouseConsignmentItemNavigator(desiredRoute: Call, houseConsignmentMode: Mode) extends HouseConsignmentItemNavigator(houseConsignmentMode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHouseConsignmentDocumentNavigator(desiredRoute: Call, houseConsignmentMode: Mode) extends HouseConsignmentDocumentNavigator(houseConsignmentMode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeAdditionalReferenceNavigator(desiredRoute: Call) extends AdditionalReferenceNavigator {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHouseConsignmentAdditionalReferenceNavigator(desiredRoute: Call, houseConsignmentMode: Mode)
    extends navigation.houseConsignment.index.AdditionalReferenceNavigator(houseConsignmentMode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeConsignmentItemAdditionalReferenceNavigator(desiredRoute: Call, houseConsignmentMode: Mode, itemMode: Mode)
    extends navigation.houseConsignment.index.items.AdditionalReferenceNavigator(houseConsignmentMode, itemMode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakePackagesNavigator(desiredRoute: Call, houseConsignmentMode: Mode, itemMode: Mode) extends PackagesNavigator(houseConsignmentMode, itemMode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHouseConsignmentNavigator(desiredRoute: Call, appConfig: FrontendAppConfig) extends HouseConsignmentNavigator(appConfig) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}

class FakeHouseConsignmentDepartureTransportMeansNavigator(desiredRoute: Call, houseConsignmentMode: Mode)
    extends DepartureTransportMeansNavigator(houseConsignmentMode) {
  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = desiredRoute
}
