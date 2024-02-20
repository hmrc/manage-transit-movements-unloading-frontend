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

package services

import base.SpecBase
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import generators.Generators
import models.SelectableList
import models.reference.AdditionalReferenceType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AdditionalReferencesServiceSpec extends SpecBase with BeforeAndAfterEach with Generators {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new AdditionalReferencesService(mockRefDataConnector)

  private val additionalReference1: AdditionalReferenceType = AdditionalReferenceType("Y015", "The rough diamonds are contained in tamper-resistant containers")
  private val additionalReference2: AdditionalReferenceType = AdditionalReferenceType("C658", "Consignor / exporter (AEO certificate number)")

  private val additionalReference3: AdditionalReferenceType =
    AdditionalReferenceType("Y016", "Electronic administrative document (e-AD), as referred to in Article 3(1) of Reg. (EC) No 684/2009")
  private val additionalReferences = NonEmptySet.of(additionalReference2, additionalReference1, additionalReference3)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "AdditionalReferences" - {

    "getAdditionalReferences" - {
      "must return a list of sorted countries" in {

        when(mockRefDataConnector.getAdditionalReferences()(any(), any()))
          .thenReturn(Future.successful(additionalReferences))

        service.getAdditionalReferences().futureValue mustBe
          SelectableList(Seq(additionalReference2, additionalReference3, additionalReference1))

        verify(mockRefDataConnector).getAdditionalReferences()(any(), any())
      }
    }
  }
}
