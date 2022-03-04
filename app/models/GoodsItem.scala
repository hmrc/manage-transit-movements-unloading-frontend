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

import cats.data.NonEmptyList
import com.lucidchart.open.xtract.{__, XmlReader}
import com.lucidchart.open.xtract.XmlReader._
import cats.syntax.all._
import xml.NonEmptyListOps

import scala.xml.{Elem, NodeSeq}
import models.XMLWrites._

final case class GoodsItem(
  itemNumber: Int,
  commodityCode: Option[String],
  description: String,
  grossMass: Option[String],
  netMass: Option[String],
  producedDocuments: Seq[ProducedDocument],
  containers: Seq[String],
  packages: NonEmptyList[Packages],
  sensitiveGoodsInformation: Seq[SensitiveGoodsInformation]
)

object GoodsItem {

  val commodityCodeLength = 22
  val descriptionLength   = 280
  val containerLength     = 17
  val maxDocuments        = 99
  val maxContainers       = 99
  val maxPackages         = 99
  val maxSensitiveGoods   = 9

  implicit val xmlReader: XmlReader[GoodsItem] = (
    (__ \ "IteNumGDS7").read[Int],
    (__ \ "ComCodTarCodGDS10").read[String].optional,
    (__ \ "GooDesGDS23").read[String],
    (__ \ "GroMasGDS46").read[String].optional,
    (__ \ "NetMasGDS48").read[String].optional,
    (__ \ "PRODOCDC2").read(strictReadSeq[ProducedDocument]),
    (__ \ "CONNR2" \ "ConNumNR21").read(strictReadSeq[String]), //TODO:Check this is the correct node values
    (__ \ "PACGS2").read[NonEmptyList[Packages]](NonEmptyListOps.nonEmptyListReader),
    (__ \ "SGICODSD2").read(seq[SensitiveGoodsInformation])
  ).mapN(apply)

  implicit def writes: XMLWrites[GoodsItem] = XMLWrites[GoodsItem] {
    goodsItem =>
      val commodityCode = goodsItem.commodityCode.fold(NodeSeq.Empty) {
        commodityCode =>
          <ComCodTarCodGDS10>{commodityCode}</ComCodTarCodGDS10>
      }

      val grossMass = goodsItem.grossMass.fold(NodeSeq.Empty) {
        grossMass =>
          <GroMasGDS46>{grossMass}</GroMasGDS46>
      }

      val netMass = goodsItem.netMass.fold(NodeSeq.Empty) {
        netMass =>
          <NetMasGDS48>{netMass}</NetMasGDS48>
      }

      val containers: Seq[Elem] = goodsItem.containers.map {
        containerId =>
          <CONNR2>
            <ConNumNR21>{containerId}</ConNumNR21>
          </CONNR2>
      }

      <GOOITEGDS>
        <IteNumGDS7>{goodsItem.itemNumber}</IteNumGDS7>
        {commodityCode}
        <GooDesGDS23>{goodsItem.description}</GooDesGDS23>
        <GooDesGDS23LNG>EN</GooDesGDS23LNG>
        {grossMass}
        {netMass}
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
  }
}
