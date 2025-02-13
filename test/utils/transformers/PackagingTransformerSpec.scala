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
import generated.PackagingType02
import generators.Generators
import models.Index
import models.reference.PackageType
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageShippingMarkPage, PackageTypePage}
import pages.sections.PackagingSection
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import services.ReferenceDataService

import scala.concurrent.Future

class PackagingTransformerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val transformer = app.injector.instanceOf[PackagingTransformer]

  private lazy val mockReferenceDataService: ReferenceDataService = mock[ReferenceDataService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[ReferenceDataService].toInstance(mockReferenceDataService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockReferenceDataService)
  }

  "must transform data" in {

    val packagingType02: Seq[PackagingType02] = arbitrary[Seq[PackagingType02]].sample.value

    packagingType02.map {
      type0 =>
        when(mockReferenceDataService.getPackageType(eqTo(type0.typeOfPackages))(any()))
          .thenReturn(
            Future.successful(PackageType(type0.typeOfPackages, "describe me"))
          )
    }

    val result = transformer.transform(packagingType02, hcIndex, itemIndex).apply(emptyUserAnswers).futureValue

    packagingType02.zipWithIndex.map {
      case (packagingType0, i) =>
        val ind = Index(i)

        result.getSequenceNumber(PackagingSection(hcIndex, itemIndex, ind)) mustBe packagingType0.sequenceNumber
        result.getValue(PackageTypePage(hcIndex, itemIndex, ind)).code mustBe packagingType0.typeOfPackages
        result.getValue(PackageTypePage(hcIndex, itemIndex, ind)).description mustBe "describe me"
        result.get(PackageShippingMarkPage(hcIndex, itemIndex, ind)) mustBe packagingType0.shippingMarks
        result.get(NumberOfPackagesPage(hcIndex, itemIndex, ind)) mustBe packagingType0.numberOfPackages

    }

  }
}
