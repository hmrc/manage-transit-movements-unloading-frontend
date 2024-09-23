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

package models

import base.SpecBase
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import Identification._
import org.scalacheck.Gen

class IdentificationSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "Identification" - {

    "must convert into correct Identification type when xmlValue applied" - {

      "when 11 applied" in {
        val xmlValue = "11"
        Identification(xmlValue) mustBe SeaGoingVessel
      }
      "when 40 applied" in {
        val xmlValue = "40"
        Identification(xmlValue) mustBe IataFlightNumber
      }
      "when 81 applied" in {
        val xmlValue = "81"
        Identification(xmlValue) mustBe InlandWaterwaysVehicle
      }
      "when 10 applied" in {
        val xmlValue = "10"
        Identification(xmlValue) mustBe ImoShipIdNumber
      }
      "when 20 applied" in {
        val xmlValue = "20"
        Identification(xmlValue) mustBe WagonNumber
      }
      "when 21 applied" in {
        val xmlValue = "21"
        Identification(xmlValue) mustBe TrainNumber
      }
      "when 30 applied" in {
        val xmlValue = "30"
        Identification(xmlValue) mustBe RegNumberRoadVehicle
      }
      "when 31 applied" in {
        val xmlValue = "31"
        Identification(xmlValue) mustBe RegNumberRoadTrailer
      }
      "when 41 applied" in {
        val xmlValue = "41"
        Identification(xmlValue) mustBe RegNumberAircraft
      }
      "when 80 applied" in {
        val xmlValue = "80"
        Identification(xmlValue) mustBe EuropeanVesselIdNumber
      }
      "when 99 applied" in {
        val xmlValue = "99"
        Identification(xmlValue) mustBe Unknown
      }
      "when any other value applied" in {
        val xmlValue = Gen.alphaNumStr.sample.value
        Identification(xmlValue) mustBe Unknown
      }
    }

    "must have an associated message value" - {

      val messageKeyPrefix = Identification.messageKeyPrefix

      "when sea going vessel" in {
        val result = messages(s"$messageKeyPrefix.${SeaGoingVessel.toString}")
        result mustBe "Name of a sea-going vessel"
      }
      "when IATA flight number" in {
        val result = messages(s"$messageKeyPrefix.${IataFlightNumber.toString}")
        result mustBe "IATA flight number"
      }
      "when inland waterways vehicle" in {
        val result = messages(s"$messageKeyPrefix.${InlandWaterwaysVehicle.toString}")
        result mustBe "Name of an inland waterways vehicle"
      }
      "when IMO ship identification number" in {
        val result = messages(s"$messageKeyPrefix.${ImoShipIdNumber.toString}")
        result mustBe "IMO ship identification number"
      }
      "when wagon number" in {
        val result = messages(s"$messageKeyPrefix.${WagonNumber.toString}")
        result mustBe "Wagon number"
      }
      "when train number" in {
        val result = messages(s"$messageKeyPrefix.${TrainNumber.toString}")
        result mustBe "Train number"
      }
      "when reg number of road vehicle" in {
        val result = messages(s"$messageKeyPrefix.${RegNumberRoadVehicle.toString}")
        result mustBe "Registration number of a road vehicle"
      }
      "when reg number of road trailer" in {
        val result = messages(s"$messageKeyPrefix.${RegNumberRoadTrailer.toString}")
        result mustBe "Registration number of a road trailer"
      }
      "when reg number of aircraft" in {
        val result = messages(s"$messageKeyPrefix.${RegNumberAircraft.toString}")
        result mustBe "Registration number of an aircraft"
      }
      "when european vessel identification number" in {
        val result = messages(s"$messageKeyPrefix.${EuropeanVesselIdNumber.toString}")
        result mustBe "European vessel identification number (ENI code)"
      }
      "when unknown" in {
        val result = messages(s"$messageKeyPrefix.${Unknown.toString}")
        result mustBe "Unknown - this can only be used during the transitional period"
      }
    }
  }
}
