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

import com.lucidchart.open.xtract.{__, XmlReader}
import cats.syntax.all._

import scala.xml.NodeSeq

final case class ProducedDocument(
  documentType: String,
  reference: Option[String],
  complementOfInformation: Option[String]
)

object ProducedDocument {

  val documentTypeLength            = 4
  val referenceLength               = 35
  val complementOfInformationLength = 26

  implicit val xmlReader: XmlReader[ProducedDocument] = (
    (__ \ "DocTypDC21").read[String],
    (__ \ "DocRefDC23").read[String].optional,
    (__ \ "ComOfInfDC25").read[String].optional
  ).mapN(apply)

  implicit def writes: XMLWrites[ProducedDocument] = XMLWrites[ProducedDocument] {
    producedDocument =>
      <PRODOCDC2>
        <DocTypDC21>{producedDocument.documentType}</DocTypDC21>
        {
        producedDocument.reference.fold(NodeSeq.Empty) {
          reference =>
            <DocRefDC23>{reference}</DocRefDC23>
              <DocRefDCLNG>{LanguageCodeEnglish.code}</DocRefDCLNG>
        } ++
          producedDocument.complementOfInformation.fold(NodeSeq.Empty) {
            complementOfInformation =>
              <ComOfInfDC25>{complementOfInformation}</ComOfInfDC25>
              <ComOfInfDC25LNG>{LanguageCodeEnglish.code}</ComOfInfDC25LNG>
          }
      }

      </PRODOCDC2>
  }

}
