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

import cats.syntax.all._
import com.lucidchart.open.xtract.{__, XmlReader}

case class TraderAtDestination(
  eori: String,
  name: String,
  streetAndNumber: String,
  postCode: String,
  city: String,
  countryCode: String
)

object TraderAtDestination {

  implicit val xmlReader: XmlReader[TraderAtDestination] = (
    (__ \ "TINTRD59").read[String],
    (__ \ "NamTRD7").read[String],
    (__ \ "StrAndNumTRD22").read[String],
    (__ \ "PosCodTRD23").read[String],
    (__ \ "CitTRD24").read[String],
    (__ \ "CouTRD25").read[String]
  ).mapN(apply)

  object Constants {
    val eoriLength            = 17
    val nameLength            = 35
    val streetAndNumberLength = 35
    val postCodeLength        = 9
    val cityLength            = 35
    val countryCodeLength     = 2
  }

  implicit def writes: XMLWrites[TraderAtDestination] = XMLWrites[TraderAtDestination] {
    trader =>
      val TraderAtDestination(eori, name, streetAndNumber, postCode, city, countryCode) = trader

      <TRADESTRD>
        <NamTRD7>{name}</NamTRD7>
        <StrAndNumTRD22>{streetAndNumber}</StrAndNumTRD22>
        <PosCodTRD23>{postCode}</PosCodTRD23>
        <CitTRD24>{city}</CitTRD24>
        <CouTRD25>{countryCode}</CouTRD25>
        <NADLNGRD>{LanguageCodeEnglish.code}</NADLNGRD>
        <TINTRD59>{eori}</TINTRD59>
      </TRADESTRD>
  }
}
