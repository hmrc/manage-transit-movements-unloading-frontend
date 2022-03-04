/*
 * Copyright 2022 HM Revenue & Customs
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
import generators.MessagesModelGenerators
import models.XMLWrites._
import org.scalacheck.Arbitrary._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.Utility.trim
import scala.xml.{Node, NodeSeq}

class UnloadingRemarksRequestSpec
    extends AnyFreeSpec
    with Matchers
    with MessagesModelGenerators
    with ScalaCheckPropertyChecks
    with StreamlinedXmlEquality
    with OptionValues {

  "UnloadingRemarksRequestSpec" - {

    "must serialise UnloadingRemarks to xml" in {

      forAll(arbitrary[UnloadingRemarksRequest]) {
        unloadingRemarksRequest =>
          val expectedResult: Node =
            <CC044A>
              {unloadingRemarksRequest.meta.toXml}
              {unloadingRemarksRequest.header.toXml}
              {unloadingRemarksRequest.traderAtDestination.toXml}
              <CUSOFFPREOFFRES>
                <RefNumRES1>{unloadingRemarksRequest.presentationOffice}</RefNumRES1>
              </CUSOFFPREOFFRES>
              {unloadingRemarkNode(unloadingRemarksRequest.unloadingRemark)}
              {resultOfControlNode(unloadingRemarksRequest.resultOfControl)}
              {unloadingRemarksRequest.seals.map(_.toXml).getOrElse(NodeSeq.Empty)}
            </CC044A>

          unloadingRemarksRequest.toXml.map(trim) mustBe expectedResult.map(trim)
      }

    }

    "must de-serialise xml to UnloadingRemarks" in {

      forAll(arbitrary[UnloadingRemarksRequest]) {
        unloadingRemarksRequest =>
          val result = XmlReader.of[UnloadingRemarksRequest].read(unloadingRemarksRequest.toXml)
          result.toOption.value mustBe unloadingRemarksRequest
      }

    }

  }

  private def unloadingRemarkNode(unloadingRemark: Remarks): NodeSeq = unloadingRemark match {
    case remarksConform: RemarksConform                   => remarksConform.toXml
    case remarksConformWithSeals: RemarksConformWithSeals => remarksConformWithSeals.toXml
    case remarksNonConform: RemarksNonConform             => remarksNonConform.toXml
  }

  def resultOfControlNode(resultsOfControl: Seq[ResultsOfControl]): NodeSeq =
    resultsOfControl.flatMap {
      case y: ResultsOfControlOther           => y.toXml
      case y: ResultsOfControlDifferentValues => y.toXml
    }
}
