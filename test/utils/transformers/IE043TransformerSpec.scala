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
import generated.CC043CType
import generators.Generators
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.CustomsOfficeOfDestinationActualPage
import pages.sections.*
import play.api.libs.json.Json
import services.ReferenceDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IE043TransformerSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private lazy val mockConsignmentTransformer                 = mock[ConsignmentTransformer]
  private lazy val mockTransitOperationTransformer            = mock[TransitOperationTransformer]
  private lazy val mockHolderOfTheTransitProcedureTransformer = mock[HolderOfTheTransitProcedureTransformer]

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  private val transformer = new IE043Transformer(
    mockConsignmentTransformer,
    mockTransitOperationTransformer,
    mockHolderOfTheTransitProcedureTransformer,
    mockReferenceDataService
  )

  "must transform data" in {
    forAll(arbitrary[CC043CType]) {
      ie043 =>
        val customsOfficeOfDestination = ie043.CustomsOfficeOfDestinationActual.referenceNumber
        val customsOffice              = CustomsOffice(customsOfficeOfDestination, "name", "countryID", None)

        when(mockConsignmentTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(ConsignmentSection, Json.obj("foo" -> "bar")))
          }

        when(mockHolderOfTheTransitProcedureTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(HolderOfTheTransitProcedureSection, Json.obj("foo" -> "bar")))
          }

        when(mockTransitOperationTransformer.transform(any())(any()))
          .thenReturn {
            ua => Future.successful(ua.setValue(TransitOperationSection, Json.obj("foo" -> "bar")))
          }

        when(mockReferenceDataService.getCustomsOffice(eqTo(customsOfficeOfDestination))(any()))
          .thenReturn(
            Future.successful(customsOffice)
          )

        val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

        val result = transformer.transform(userAnswers).futureValue

        result.getValue(ConsignmentSection) mustEqual Json.obj("foo" -> "bar")
        result.getValue(TransitOperationSection) mustEqual Json.obj("foo" -> "bar")
        result.getValue(HolderOfTheTransitProcedureSection) mustEqual Json.obj("foo" -> "bar")
        result.getValue(CustomsOfficeOfDestinationActualPage) mustEqual customsOffice
    }
  }
}
