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

import com.lucidchart.open.xtract.{ParseSuccess, XmlReader}
import generators.Generators
import models.XMLWrites._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.StreamlinedXmlEquality
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.xml.Utility.trim
import scala.xml.{Elem, NodeSeq}

class GoodsItemSpec extends AnyFreeSpec with Matchers with Generators with ScalaCheckPropertyChecks with StreamlinedXmlEquality {

  import GoodsItemSpec._

  "GoodsItem" - {

    "must serialize GoodsItem from Xml" in {

      forAll(arbitrary[GoodsItem]) {

        goodsItem =>
          val commodityCode = goodsItem.commodityCode.map {
            code =>
              <ComCodTarCodGDS10>{code}</ComCodTarCodGDS10>
          }

          val grossMass: Option[Elem] = goodsItem.grossMass.map {
            grossMass =>
              <GroMasGDS46>{grossMass}</GroMasGDS46>
          }

          val netMass: Option[Elem] = goodsItem.netMass.map {
            netMass =>
              <NetMasGDS48>{netMass}</NetMasGDS48>
          }

          val containers: Seq[Elem] = goodsItem.containers.map {
            value =>
              <ConNumNR21>{value}</ConNumNR21>
          }

          val expectedResult =
            <GOOITEGDS>
              <IteNumGDS7>
                {goodsItem.itemNumber}
              </IteNumGDS7>
                {commodityCode.getOrElse(NodeSeq.Empty)}
              <GooDesGDS23>
                {goodsItem.description}
              </GooDesGDS23>
              {grossMass.getOrElse(NodeSeq.Empty)}
              {netMass.getOrElse(NodeSeq.Empty)}
              {producedDocument(goodsItem)}
              {
              containers.map {
                x => <CONNR2>{x}</CONNR2>
              }
            }
              {packages(goodsItem)}
              {sensitiveGoodsInformation(goodsItem)}
              </GOOITEGDS>

          XmlReader.of[GoodsItem].read(trim(expectedResult)) mustBe
            ParseSuccess(
              GoodsItem(
                goodsItem.itemNumber,
                goodsItem.commodityCode,
                goodsItem.description,
                goodsItem.grossMass,
                goodsItem.netMass,
                goodsItem.producedDocuments,
                goodsItem.containers,
                goodsItem.packages,
                goodsItem.sensitiveGoodsInformation
              )
            )
      }
    }

    "must serialize GoodsItem to Xml" in {

      forAll(arbitrary[GoodsItem]) {

        goodsItem =>
          val commodityCode = goodsItem.commodityCode.map {
            code =>
              <ComCodTarCodGDS10>{code}</ComCodTarCodGDS10>
          }

          val grossMass: Option[Elem] = goodsItem.grossMass.map {
            grossMass =>
              <GroMasGDS46>{grossMass}</GroMasGDS46>
          }

          val netMass: Option[Elem] = goodsItem.netMass.map {
            netMass =>
              <NetMasGDS48>{netMass}</NetMasGDS48>
          }

          val containers: Seq[Elem] = goodsItem.containers.map {
            value =>
              <CONNR2><ConNumNR21>{value}</ConNumNR21></CONNR2>
          }

          val expectedResult =
            <GOOITEGDS>
              <IteNumGDS7>
                {goodsItem.itemNumber}
              </IteNumGDS7>
              {commodityCode.getOrElse(NodeSeq.Empty)}
              <GooDesGDS23>
                {goodsItem.description}
              </GooDesGDS23>
              <GooDesGDS23LNG>EN</GooDesGDS23LNG>
              {grossMass.getOrElse(NodeSeq.Empty)}
              {netMass.getOrElse(NodeSeq.Empty)}
              {
              goodsItem.producedDocuments.map(
                x => x.toXml
              )
            }
              {containers}
              {goodsItem.packages.toList.map(_.toXml)}
              {
              goodsItem.sensitiveGoodsInformation.map(
                x => x.toXml
              )
            }
            </GOOITEGDS>

          //TODO: Strip off trim
          goodsItem.toXml.map(trim) mustBe expectedResult.map(trim)
      }
    }
  }
}

object GoodsItemSpec {

  private[models] def packages(goodsItem: GoodsItem) =
    goodsItem.packages.map {
      packages =>
        val marksAndNumberPackage = packages.marksAndNumberPackage
          .map {
            marksAndNumber =>
              <MarNumOfPacGS21>{marksAndNumber}</MarNumOfPacGS21>
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

        <PACGS2>
          {marksAndNumberPackage.getOrElse(NodeSeq.Empty)}
          <KinOfPacGS23>
            {packages.kindOfPackage}
          </KinOfPacGS23>
          {numberOfPackage.getOrElse(NodeSeq.Empty)}
          {numberOfPieces.getOrElse(NodeSeq.Empty)}
        </PACGS2>
    }.toList

  private[models] def producedDocument(goodsItem: GoodsItem) =
    goodsItem.producedDocuments.map {

      producedDocument =>
        val reference = producedDocument.reference.map {
          ref =>
            <DocRefDC23>{ref}</DocRefDC23>
        }

        val complementOfInformation = producedDocument.complementOfInformation.map {
          information =>
            <ComOfInfDC25>{information}</ComOfInfDC25>
        }

        <PRODOCDC2>
          <DocTypDC21>
            {producedDocument.documentType}
          </DocTypDC21>
          {reference.getOrElse(false)}
          {complementOfInformation.getOrElse(false)}
        </PRODOCDC2>
    }

  private[models] def sensitiveGoodsInformation(goodsItem: GoodsItem) =
    goodsItem.sensitiveGoodsInformation.map {
      sensitiveGoodsInformation =>
        val goodsCode = sensitiveGoodsInformation.goodsCode.map {
          code =>
            <SenGooCodSD22>{code}</SenGooCodSD22>
        }

        <SGICODSD2>
          {goodsCode.getOrElse(NodeSeq.Empty)}
          <SenQuaSD23>
            {sensitiveGoodsInformation.quantity}
          </SenQuaSD23>
        </SGICODSD2>
    }.toList
}
