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

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated.TransportEquipmentType05
import generators.Generators
import models.Index
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.TransportEquipmentSection
import pages.{ContainerIdentificationNumberPage, QuestionPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsBoolean, JsObject, JsPath, Json}

import scala.concurrent.Future

class TransportEquipmentTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[TransportEquipmentTransformer]

  private lazy val mockSealsTransformer           = mock[SealsTransformer]
  private lazy val mockGoodsReferencesTransformer = mock[GoodsReferencesTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[SealsTransformer].toInstance(mockSealsTransformer),
        bind[GoodsReferencesTransformer].toInstance(mockGoodsReferencesTransformer)
      )

  private case class FakeSealsSection(equipmentIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ equipmentIndex.position.toString \ "seals"
  }

  private case class FakeGoodsReferencesSection(equipmentIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ equipmentIndex.position.toString \ "goodsReferences"
  }

  "must transform data" in {
    forAll(arbitrary[Seq[TransportEquipmentType05]]) {
      transportEquipment =>
        transportEquipment.zipWithIndex.map {
          case (_, i) =>
            val equipmentIndex = Index(i)

            when(mockSealsTransformer.transform(any(), eqTo(equipmentIndex)))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeSealsSection(equipmentIndex), Json.obj("foo" -> i.toString)))
              }
            when(mockGoodsReferencesTransformer.transform(any(), eqTo(equipmentIndex)))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeGoodsReferencesSection(equipmentIndex), Json.obj("foo" -> i.toString)))
              }
        }

        val result = transformer.transform(transportEquipment).apply(emptyUserAnswers).futureValue

        transportEquipment.zipWithIndex.map {
          case (te, i) =>
            val equipmentIndex = Index(i)

            result.getSequenceNumber(TransportEquipmentSection(equipmentIndex)) mustBe te.sequenceNumber
            result.getValue[JsBoolean](TransportEquipmentSection(equipmentIndex), "addedFromIE043").value mustBe true
            result.get(ContainerIdentificationNumberPage(equipmentIndex)) mustBe te.containerIdentificationNumber
            result.getValue(FakeSealsSection(equipmentIndex)) mustBe Json.obj("foo" -> i.toString)
            result.getValue(FakeGoodsReferencesSection(equipmentIndex)) mustBe Json.obj("foo" -> i.toString)
        }
    }
  }
}
