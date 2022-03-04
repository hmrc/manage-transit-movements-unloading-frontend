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

import java.time.LocalDate

import cats.syntax.all._
import com.lucidchart.open.xtract.{__, XmlReader}
import models.XMLReads._
import models.{LanguageCodeEnglish, XMLWrites}
import utils.Format

import scala.xml.NodeSeq

sealed trait Remarks {
  val conform: String //1 if no unloading remarks, 0 if unloading remarks are present OR any seals changed/updated OR any data changed
  val unloadingCompleted: String = "1"
}

object Remarks {

  implicit lazy val xmlReader: XmlReader[Remarks] = XmlReader {
    xml =>
      val confirm     = __ \ "ConREM65"
      val stateOfSeal = __ \ "StaOfTheSeaOKREM19"

      (confirm(xml).text, stateOfSeal(xml).nonEmpty) match {
        case ("0", _)    => RemarksNonConform.xmlReader.read(xml)
        case ("1", true) => RemarksConformWithSeals.xmlReader.read(xml)
        case _           => RemarksConform.xmlReader.read(xml)
      }
  }
}

case class RemarksConform(unloadingDate: LocalDate, unloadingRemark: Option[String]) extends Remarks {
  val conform = "1"
}

object RemarksConform {

  implicit val writes: XMLWrites[RemarksConform] =
    XMLWrites {
      remarks =>
        val unloadingRemarks = remarks.unloadingRemark.map {
          remarks =>
            <UnlRemREM53>{remarks}</UnlRemREM53>
            <UnlRemREM53LNG>{LanguageCodeEnglish.code}</UnlRemREM53LNG>
        }

        <UNLREMREM>
        {unloadingRemarks.getOrElse(NodeSeq.Empty)}
        <ConREM65>{remarks.conform}</ConREM65>
        <UnlComREM66>{remarks.unloadingCompleted}</UnlComREM66>
        <UnlDatREM67>{Format.dateFormatted(remarks.unloadingDate)}</UnlDatREM67>
      </UNLREMREM>
    }

  implicit val xmlReader: XmlReader[RemarksConform] =
    (
      (__ \ "UnlDatREM67").read[LocalDate],
      (__ \ "UnlRemREM53").read[String].optional
    ) mapN apply
}

case class RemarksConformWithSeals(unloadingDate: LocalDate, unloadingRemark: Option[String]) extends Remarks {
  val conform      = "1"
  val stateOfSeals = "1"
}

object RemarksConformWithSeals {

  implicit val writes: XMLWrites[RemarksConformWithSeals] =
    XMLWrites {
      remarks =>
        val unloadingRemarks = remarks.unloadingRemark.map {
          remarks =>
            <UnlRemREM53>{remarks}</UnlRemREM53>
            <UnlRemREM53LNG>{LanguageCodeEnglish.code}</UnlRemREM53LNG>
        }

        <UNLREMREM>
        <StaOfTheSeaOKREM19>{remarks.stateOfSeals}</StaOfTheSeaOKREM19>
        {unloadingRemarks.getOrElse(NodeSeq.Empty)}
        <ConREM65>{remarks.conform}</ConREM65>
        <UnlComREM66>{remarks.unloadingCompleted}</UnlComREM66>
        <UnlDatREM67>{Format.dateFormatted(remarks.unloadingDate)}</UnlDatREM67>
      </UNLREMREM>

    }

  implicit val xmlReader: XmlReader[RemarksConformWithSeals] =
    (
      (__ \ "UnlDatREM67").read[LocalDate],
      (__ \ "UnlRemREM53").read[String].optional
    ) mapN apply
}

case class RemarksNonConform(
  stateOfSeals: Option[Int],
  unloadingRemark: Option[String],
  unloadingDate: LocalDate
) extends Remarks {
  val conform = "0"
}

object RemarksNonConform {

  val unloadingRemarkLength  = 350
  val resultsOfControlLength = 9

  implicit val writes: XMLWrites[RemarksNonConform] =
    XMLWrites {
      remarks =>
        val stateOfSeals = remarks.stateOfSeals.map {
          int =>
            <StaOfTheSeaOKREM19>{int}</StaOfTheSeaOKREM19>
        }

        val unloadingRemarks = remarks.unloadingRemark.map {
          remarks =>
            <UnlRemREM53>{remarks}</UnlRemREM53>
          <UnlRemREM53LNG>{LanguageCodeEnglish.code}</UnlRemREM53LNG>
        }

        <UNLREMREM>
        {stateOfSeals.getOrElse(NodeSeq.Empty)}
        {unloadingRemarks.getOrElse(NodeSeq.Empty)}
        <ConREM65>{remarks.conform}</ConREM65>
        <UnlComREM66>{remarks.unloadingCompleted}</UnlComREM66>
        <UnlDatREM67>{Format.dateFormatted(remarks.unloadingDate)}</UnlDatREM67>
      </UNLREMREM>

    }

  implicit val xmlReader: XmlReader[RemarksNonConform] = (
    (__ \ "StaOfTheSeaOKREM19").read[Int].optional,
    (__ \ "UnlRemREM53").read[String].optional,
    (__ \ "UnlDatREM67").read[LocalDate]
  ) mapN apply
}
