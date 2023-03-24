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
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService
import viewModels.UnloadingFindingsViewModel.UnloadingFindingsViewModelProvider

import scala.concurrent.Future
import play.api.inject.bind

import scala.concurrent.ExecutionContext.Implicits.global

class UnloadingFindingsViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {
  val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[ReferenceDataService].toInstance(mockReferenceDataService))

  "Unloading findings sections" - {

    "must render Means of Transport section" - {
      "when there is one" in {

        val userAnswers = emptyUserAnswers
          .setValue(VehicleIdentificationNumberPage(index), "123456")
          .setValue(VehicleIdentificationTypePage(index), "31")
          .setValue(VehicleRegistrationCountryPage(index), "GB")

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful("Great Britian"))

        val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers).futureValue
        val section           = result.section.head

        section.sectionTitle.value mustBe "Means of transport 1"
        section.rows.size mustBe 2
        section.viewLink must not be defined
      }
      "when there is multiple" in {
        val userAnswers = emptyUserAnswers
          .setValue(VehicleIdentificationNumberPage(index), "123456")
          .setValue(VehicleIdentificationTypePage(index), "31")
          .setValue(VehicleRegistrationCountryPage(index), "DE")
          .setValue(VehicleIdentificationNumberPage(Index(1)), "123456")
          .setValue(VehicleIdentificationTypePage(Index(1)), "31")
          .setValue(VehicleRegistrationCountryPage(Index(1)), "DE")

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful("Great Britian"))

        val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers).futureValue
        val section           = result.section(1)

        section.sectionTitle.value mustBe "Means of transport 2"
        section.rows.size mustBe 2
        section.viewLink must not be defined
      }
    }

    "must render transport equipment section" - {
      "when there is one" - {

        "with no seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "123456")

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful("Great Britian"))

          val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
          val result            = viewModelProvider.apply(userAnswers).futureValue
          val section           = result.section.head

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 1
          section.viewLink must not be defined
        }

        "with seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "123456")
            .setValue(SealPage(equipmentIndex, sealIndex), "123456")

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful("Great Britian"))

          val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
          val result            = viewModelProvider.apply(userAnswers).futureValue
          val section           = result.section.head

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 2
          section.viewLink must not be defined
        }
      }
      "when there is multiple" - {

        "with no seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "123456")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "123456")

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful("Great Britian"))

          val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
          val result            = viewModelProvider.apply(userAnswers).futureValue
          val section           = result.section(1)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 1
          section.viewLink must not be defined
        }

        "with seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "123456")
            .setValue(SealPage(equipmentIndex, sealIndex), "123456")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "123456")
            .setValue(SealPage(Index(1), sealIndex), "123456")

          setExistingUserAnswers(userAnswers)
          when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful("Great Britian"))

          val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
          val result            = viewModelProvider.apply(userAnswers).futureValue
          val section           = result.section(1)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 2
          section.viewLink must not be defined
        }
      }
    }

    "must render house consignment sections" - {
      "when there is one" in {
        val userAnswers = emptyUserAnswers
          .setValue(GrossWeightPage(index, itemIndex), 10.00d)
          .setValue(NetWeightPage(index, itemIndex), 20.00d)
          .setValue(ConsignorNamePage(index), "name")
          .setValue(ConsignorIdentifierPage(index), "identifier")

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful("Great Britian"))

        val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers).futureValue
        val section           = result.section.head

        section.sectionTitle.value mustBe "House consignment 1"
        section.rows.size mustBe 4
        section.viewLink mustBe defined
      }
      "when there is multiple" in {
        val userAnswers = emptyUserAnswers
          .setValue(GrossWeightPage(index, itemIndex), 10.00d)
          .setValue(NetWeightPage(index, itemIndex), 20.00d)
          .setValue(ConsignorNamePage(index), "name")
          .setValue(ConsignorIdentifierPage(index), "identifier")
          .setValue(GrossWeightPage(Index(1), itemIndex), 10.00d)
          .setValue(NetWeightPage(Index(1), itemIndex), 20.00d)
          .setValue(ConsignorNamePage(Index(1)), "name")
          .setValue(ConsignorIdentifierPage(Index(1)), "identifier")

        setExistingUserAnswers(userAnswers)
        when(mockReferenceDataService.getCountryNameByCode(any())(any(), any())).thenReturn(Future.successful("Great Britian"))

        val viewModelProvider = new UnloadingFindingsViewModelProvider(mockReferenceDataService)
        val result            = viewModelProvider.apply(userAnswers).futureValue
        val section           = result.section(1)

        section.sectionTitle.value mustBe "House consignment 2"
        section.rows.size mustBe 4
        section.viewLink mustBe defined
      }
    }

//    "must render item section" - {
//      "when there is one" - {
//        val userAnswers = emptyUserAnswers
//          .setValue(ItemDescriptionPage(index, itemIndex), "shirts")
//          .setValue(GrossWeightPage(index, itemIndex), 10.00d)
//          .setValue(NetWeightPage(index, itemIndex), 20.00d)
//
//        setExistingUserAnswers(userAnswers)
//
//        val viewModelProvider = new UnloadingFindingsViewModelProvider()
//        val result            = viewModelProvider.apply(userAnswers)
//        val section           = result.section(2)
//
//        section.sectionTitle.value mustBe "Item 1"
//        section.rows.size mustBe 3
//        section.viewLink must not be defined
//      }
//
//      "when there is multiple" - {
//        val userAnswers = emptyUserAnswers
//          .setValue(ItemDescriptionPage(index, itemIndex), "shirts")
//          .setValue(GrossWeightPage(index, itemIndex), 10.00d)
//          .setValue(NetWeightPage(index, itemIndex), 20.00d)
//          .setValue(ItemDescriptionPage(Index(1), Index(1)), "pants")
//          .setValue(GrossWeightPage(Index(1), Index(1)), 22.00d)
//          .setValue(NetWeightPage(Index(1), Index(1)), 24.00d)
//
//        setExistingUserAnswers(userAnswers)
//
//        val viewModelProvider = new UnloadingFindingsViewModelProvider()
//        val result            = viewModelProvider.apply(userAnswers)
//        val section           = result.section(3)
//
//        section.sectionTitle.value mustBe "Item 2"
//        section.rows.size mustBe 3
//        section.viewLink must not be defined
//      }
//    }
  }
}
