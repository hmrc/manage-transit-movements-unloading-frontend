/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.answersHelpers.consignment

import base.AppWithDefaultMockFixtures
import generated._
import org.scalacheck.Arbitrary.arbitrary
import utils.answersHelpers.AnswersHelperSpecBase
import utils.answersHelpers.consignment.incident.IncidentTransportEquipmentAnswersHelper

class IncidentTransportEquipmentAnswersHelperSpec extends AnswersHelperSpecBase with AppWithDefaultMockFixtures {

  "IncidentTransportEquipmentAnswersHelper" - {

    "containerIdentificationNumber" - {

      "must return None when containerIdentificationNumber undefined" in {
        forAll(arbitrary[TransportEquipmentType06].map(_.copy(containerIdentificationNumber = None))) {
          transportEquipment =>
            val helper = new IncidentTransportEquipmentAnswersHelper(emptyUserAnswers, transportEquipment)

            val result = helper.containerIdentificationNumber

            result mustBe None
        }
      }

      "return a SummaryListRow for containerIdentificationNumber" in {
        val containerIdentificationNumber = "Container123"

        forAll(arbitrary[TransportEquipmentType06].map(_.copy(containerIdentificationNumber = Some(containerIdentificationNumber)))) {
          transportEquipment =>
            val helper = new IncidentTransportEquipmentAnswersHelper(emptyUserAnswers, transportEquipment)

            val result = helper.containerIdentificationNumber

            result.isDefined mustBe true
            result.get.value.content.value mustBe containerIdentificationNumber
            result.get.actions must not be defined
        }
      }
    }

    "transportEquipmentSeals" - {
      "must generate row for each seal" in {
        val seals = Seq(SealType01(1, "Seal1"), SealType01(2, "Seal2"))

        forAll(arbitrary[TransportEquipmentType06].map(_.copy(Seal = seals))) {
          transportEquipment =>
            val helper = new IncidentTransportEquipmentAnswersHelper(emptyUserAnswers, transportEquipment)

            val result = helper.transportEquipmentSeals

            result.sectionTitle.value mustBe "Seals"

            result.rows.size mustBe 2

            result.rows.head.key.value mustBe "Seal 1"
            result.rows.head.value.value mustBe "Seal1"
            result.rows.head.actions must not be defined

            result.rows(1).key.value mustBe "Seal 2"
            result.rows(1).value.value mustBe "Seal2"
            result.rows(1).actions must not be defined
        }
      }
    }

    "itemNumber" - {
      "must generate row for item" in {
        val goodsReferences = Seq(GoodsReferenceType03(1, 123), GoodsReferenceType03(2, 234))

        forAll(arbitrary[TransportEquipmentType06].map(_.copy(GoodsReference = goodsReferences))) {
          transportEquipment =>
            val helper = new IncidentTransportEquipmentAnswersHelper(emptyUserAnswers, transportEquipment)

            val result = helper.itemNumbers

            result.sectionTitle.value mustBe "Goods item numbers"

            result.rows.size mustBe 2

            result.rows.head.key.value mustBe "Goods item number 1"
            result.rows.head.value.value mustBe "123"
            result.rows.head.actions must not be defined

            result.rows(1).key.value mustBe "Goods item number 2"
            result.rows(1).value.value mustBe "234"
            result.rows(1).actions must not be defined
        }
      }
    }
  }
}
