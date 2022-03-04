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
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.NodeSeq

class HeaderSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality with OptionValues {

  "HeaderSpec" - {

    "must serialize Header to xml" in {
      forAll(arbitrary[Header]) {
        header =>
          val transportIdentity = header.transportIdentity.map(
            transportIdentity => <IdeOfMeaOfTraAtDHEA78>{escapeXml(transportIdentity)}</IdeOfMeaOfTraAtDHEA78>
            <IdeOfMeaOfTraAtDHEA78LNG>EN</IdeOfMeaOfTraAtDHEA78LNG>
          )

          val transportCountry = header.transportCountry.map(
            transportCountry => <NatOfMeaOfTraAtDHEA80>{escapeXml(transportCountry)}</NatOfMeaOfTraAtDHEA80>
          )

          val expectedResult: NodeSeq =
            <HEAHEA>
            <DocNumHEA5>{escapeXml(header.movementReferenceNumber)}</DocNumHEA5>
            {transportIdentity.getOrElse(NodeSeq.Empty)}
            {transportCountry.getOrElse(NodeSeq.Empty)}
            <TotNumOfIteHEA305>{header.numberOfItems}</TotNumOfIteHEA305>
            {
              header.numberOfPackages.fold(NodeSeq.Empty) {
                numberOfPackages =>
                  <TotNumOfPacHEA306>{numberOfPackages}</TotNumOfPacHEA306>
              }
            }
            <TotGroMasHEA307>{escapeXml(header.grossMass)}</TotGroMasHEA307>
          </HEAHEA>

          header.toXml mustEqual expectedResult
      }

    }

    "must deserialize Xml to Header" in {
      forAll(arbitrary[Header]) {
        header =>
          val result = XmlReader.of[Header].read(header.toXml).toOption.value

          result mustBe header
      }

    }

  }

}
