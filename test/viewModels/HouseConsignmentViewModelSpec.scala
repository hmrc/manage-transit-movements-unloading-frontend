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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.Index
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.HouseConsignmentViewModel.HouseConsignmentViewModelProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HouseConsignmentViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]
  private val countryDesc                            = "Great Britain"

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "HouseConsignmentViewModel" - {

    "must render Means of Transport section" - {
      "when there is one" in {

        val userAnswers = emptyUserAnswers
          .setValue(DepartureTransportMeansIdentificationNumberPage(index, index), "123456")
          .setValue(DepartureTransportMeansIdentificationTypePage(index, index), "31")
          .setValue(DepartureTransportMeansCountryPage(index, index), "GB")

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new HouseConsignmentViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers, index).futureValue
        val section           = result.section.head

        section.sectionTitle.value mustBe "Departure means of transport 1"
        section.rows.size mustBe 2
        section.viewLink must not be defined
      }
      "when there is multiple" in {
        val userAnswers = emptyUserAnswers
          .setValue(DepartureTransportMeansIdentificationNumberPage(index, index), "123456")
          .setValue(DepartureTransportMeansIdentificationTypePage(index, index), "31")
          .setValue(DepartureTransportMeansCountryPage(index, index), "DE")
          .setValue(DepartureTransportMeansIdentificationNumberPage(index, Index(1)), "123456")
          .setValue(DepartureTransportMeansIdentificationTypePage(index, Index(1)), "31")
          .setValue(DepartureTransportMeansCountryPage(index, Index(1)), "DE")

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new HouseConsignmentViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers, index).futureValue
        val section           = result.section(1)

        section.sectionTitle.value mustBe "Departure means of transport 2"
        section.rows.size mustBe 2
        section.viewLink must not be defined
      }
    }

    "must render house consignment section" - {
      "when there is one" in {
        val userAnswers = emptyUserAnswers
          .setValue(GrossWeightPage(index, itemIndex), 10.00d)
          .setValue(NetWeightPage(index, itemIndex), 20.00d)
          .setValue(ConsignorNamePage(index), "name")
          .setValue(ConsignorIdentifierPage(index), "identifier")

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful(countryDesc))

        val viewModelProvider = new HouseConsignmentViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers, index).futureValue
        val section           = result.houseConsignment.head

        section.sectionTitle.value mustBe "House consignment 1"
        section.rows.size mustBe 4
      }
    }

    "must render item section" - {
      "when there is one" in {
        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(index, itemIndex), "shirts")
          .setValue(GrossWeightPage(index, itemIndex), 10.00d)
          .setValue(NetWeightPage(index, itemIndex), 20.00d)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new HouseConsignmentViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers, index).futureValue
        val section           = result.section.head

        section.sectionTitle.value mustBe "Item 1"
        section.rows.size mustBe 3
      }

      "when there are multiple" in {
        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(index, itemIndex), "shirts")
          .setValue(GrossWeightPage(index, itemIndex), 10.00d)
          .setValue(NetWeightPage(index, itemIndex), 20.00d)
          .setValue(ItemDescriptionPage(index, Index(1)), "pants")
          .setValue(GrossWeightPage(index, Index(1)), 22.00d)
          .setValue(NetWeightPage(index, Index(1)), 24.00d)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new HouseConsignmentViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers, index).futureValue
        val section           = result.section(1)

        section.sectionTitle.value mustBe "Item 2"
        section.rows.size mustBe 3
      }
    }
  }
}
