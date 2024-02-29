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

package viewModels.transportEquipment.index

import base.SpecBase
import generators.Generators
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import viewModels.transportEquipment.index.AddAnotherSealViewModel.AddAnotherSealViewModelProvider

class AddAnotherSealViewModelSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  "must get list items" - {

    "when there is no seal" in {
      forAll(arbitrary[Mode]) {
        mode =>
          val result = new AddAnotherSealViewModelProvider().apply(emptyUserAnswers, arrivalId, mode, equipmentIndex)

          result.listItems.length mustBe 0
          result.title mustBe s"You have added 0 seal to transport equipment ${equipmentIndex.display}"
          result.heading mustBe s"You have added 0 seal to transport equipment ${equipmentIndex.display}"
          result.emptyListLegend mustBe s"Do you want to add a seal to transport equipment ${equipmentIndex.display}?"
          result.maxLimitLabel mustBe
            s"You cannot add any more seals to transport equipment ${equipmentIndex.display}. To add another, you need to remove one first."
      }
    }

    "when there is one seal" in {
      forAll(arbitrary[Mode], nonEmptyString) {
        (mode, identificationNumber) =>
          val userAnswers = emptyUserAnswers
            .setValue(SealIdentificationNumberPage(equipmentIndex, Index(0)), identificationNumber)

          val result = new AddAnotherSealViewModelProvider().apply(userAnswers, arrivalId, mode, equipmentIndex)

          result.listItems.length mustBe 1
          result.title mustBe s"You have added 1 seal to transport equipment ${equipmentIndex.display}"
          result.heading mustBe s"You have added 1 seal to transport equipment ${equipmentIndex.display}"
          result.legend mustBe s"Do you want to add another seal to transport equipment ${equipmentIndex.display}?"
          result.maxLimitLabel mustBe
            s"You cannot add any more seals to transport equipment ${equipmentIndex.display}. To add another, you need to remove one first."
      }
    }

    "when there are multiple seals" in {
      val formatter = java.text.NumberFormat.getIntegerInstance

      forAll(arbitrary[Mode], nonEmptyString, Gen.choose(2, frontendAppConfig.maxSeals)) {
        (mode, identificationNumber, seals) =>
          val userAnswers = (0 until seals).foldLeft(emptyUserAnswers) {
            (acc, i) =>
              acc
                .setValue(SealIdentificationNumberPage(equipmentIndex, Index(i)), s"$identificationNumber$i")
          }

          val result = new AddAnotherSealViewModelProvider().apply(userAnswers, arrivalId, mode, equipmentIndex)
          result.listItems.length mustBe seals
          result.title mustBe s"You have added ${formatter.format(seals)} seals to transport equipment ${equipmentIndex.display}"
          result.heading mustBe s"You have added ${formatter.format(seals)} seals to transport equipment ${equipmentIndex.display}"
          result.legend mustBe s"Do you want to add another seal to transport equipment ${equipmentIndex.display}?"
          result.maxLimitLabel mustBe
            s"You cannot add any more seals to transport equipment ${equipmentIndex.display}. To add another, you need to remove one first."
      }
    }
  }
}
