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

import com.lucidchart.open.xtract.XmlReader
import generators.{Generators, ModelGenerators}
import models.XMLWrites._
import org.scalacheck.Arbitrary._
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.Utility.trim

class ResultsOfControlSpec extends AnyFreeSpec with Matchers with Generators with ModelGenerators with ScalaCheckPropertyChecks with OptionValues {

  "ResultsOfControlSpec" - {

    "must serialize ResultsOfControl to xml" in {

      forAll(arbitrary[ResultsOfControl]) {
        case resultsOfControl: ResultsOfControlOther =>
          val xml = <RESOFCON534>
                    <DesTOC2>{resultsOfControl.description}</DesTOC2>
                    <DesTOC2LNG>EN</DesTOC2LNG>
                    <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
                  </RESOFCON534>

          resultsOfControl.toXml.map(trim) mustBe xml.map(trim)
        case resultsOfControl: ResultsOfControlDifferentValues =>
          val xml = <RESOFCON534>
                    <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
                    <PoiToTheAttTOC5>{resultsOfControl.pointerToAttribute.pointer.value}</PoiToTheAttTOC5>
                    <CorValTOC4>{resultsOfControl.correctedValue}</CorValTOC4>
                  </RESOFCON534>

          resultsOfControl.toXml.map(trim) mustBe xml.map(trim)

      }

    }

    "must read xml as ResultsOfControl" in {

      val resultsOfControl = arbitrary[ResultsOfControl].sample.get

      resultsOfControl match {
        case resultsOfControl: ResultsOfControlOther =>
          val xml = <RESOFCON534>
            <DesTOC2>{resultsOfControl.description}</DesTOC2>
            <DesTOC2LNG>EN</DesTOC2LNG>
            <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
          </RESOFCON534>

          val result = XmlReader.of[ResultsOfControlOther].read(xml)

          result.toOption.value mustEqual resultsOfControl
        case resultsOfControl: ResultsOfControlDifferentValues =>
          val xml = <RESOFCON534>
            <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
            <PoiToTheAttTOC5>{resultsOfControl.pointerToAttribute.pointer.value}</PoiToTheAttTOC5>
            <CorValTOC4>{resultsOfControl.correctedValue}</CorValTOC4>
          </RESOFCON534>

          val result = XmlReader.of[ResultsOfControlDifferentValues].read(xml)

          result.toOption.value mustEqual resultsOfControl
      }

    }

  }

}
