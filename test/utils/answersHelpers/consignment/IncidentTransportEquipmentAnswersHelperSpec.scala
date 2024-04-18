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
import generated.{GoodsReferenceType01, SealType04, TransportEquipmentType07}
import models.UserAnswers
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.answersHelpers.AnswersHelperSpecBase
import utils.answersHelpers.consignment.incident.IncidentTransportEquipmentAnswersHelper

class IncidentTransportEquipmentAnswersHelperSpec extends AnswersHelperSpecBase with AppWithDefaultMockFixtures {

  val mockUserAnswers: UserAnswers                         = mock[UserAnswers]
  val mockTransportEquipmentType: TransportEquipmentType07 = mock[TransportEquipmentType07]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(bind[TransportEquipmentType07].toInstance(mockTransportEquipmentType))

  "IncidentTransportEquipmentAnswersHelper" - {

    "containerIdentificationNumber" - {

      "must return None when containerIdentificationNumber undefined" in {

        val helper =
          new IncidentTransportEquipmentAnswersHelper(emptyUserAnswers,
                                                      TransportEquipmentType07(1, None, None, Seq(SealType04(1, "1")), Seq(GoodsReferenceType01(1, 123)))
          )
        helper.containerIdentificationNumber mustBe None
      }

      "return a SummaryListRow for containerIdentificationNumber" in {
        val helper: IncidentTransportEquipmentAnswersHelper = new IncidentTransportEquipmentAnswersHelper(mockUserAnswers, mockTransportEquipmentType)
        when(mockTransportEquipmentType.containerIdentificationNumber).thenReturn(Some("Container123"))

        val result: Option[SummaryListRow] = helper.containerIdentificationNumber

        result.isDefined mustBe true
        result.get.value.content.value mustBe "Container123"
        result.get.actions must not be defined

      }
    }

    "transportEquipmentSeals" - {
      "must generate row for each seal" in {

        val helper: IncidentTransportEquipmentAnswersHelper = new IncidentTransportEquipmentAnswersHelper(mockUserAnswers, mockTransportEquipmentType)
        when(mockTransportEquipmentType.Seal).thenReturn(Seq(SealType04(1, "Seal1"), SealType04(2, "Seal2")))

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

    "itemNumber" - {
      "must generate row for item" in {

        val helper: IncidentTransportEquipmentAnswersHelper = new IncidentTransportEquipmentAnswersHelper(mockUserAnswers, mockTransportEquipmentType)
        when(mockTransportEquipmentType.GoodsReference).thenReturn(Seq(GoodsReferenceType01(1, 123), GoodsReferenceType01(2, 234)))

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
