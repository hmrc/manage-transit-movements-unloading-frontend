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

package models.messages

import generators.{Generators, ModelGenerators}
import models.XMLWrites._
import org.scalacheck.Arbitrary._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.Node
import scala.xml.Utility.trim

class ControlIndicatorSpec extends AnyFreeSpec with Matchers with Generators with ModelGenerators with ScalaCheckPropertyChecks {

  "ControlIndicatorSpec" - {
    "must serialize ControlIndicator to xml" in {

      forAll(arbitrary[ControlIndicator]) {
        controlIndicator =>
          val xml: Node = <ConInd424>{controlIndicator.indicator.value}</ConInd424>
          controlIndicator.toXml.map(trim) mustBe xml.map(trim)
      }
    }

  }

}
