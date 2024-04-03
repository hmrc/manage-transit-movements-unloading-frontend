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

package models

import base.SpecBase
import generators.Generators
import models.reference.TransportMeansIdentification
import models.removable.TransportMeans
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TransportMeansSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must format as string" - {

    "when identification type and identification number defined" in {
      forAll(arbitrary[TransportMeansIdentification], Gen.alphaNumStr) {
        (identificationType, identificationNumber) =>
          val transportMeans = TransportMeans(Some(identificationType), Some(identificationNumber))
          val result         = transportMeans.asString
          result.value mustBe s"${identificationType.toString} - $identificationNumber"
      }
    }

    "when identification type defined" in {
      forAll(arbitrary[TransportMeansIdentification]) {
        identificationType =>
          val transportMeans = TransportMeans(Some(identificationType), None)
          val result         = transportMeans.asString
          result.value mustBe identificationType.toString
      }
    }

    "when identification number defined" in {
      forAll(Gen.alphaNumStr) {
        identificationNumber =>
          val transportMeans = TransportMeans(None, Some(identificationNumber))
          val result         = transportMeans.asString
          result.value mustBe identificationNumber
      }
    }

    "when nothing defined" in {
      val transportMeans = TransportMeans(None, None)
      val result         = transportMeans.asString
      result mustBe None
    }
  }
}
