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

package extractors

import base.SpecBase
import generators.Generators
import models.{Seal, UnloadingPermission}
import models.reference.Country
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import pages._
import queries.{GoodsItemsQuery, SealsQuery}
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnloadingPermissionExtractorSpec extends SpecBase with Generators with BeforeAndAfterEach {

  private val mockReferenceDataService = mock[ReferenceDataService]
  private val extractor                = new UnloadingPermissionExtractor(mockReferenceDataService)

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  "must populate user answers with unloading permission data" in {
    forAll(arbitrary[UnloadingPermission], arbitrary[Country]) {
      (unloadingPermission, country) =>
        beforeEach()

        when(mockReferenceDataService.getCountryByCode(any())(any(), any()))
          .thenReturn(Future.successful(Some(country)))

        whenReady(extractor.apply(emptyUserAnswers, unloadingPermission)) {
          result =>
            result.get.get(VehicleNameRegistrationReferencePage) mustBe unloadingPermission.transportIdentity
            result.get.get(VehicleRegistrationCountryPage).get mustBe country
            result.get.get(GrossWeightAmountPage).get mustBe unloadingPermission.GrossWeight
            result.get.get(TotalNumberOfItemsPage).get mustBe unloadingPermission.numberOfItems
            result.get.get(TotalNumberOfPackagesPage) mustBe unloadingPermission.numberOfPackages
            result.get.get(SealsQuery) mustBe unloadingPermission.seals.map(_.sealIds.map(Seal(_, removable = false)))
            result.get.get(GoodsItemsQuery).get mustBe unloadingPermission.goodsItems.map(_.description).toList

            result.get.get(DateOfPreparationPage).get mustBe unloadingPermission.dateOfPreparation

            verify(mockReferenceDataService).getCountryByCode(eqTo(unloadingPermission.transportCountry))(any(), any())
        }
    }
  }
}
