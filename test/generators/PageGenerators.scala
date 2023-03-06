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

trait PageGenerators {

  implicit lazy val arbitraryTotalNumberOfPackagesPage: Arbitrary[TotalNumberOfPackagesPage.type] =
    Arbitrary(TotalNumberOfPackagesPage)

  implicit lazy val arbitraryTotalNumberOfItemsPage: Arbitrary[TotalNumberOfItemsPage.type] =
    Arbitrary(TotalNumberOfItemsPage)

  implicit lazy val arbitraryConfirmRemoveSealPage: Arbitrary[ConfirmRemoveSealPage.type] =
    Arbitrary(ConfirmRemoveSealPage)

  implicit lazy val arbitraryConfirmRemoveCommentsPage: Arbitrary[ConfirmRemoveCommentsPage.type] =
    Arbitrary(ConfirmRemoveCommentsPage)

  implicit lazy val arbitraryChangesToReportPage: Arbitrary[UnloadingCommentsPage.type] =
    Arbitrary(UnloadingCommentsPage)

  implicit lazy val arbitraryAreAnySealsBrokenPage: Arbitrary[AreAnySealsBrokenPage.type] =
    Arbitrary(AreAnySealsBrokenPage)

  implicit lazy val arbitraryCanSealsBeReadPage: Arbitrary[CanSealsBeReadPage.type] =
    Arbitrary(CanSealsBeReadPage)

  implicit lazy val arbitrarySealNumberPage: Arbitrary[SealPage] =
    Arbitrary(SealPage(Index(0)))

  implicit lazy val arbitraryGrossWeightAmountPage: Arbitrary[GrossWeightPage.type] =
    Arbitrary(GrossWeightPage)

  implicit lazy val arbitraryVehicleRegistrationCountryPage: Arbitrary[VehicleRegistrationCountryPage.type] =
    Arbitrary(VehicleRegistrationCountryPage)

  implicit lazy val arbitraryVehicleNameRegistrationReferencePage: Arbitrary[VehicleIdentificationNumberPage.type] =
    Arbitrary(VehicleIdentificationNumberPage)

  implicit lazy val arbitraryDateGoodsUnloadedPage: Arbitrary[DateGoodsUnloadedPage.type] =
    Arbitrary(DateGoodsUnloadedPage)
}
