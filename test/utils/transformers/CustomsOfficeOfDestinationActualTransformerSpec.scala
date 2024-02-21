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
import generated.CustomsOfficeOfDestinationActualType03
import generators.Generators
import models.reference.CustomsOffice
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.CustomsOfficeOfDestinationActualPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService

import scala.concurrent.Future

class CustomsOfficeOfDestinationActualTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer: CustomsOfficeOfDestinationActualTransformer = app.injector.instanceOf[CustomsOfficeOfDestinationActualTransformer]

  private lazy val mockRefDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataService].toInstance(mockRefDataService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRefDataService)
  }

  "must transform data" in {

    val customsOfficeOfDestinationActualType03: CustomsOfficeOfDestinationActualType03 = arbitrary[CustomsOfficeOfDestinationActualType03].sample.value

    when(mockRefDataService.getCustomsOfficeByCode(eqTo(customsOfficeOfDestinationActualType03.referenceNumber))(any(), any()))
      .thenReturn(
        Future.successful(Some(CustomsOffice(customsOfficeOfDestinationActualType03.referenceNumber, "name", "countryID", None)))
      )

    val result = transformer.transform(customsOfficeOfDestinationActualType03).apply(Future.successful(emptyUserAnswers)).futureValue

    result.getValue(CustomsOfficeOfDestinationActualPage()).description mustBe "name"
    result.getValue(CustomsOfficeOfDestinationActualPage()).value mustBe customsOfficeOfDestinationActualType03.referenceNumber

  }
}
