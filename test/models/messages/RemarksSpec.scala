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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.Format

import scala.xml.Utility.trim
import scala.xml.{Elem, Node, NodeSeq}

class RemarksSpec extends AnyFreeSpec with Matchers with Generators with ModelGenerators with ScalaCheckPropertyChecks with OptionValues {

  "RemarksSpec" - {

    "must serialize" - {

      "RemarksConform to xml" in {

        forAll(arbitrary[RemarksConform]) {
          remarksConform =>
            val unloadingRemarks: Option[NodeSeq] = remarksConform.unloadingRemark.map {
              remarks =>
                <UnlRemREM53>{remarks}</UnlRemREM53> ++
                  <UnlRemREM53LNG>EN</UnlRemREM53LNG>
            }

            val xml: Node =
              <UNLREMREM>
                {unloadingRemarks.getOrElse(NodeSeq.Empty)}
                <ConREM65>1</ConREM65>
                <UnlComREM66>1</UnlComREM66>
                <UnlDatREM67>{Format.dateFormatted(remarksConform.unloadingDate)}</UnlDatREM67>
              </UNLREMREM>

            remarksConform.toXml.map(trim) mustBe xml.map(trim)
        }
      }

      "read xml as RemarksConform" in {

        forAll(arbitrary[RemarksConform]) {
          remarksConform =>
            val result = XmlReader.of[RemarksConform].read(remarksConform.toXml).toOption.value
            result mustBe remarksConform
        }
      }

      "RemarksConformWithSeals to xml" in {

        forAll(arbitrary[RemarksConformWithSeals]) {
          remarksConformWithSeals =>
            val unloadingRemarks: Option[NodeSeq] = remarksConformWithSeals.unloadingRemark.map {
              remarks =>
                <UnlRemREM53>{remarks}</UnlRemREM53> ++
                  <UnlRemREM53LNG>EN</UnlRemREM53LNG>
            }

            val xml: Node =
              <UNLREMREM>
                <StaOfTheSeaOKREM19>1</StaOfTheSeaOKREM19>
                {unloadingRemarks.getOrElse(NodeSeq.Empty)}
                <ConREM65>1</ConREM65>
                <UnlComREM66>1</UnlComREM66>
                <UnlDatREM67>{Format.dateFormatted(remarksConformWithSeals.unloadingDate)}</UnlDatREM67>
              </UNLREMREM>

            remarksConformWithSeals.toXml.map(trim) mustBe xml.map(trim)
        }
      }

      "read xml as RemarksConformWithSeals" in {
        forAll(arbitrary[RemarksConformWithSeals]) {
          remarksConformWithSeals =>
            val result = XmlReader.of[RemarksConformWithSeals].read(remarksConformWithSeals.toXml).toOption.value
            result mustBe remarksConformWithSeals
        }
      }

      "RemarksNonConform to xml" in {

        forAll(arbitrary[RemarksNonConform]) {
          remarksNonConform =>
            val stateOfSeals: Option[Elem] = remarksNonConform.stateOfSeals.map {
              int =>
                <StaOfTheSeaOKREM19>{int}</StaOfTheSeaOKREM19>
            }

            val unloadingRemarks = remarksNonConform.unloadingRemark.map {
              remarks =>
                <UnlRemREM53>{remarks}</UnlRemREM53> ++
                  <UnlRemREM53LNG>EN</UnlRemREM53LNG>
            }

            val xml: NodeSeq =
              <UNLREMREM>
                {stateOfSeals.getOrElse(NodeSeq.Empty)}
                {unloadingRemarks.getOrElse(NodeSeq.Empty)}
                <ConREM65>0</ConREM65>
                <UnlComREM66>1</UnlComREM66>
                <UnlDatREM67>{Format.dateFormatted(remarksNonConform.unloadingDate)}</UnlDatREM67>
              </UNLREMREM>

            remarksNonConform.toXml.map(trim) mustBe xml.map(trim)
        }
      }

      "read xml as RemarksNonConform" in {

        forAll(arbitrary[RemarksNonConform]) {
          remarksNonConform =>
            val result = XmlReader.of[RemarksNonConform].read(remarksNonConform.toXml).toOption.value
            result mustBe remarksNonConform
        }
      }
    }
  }
}

object RemarksSpec {

  def resultsOfControlNode(resultsOfControl: ResultsOfControl): Elem = resultsOfControl match {
    case resultsOfControl: ResultsOfControlOther =>
      <RESOFCON534>
        <DesTOC2>{resultsOfControl.description}</DesTOC2>
        <DesTOC2LNG>EN</DesTOC2LNG>
        <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
      </RESOFCON534>
    case resultsOfControl: ResultsOfControlDifferentValues =>
      <RESOFCON534>
        <ConInd424>{resultsOfControl.controlIndicator.indicator.value}</ConInd424>
        <PoiToTheAttTOC5>{resultsOfControl.pointerToAttribute.pointer.value}</PoiToTheAttTOC5>
        <CorValTOC4>{resultsOfControl.correctedValue}</CorValTOC4>
      </RESOFCON534>
  }
}
