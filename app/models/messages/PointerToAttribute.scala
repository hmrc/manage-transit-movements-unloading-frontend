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

import com.lucidchart.open.xtract.{__, XmlReader}
import models.XMLWrites

//TODO: Can we get rid of PointerToAttribute (use PointerIdentity)
case class PointerToAttribute(pointer: PointerIdentity)

object PointerToAttribute {

  implicit val writes: XMLWrites[PointerToAttribute] =
    XMLWrites(
      pointerToAttribute => <PoiToTheAttTOC5>{pointerToAttribute.pointer.value}</PoiToTheAttTOC5>
    )

  implicit val reads: XmlReader[PointerToAttribute] = (__ \ "PoiToTheAttTOC5").read[String] map {
    case TransportIdentity.value => PointerToAttribute(TransportIdentity)
    case TransportCountry.value  => PointerToAttribute(TransportCountry)
    case NumberOfItems.value     => PointerToAttribute(NumberOfItems)
    case NumberOfPackages.value  => PointerToAttribute(NumberOfPackages)
    case GrossMass.value         => PointerToAttribute(GrossMass)
  }

}

sealed trait PointerIdentity extends Serializable {
  val value: String
}

object PointerIdentity {

  val implementations: Seq[PointerIdentity] = Seq(
    TransportIdentity,
    TransportCountry,
    NumberOfItems,
    NumberOfPackages,
    GrossMass
  )
}

object TransportIdentity extends PointerIdentity {
  val value = "18#1"
}

object TransportCountry extends PointerIdentity {
  val value = "18#2"
}

object NumberOfItems extends PointerIdentity {
  val value = "5"
}

object NumberOfPackages extends PointerIdentity {
  val value = "6"
}

object GrossMass extends PointerIdentity {
  val value = "35"
}
