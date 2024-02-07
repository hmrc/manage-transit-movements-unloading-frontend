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

package generators

import models.Index
import org.scalacheck.Arbitrary
import pages._
import pages.departureMeansOfTransport.{CountryPage, VehicleIdentificationNumberPage}
import pages.houseConsignment.index.items.GrossWeightPage
import pages.transportEquipment.index.seals.SealIdentificationNumberPage

trait PageGenerators {

  implicit lazy val arbitraryTotalNumberOfPackagesPage: Arbitrary[NumberOfPackagesPage.type] =
    Arbitrary(NumberOfPackagesPage)

  implicit lazy val arbitraryConfirmRemoveCommentsPage: Arbitrary[ConfirmRemoveCommentsPage.type] =
    Arbitrary(ConfirmRemoveCommentsPage)

  implicit lazy val arbitraryChangesToReportPage: Arbitrary[UnloadingCommentsPage.type] =
    Arbitrary(UnloadingCommentsPage)

  implicit lazy val arbitraryAreAnySealsBrokenPage: Arbitrary[AreAnySealsBrokenPage.type] =
    Arbitrary(AreAnySealsBrokenPage)

  implicit lazy val arbitraryCanSealsBeReadPage: Arbitrary[CanSealsBeReadPage.type] =
    Arbitrary(CanSealsBeReadPage)

  implicit lazy val arbitrarySealNumberPage: Arbitrary[SealIdentificationNumberPage] =
    Arbitrary(SealIdentificationNumberPage(Index(0), Index(0)))

  implicit lazy val arbitraryGrossWeightAmountPage: Arbitrary[GrossWeightPage] =
    Arbitrary(GrossWeightPage(Index(0), Index(0)))

  implicit lazy val arbitraryDepartureMeansOfTransportCountryPage: Arbitrary[CountryPage] =
    Arbitrary(CountryPage(Index(0)))

  implicit lazy val arbitraryVehicleNameRegistrationReferencePage: Arbitrary[VehicleIdentificationNumberPage] =
    Arbitrary(VehicleIdentificationNumberPage(Index(0)))

  implicit lazy val arbitraryDateGoodsUnloadedPage: Arbitrary[DateGoodsUnloadedPage.type] =
    Arbitrary(DateGoodsUnloadedPage)
}
