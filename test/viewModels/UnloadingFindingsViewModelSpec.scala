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
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import viewModels.UnloadingFindingsViewModel.UnloadingFindingsViewModelProvider

class UnloadingFindingsViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  "Unloading findings sections" - {

    "must render Means of Transport section" in {
      val userAnswers = emptyUserAnswers
        .setValue(VehicleIdentificationNumberPage, "123456")
        .setValue(VehicleIdentificationTypePage, "31")
        .setValue(VehicleRegistrationCountryPage, "DE")

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new UnloadingFindingsViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)
      val section           = result.section.head

      section.sectionTitle.value mustBe "Means of transport"
      section.rows.size mustBe 2
      section.addAnotherLink must not be defined
    }

    "must render transport equipment section" - {
      "when there is one" - {

        "with no seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "123456")

          setExistingUserAnswers(userAnswers)

          val viewModelProvider = new UnloadingFindingsViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.section(1)

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 1
          section.addAnotherLink must not be defined
        }

        "with seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "123456")
            .setValue(SealPage(equipmentIndex, sealIndex), "123456")

          setExistingUserAnswers(userAnswers)

          val viewModelProvider = new UnloadingFindingsViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.section(1)

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 2
          section.addAnotherLink mustBe defined
        }

        "with seals and added seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "123456")
            .setValue(SealPage(equipmentIndex, sealIndex), "123456")
            .setValue(NewSealPage(equipmentIndex, sealIndex), "123456")

          setExistingUserAnswers(userAnswers)

          val viewModelProvider = new UnloadingFindingsViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.section(1)

          section.sectionTitle.value mustBe "Transport equipment 1"
          section.rows.size mustBe 3
          section.addAnotherLink mustBe defined
        }

      }
      "when there is multiple" - {

        "with no seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "123456")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "123456")

          setExistingUserAnswers(userAnswers)

          val viewModelProvider = new UnloadingFindingsViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.section(2)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 1
          section.addAnotherLink must not be defined
        }

        "with seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "123456")
            .setValue(SealPage(equipmentIndex, sealIndex), "123456")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "123456")
            .setValue(SealPage(Index(1), sealIndex), "123456")

          setExistingUserAnswers(userAnswers)

          val viewModelProvider = new UnloadingFindingsViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.section(2)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 2
          section.addAnotherLink mustBe defined
        }

        "with seals and added seals" in {
          val userAnswers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(equipmentIndex), "123456")
            .setValue(SealPage(equipmentIndex, sealIndex), "123456")
            .setValue(NewSealPage(equipmentIndex, sealIndex), "123456")
            .setValue(ContainerIdentificationNumberPage(Index(1)), "123456")
            .setValue(SealPage(Index(1), sealIndex), "123456")
            .setValue(NewSealPage(Index(1), sealIndex), "123456")

          setExistingUserAnswers(userAnswers)

          val viewModelProvider = new UnloadingFindingsViewModelProvider()
          val result            = viewModelProvider.apply(userAnswers)
          val section           = result.section(2)

          section.sectionTitle.value mustBe "Transport equipment 2"
          section.rows.size mustBe 3
          section.addAnotherLink mustBe defined
        }
      }
    }

    "must render item summary section" in {
      val userAnswers = emptyUserAnswers
        .setValue(ItemDescriptionPage(itemIndex), "shirts")
        .setValue(GrossWeightPage(itemIndex), 10.00d)
        .setValue(NetWeightPage(itemIndex), 20.00d)

      setExistingUserAnswers(userAnswers)

      val viewModelProvider = new UnloadingFindingsViewModelProvider()
      val result            = viewModelProvider.apply(userAnswers)
      val section           = result.section(1)

      section.sectionTitle.value mustBe "Items summary"
      section.rows.size mustBe 3
      section.addAnotherLink must not be defined
    }

    "must render item section" - {
      "when there is one" - {
        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(itemIndex), "shirts")
          .setValue(GrossWeightPage(itemIndex), 10.00d)
          .setValue(NetWeightPage(itemIndex), 20.00d)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.section(2)

        section.sectionTitle.value mustBe "Item 1"
        section.rows.size mustBe 3
        section.addAnotherLink must not be defined
      }

      "when there is multiple" - {
        val userAnswers = emptyUserAnswers
          .setValue(ItemDescriptionPage(itemIndex), "shirts")
          .setValue(GrossWeightPage(itemIndex), 10.00d)
          .setValue(NetWeightPage(itemIndex), 20.00d)
          .setValue(ItemDescriptionPage(Index(1)), "pants")
          .setValue(GrossWeightPage(Index(1)), 22.00d)
          .setValue(NetWeightPage(Index(1)), 24.00d)

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.section(3)

        section.sectionTitle.value mustBe "Item 2"
        section.rows.size mustBe 3
        section.addAnotherLink must not be defined
      }
    }

    "must render additional comments section" - {
      "when there is not a comment" in {
        val userAnswers = emptyUserAnswers

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.additionalComments

        section.sectionTitle.value mustBe "Additional comments"
        section.rows.size mustBe 0
        section.addAnotherLink mustBe defined
      }

      "when there is a comment" in {
        val userAnswers = emptyUserAnswers
          .setValue(UnloadingCommentsPage, "comment")

        setExistingUserAnswers(userAnswers)

        val viewModelProvider = new UnloadingFindingsViewModelProvider()
        val result            = viewModelProvider.apply(userAnswers)
        val section           = result.additionalComments

        section.sectionTitle.value mustBe "Additional comments"
        section.rows.size mustBe 1
        section.addAnotherLink must not be defined
      }
    }
  }
}
