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
import generated.CC043CType
import generators.Generators
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{CustomsOfficeOfDestinationActualPage, QuestionPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsPath, Json}
import services.ReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IE043TransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[IE043Transformer]

  private lazy val mockConsignmentTransformer                 = mock[ConsignmentTransformer]
  private lazy val mockTransitOperationTransformer            = mock[TransitOperationTransformer]
  private lazy val mockHolderOfTheTransitProcedureTransformer = mock[HolderOfTheTransitProcedureTransformer]

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ConsignmentTransformer].toInstance(mockConsignmentTransformer),
        bind[TransitOperationTransformer].toInstance(mockTransitOperationTransformer),
        bind[HolderOfTheTransitProcedureTransformer].toInstance(mockHolderOfTheTransitProcedureTransformer),
        bind[ReferenceDataService].toInstance(mockReferenceDataService)
      )

  private case object FakeConsignmentSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "consignment"
  }

  private case object FakeTransitOperationSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "transitOperation"
  }

  private case object FakeCustomsOfficeOfDestinationActualSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "CustomsOfficeOfDestinationActual"
  }

  private case object FakeHolderOfTheTransitProcedureSection extends QuestionPage[JsObject] {
    override def path: JsPath = JsPath \ "HolderOfTheTransitProcedure"
  }

  "must transform data" in {
    forAll(arbitrary[CC043CType]) {
      ie043 =>
        val customsOfficeOfDestination = ie043.CustomsOfficeOfDestinationActual.referenceNumber
        val customsOffice              = CustomsOffice(customsOfficeOfDestination, "name", "countryID", None)

        when(mockConsignmentTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(FakeConsignmentSection, Json.obj("foo" -> "bar")))
          }

        when(mockHolderOfTheTransitProcedureTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(FakeHolderOfTheTransitProcedureSection, Json.obj("foo" -> "bar")))
          }

        when(mockTransitOperationTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(FakeTransitOperationSection, Json.obj("foo" -> "bar")))
          }

        when(mockReferenceDataService.getCustomsOffice(eqTo(customsOfficeOfDestination))(any()))
          .thenReturn(
            Future.successful(customsOffice)
          )

        val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

        val result = transformer.transform(userAnswers).futureValue

        result.getValue(FakeConsignmentSection) mustEqual Json.obj("foo" -> "bar")
        result.getValue(FakeTransitOperationSection) mustEqual Json.obj("foo" -> "bar")
        result.getValue(FakeHolderOfTheTransitProcedureSection) mustEqual Json.obj("foo" -> "bar")
        result.getValue(CustomsOfficeOfDestinationActualPage) mustEqual customsOffice
    }
  }
}
