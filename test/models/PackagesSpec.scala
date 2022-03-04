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
import models.messages.escapeXml
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.StreamlinedXmlEquality
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.Utility.trim
import scala.xml.{Node, NodeSeq}

class PackagesSpec extends AnyFreeSpec with Matchers with Generators with ScalaCheckPropertyChecks with StreamlinedXmlEquality {

  "Packages" - {

    "must serialise packages from Xml" in {

      forAll(arbitrary[Packages]) {

        packages =>
          val marksAndNumberPackage = packages.marksAndNumberPackage
            .map {
              marksAndNumber =>
                <MarNumOfPacGS21>
                  {marksAndNumber}
                </MarNumOfPacGS21>
            }

          val numberOfPackage = packages.numberOfPackages
            .map {
              number =>
                <NumOfPacGS24>
                  {number}
                </NumOfPacGS24>
            }

          val numberOfPieces = packages.numberOfPieces
            .map {
              number =>
                <NumOfPieGS25>
                  {number}
                </NumOfPieGS25>
            }

          val expectedResult =
            <PACGS2>
              {marksAndNumberPackage.getOrElse(NodeSeq.Empty)}
              <KinOfPacGS23>
                {packages.kindOfPackage}
              </KinOfPacGS23>
              {numberOfPackage.getOrElse(NodeSeq.Empty)}
              {numberOfPieces.getOrElse(NodeSeq.Empty)}
            </PACGS2>

          XmlReader.of[Packages].read(trim(expectedResult)) mustBe
            ParseSuccess(Packages(packages.marksAndNumberPackage, packages.kindOfPackage, packages.numberOfPackages, packages.numberOfPieces))
      }
    }

    "must serialise packages to Xml" in {

      forAll(arbitrary[Packages]) {
        packages =>
          val marksAndNumberPackage = packages.marksAndNumberPackage
            .map {
              marksAndNumber =>
                <MarNumOfPacGS21>{marksAndNumber}</MarNumOfPacGS21>
                  <MarNumOfPacGS21LNG>EN</MarNumOfPacGS21LNG>
            }

          val numberOfPackage = packages.numberOfPackages
            .map {
              number =>
                <NumOfPacGS24>{number}</NumOfPacGS24>
            }

          val numberOfPieces = packages.numberOfPieces
            .map {
              number =>
                <NumOfPieGS25>{number}</NumOfPieGS25>
            }

          val expectedResult: Node =
            <PACGS2>
              {marksAndNumberPackage.getOrElse(NodeSeq.Empty)}
              <KinOfPacGS23>{escapeXml(packages.kindOfPackage)}</KinOfPacGS23>
              {numberOfPackage.getOrElse(NodeSeq.Empty)}
              {numberOfPieces.getOrElse(NodeSeq.Empty)}
            </PACGS2>

          packages.toXml.map(trim) mustBe expectedResult.map(trim)
      }
    }
  }
}
