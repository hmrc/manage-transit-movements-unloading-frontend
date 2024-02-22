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
import connectors.ReferenceDataConnector
import generated.PackagingType02
import generators.Generators
import models.Index
import models.reference.PackageType
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.packaging.{PackagingCountPage, PackagingMarksPage, PackagingTypePage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future

class PackagingTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[PackagingTransformer]

  private lazy val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataConnector].toInstance(mockRefDataConnector)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRefDataConnector)
  }

  "must transform data" in {

    val packagingType02: Seq[PackagingType02] = arbitrary[Seq[PackagingType02]].sample.value

    packagingType02.map {
      type0 =>
        when(mockRefDataConnector.getPackageType(eqTo(type0.typeOfPackages))(any(), any()))
          .thenReturn(
            Future.successful(PackageType(type0.typeOfPackages, Some("describe me")))
          )
    }

    val result = transformer.transform(packagingType02, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

    packagingType02.zipWithIndex.map {
      case (packagingType0, i) =>
        val ind = Index(i)
        result.getValue(PackagingTypePage(hcIndex, itemIndex, ind)).code mustBe packagingType0.typeOfPackages
        result.getValue(PackagingTypePage(hcIndex, itemIndex, ind)).description mustBe Some("describe me")
        result.get(PackagingMarksPage(hcIndex, itemIndex, ind)) mustBe packagingType0.shippingMarks
        result.get(PackagingCountPage(hcIndex, itemIndex, ind)) mustBe packagingType0.numberOfPackages

    }

  }
}
