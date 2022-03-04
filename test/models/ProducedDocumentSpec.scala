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

package models

import com.lucidchart.open.xtract.{ParseSuccess, XmlReader}
import generators.Generators
import models.XMLWrites._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.StreamlinedXmlEquality
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.NodeSeq
import scala.xml.Utility.trim

class ProducedDocumentSpec extends AnyFreeSpec with Matchers with Generators with ScalaCheckPropertyChecks with StreamlinedXmlEquality {

  "ProducedDocument" - {

    "must serialise ProducedDocument from Xml" in {

      forAll(arbitrary[ProducedDocument]) {
        producedDocument =>
          val reference = producedDocument.reference.map {
            ref =>
              <DocRefDC23>
              {ref}
            </DocRefDC23>
          }

          val complementOfInformation = producedDocument.complementOfInformation.map {
            information =>
              <ComOfInfDC25>
                {information}
              </ComOfInfDC25>
          }

          val expectedResult =
            <PRODOCDC2>
              <DocTypDC21>
                {producedDocument.documentType}
              </DocTypDC21>
              {reference.getOrElse(NodeSeq.Empty)}
              {complementOfInformation.getOrElse(NodeSeq.Empty)}
            </PRODOCDC2>

          XmlReader.of[ProducedDocument].read(trim(expectedResult)) mustBe
            ParseSuccess(ProducedDocument(producedDocument.documentType, producedDocument.reference, producedDocument.complementOfInformation))
      }
    }

    "must serialise ProducedDocument to Xml" in {

      forAll(arbitrary[ProducedDocument]) {
        producedDocument =>
          val reference = producedDocument.reference.map {
            ref =>
              <DocRefDC23>{ref}</DocRefDC23>
                <DocRefDCLNG>EN</DocRefDCLNG>
          }

          val complementOfInformation = producedDocument.complementOfInformation.map {
            information =>
              <ComOfInfDC25>{information}</ComOfInfDC25>
                <ComOfInfDC25LNG>EN</ComOfInfDC25LNG>
          }

          val expectedResult =
            <PRODOCDC2>
              <DocTypDC21>
                {producedDocument.documentType}
              </DocTypDC21>
              {reference.getOrElse(NodeSeq.Empty)}
              {complementOfInformation.getOrElse(NodeSeq.Empty)}
            </PRODOCDC2>

          producedDocument.toXml mustEqual expectedResult
      }

    }

  }

}
