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

package utils.transformers

import base.SpecBase
import generated.TransportEquipmentType03
import generators.Generators
import models.Index
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.ContainerIdentificationNumberPage
import pages.sections.transport.equipment.ItemsSection
import pages.sections.{SealsSection, TransportEquipmentSection}
import play.api.libs.json.{JsArray, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransportEquipmentTransformerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private lazy val mockSealsTransformer           = mock[SealsTransformer]
  private lazy val mockGoodsReferencesTransformer = mock[GoodsReferencesTransformer]

  private val transformer = new TransportEquipmentTransformer(mockSealsTransformer, mockGoodsReferencesTransformer)

  "must transform data" in {
    forAll(arbitrary[Seq[TransportEquipmentType03]]) {
      transportEquipment =>
        transportEquipment.zipWithIndex.map {
          case (_, i) =>
            val equipmentIndex = Index(i)

            when(mockSealsTransformer.transform(any(), eqTo(equipmentIndex)))
              .thenReturn {
                ua => Future.successful(ua.setValue(SealsSection(equipmentIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }

            when(mockGoodsReferencesTransformer.transform(any(), eqTo(equipmentIndex)))
              .thenReturn {
                ua => Future.successful(ua.setValue(ItemsSection(equipmentIndex), JsArray(Seq(Json.obj("foo" -> i.toString)))))
              }
        }

        val result = transformer.transform(transportEquipment).apply(emptyUserAnswers).futureValue

        transportEquipment.zipWithIndex.map {
          case (te, i) =>
            val equipmentIndex = Index(i)

            result.getSequenceNumber(TransportEquipmentSection(equipmentIndex)) mustEqual te.sequenceNumber
            result.getRemoved(TransportEquipmentSection(equipmentIndex)) mustEqual false
            result.get(ContainerIdentificationNumberPage(equipmentIndex)) mustEqual te.containerIdentificationNumber
            result.getValue(SealsSection(equipmentIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
            result.getValue(ItemsSection(equipmentIndex)) mustEqual JsArray(Seq(Json.obj("foo" -> i.toString)))
        }
    }
  }
}
