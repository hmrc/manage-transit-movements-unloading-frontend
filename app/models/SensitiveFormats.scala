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

import generated.CC043CType
import play.api.libs.json._
import scalaxb.`package`.fromXML
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import generated.Generated_CC043CTypeFormat

import scala.util.{Failure, Success, Try}
import scala.xml.XML

class SensitiveFormats(encryptionEnabled: Boolean)(implicit crypto: Encrypter & Decrypter) {

  val jsObjectReads: Reads[JsObject] =
    JsonEncryption.sensitiveDecrypter(SensitiveString.apply).map(_.decrypt) orElse
      implicitly[Reads[JsObject]]

  val jsObjectWrites: Writes[JsObject] =
    jsObjectWrites(encryptionEnabled)

  def jsObjectWrites(encryptionEnabled: Boolean): Writes[JsObject] =
    if (encryptionEnabled) {
      JsonEncryption.sensitiveEncrypter[String, SensitiveString].contramap(_.encrypt)
    } else {
      SensitiveFormats.nonSensitiveJsObjectWrites
    }

  val cc043cReads: Reads[CC043CType] = {
    def parseXml(xml: String): CC043CType = fromXML[CC043CType](XML.loadString(xml))

    implicitly[Reads[String]].flatMap {
      xml =>
        Try(parseXml(xml)) match {
          case Success(value) =>
            Reads {
              _ => JsSuccess(value)
            }
          case Failure(_) =>
            JsonEncryption.sensitiveDecrypter(SensitiveString.apply).map(_.decryptedValue).map(parseXml)
        }
    }
  }

  val cc043cWrites: Writes[CC043CType] =
    if (encryptionEnabled) {
      JsonEncryption.sensitiveEncrypter[String, SensitiveString].contramap {
        cc034cType =>
          SensitiveString(cc034cType.toXML.toString())
      }
    } else {
      SensitiveFormats.nonSensitiveCc043cTypeWrites
    }
}

object SensitiveFormats {

  case class SensitiveWrites(
    jsObjectWrites: Writes[JsObject],
    cc043cTypeWrites: Writes[CC043CType]
  )

  object SensitiveWrites {

    def apply(): SensitiveWrites =
      new SensitiveWrites(nonSensitiveJsObjectWrites, nonSensitiveCc043cTypeWrites)

    def apply(sensitiveFormats: SensitiveFormats) =
      new SensitiveWrites(sensitiveFormats.jsObjectWrites, sensitiveFormats.cc043cWrites)
  }

  val nonSensitiveJsObjectWrites: Writes[JsObject] = implicitly[Writes[JsObject]]

  val nonSensitiveCc043cTypeWrites: Writes[CC043CType] = Writes {
    cc034cType =>
      JsString(cc034cType.toXML.toString())
  }
}
