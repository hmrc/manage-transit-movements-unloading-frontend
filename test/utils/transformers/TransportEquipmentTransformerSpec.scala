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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.sections.ItemsSection
import pages.{ContainerIdentificationNumberPage, QuestionPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}

import scala.concurrent.Future

class TransportEquipmentTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[TransportEquipmentTransformer]

  private lazy val mockSealsTransformer = mock[SealsTransformer]
  private lazy val mockItemTransformer  = mock[ItemsTransformer]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[SealsTransformer].toInstance(mockSealsTransformer),
        bind[ItemsTransformer].toInstance(mockItemTransformer)
      )

  private case class FakeSealsSection(equipmentIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ equipmentIndex.position.toString \ "seals"
  }

  private case class FakeItemsSection(houseConsignmentIndex: Index, itemIndex: Index) extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ ItemsSection(houseConsignmentIndex) \ "items" \ itemIndex.position
  }

  "must transform data" in {
    forAll(arbitrary[Seq[TransportEquipmentType05]]) {
      transportEquipment =>
        transportEquipment.zipWithIndex.map {
          case (te, i) =>
            when(mockSealsTransformer.transform(any(), any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeSealsSection(Index(i)), Json.obj("foo" -> "bar")))
              }
            when(mockItemTransformer.transform(any(), any()))
              .thenReturn {
                ua => Future.successful(ua.setValue(FakeItemsSection(hcIndex, itemIndex), Json.obj("foo" -> "bar")))
              }

            val result = transformer.transform(transportEquipment).apply(emptyUserAnswers).futureValue

            result.get(ContainerIdentificationNumberPage(Index(i))) mustBe te.containerIdentificationNumber
            result.getValue(FakeItemsSection(hcIndex, itemIndex)) mustBe Json.obj("foo" -> "bar")
            result.getValue(FakeSealsSection(Index(i))) mustBe Json.obj("foo" -> "bar")
        }
    }
  }
}
