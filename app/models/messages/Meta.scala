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

package models.messages

import java.time.{LocalDate, LocalTime}

import com.lucidchart.open.xtract.{__, XmlReader}
import cats.syntax.all._
import models.XMLWrites
import models.XMLWrites._
import models.XMLReads._
import utils.Format

import scala.xml.NodeSeq

case class Meta(interchangeControlReference: InterchangeControlReference,
                dateOfPreparation: LocalDate,
                timeOfPreparation: LocalTime,
                senderIdentificationCodeQualifier: Option[String] = None,
                recipientIdentificationCodeQualifier: Option[String] = None,
                recipientsReferencePassword: Option[String] = None,
                recipientsReferencePasswordQualifier: Option[String] = None,
                priority: Option[String] = None,
                acknowledgementRequest: Option[String] = None,
                communicationsAgreementId: Option[String] = None,
                commonAccessReference: Option[String] = None,
                messageSequenceNumber: Option[String] = None,
                firstAndLastTransfer: Option[String] = None
)

object Meta {

  implicit def writes: XMLWrites[Meta] =
    XMLWrites(
      a =>
        <SynIdeMES1>UNOC</SynIdeMES1>
          <SynVerNumMES2>3</SynVerNumMES2> ++ {
          a.senderIdentificationCodeQualifier.fold(NodeSeq.Empty) {
            senderIdentificationCodeQualifier =>
              <SenIdeCodQuaMES4>{senderIdentificationCodeQualifier}</SenIdeCodQuaMES4>
          }
        } ++ {
          a.recipientIdentificationCodeQualifier.fold(NodeSeq.Empty) {
            recipientIdentificationCodeQualifier =>
              <RecIdeCodQuaMES7>{recipientIdentificationCodeQualifier}</RecIdeCodQuaMES7>
          }
        } ++
          <MesRecMES6>NCTS</MesRecMES6>
            <DatOfPreMES9>{Format.dateFormatted(a.dateOfPreparation)}</DatOfPreMES9>
            <TimOfPreMES10>{Format.timeFormatted(a.timeOfPreparation)}</TimOfPreMES10> ++ {
            a.interchangeControlReference.toXml
          } ++ {
            a.recipientsReferencePassword.fold(NodeSeq.Empty) {
              recipientsReferencePassword =>
                <RecRefMES12>{recipientsReferencePassword}</RecRefMES12>
            }
          } ++ {
            a.recipientsReferencePasswordQualifier.fold(NodeSeq.Empty) {
              recipientsReferencePasswordQualifier =>
                <RecRefQuaMES13>{recipientsReferencePasswordQualifier}</RecRefQuaMES13>
            }
          } ++
          <AppRefMES14>NCTS</AppRefMES14> ++ {

            a.priority.fold(NodeSeq.Empty) {
              priority =>
                <PriMES15>{priority}</PriMES15>
            }
          } ++ {

            a.acknowledgementRequest.fold(NodeSeq.Empty) {
              acknowledgementRequest =>
                <AckReqMES16>{acknowledgementRequest}</AckReqMES16>
            }
          } ++ {

            a.communicationsAgreementId.fold(NodeSeq.Empty) {
              communicationsAgreementId =>
                <ComAgrIdMES17>{communicationsAgreementId}</ComAgrIdMES17>
            }

          } ++
          <TesIndMES18>0</TesIndMES18>
            <MesIdeMES19>1</MesIdeMES19>
            <MesTypMES20>GB044A</MesTypMES20> ++ {

            a.commonAccessReference.fold(NodeSeq.Empty) {
              commonAccessReference =>
                <ComAccRefMES21>{commonAccessReference}</ComAccRefMES21>
            }
          } ++ {

            a.messageSequenceNumber.fold(NodeSeq.Empty) {
              messageSequenceNumber =>
                <MesSeqNumMES22>{messageSequenceNumber}</MesSeqNumMES22>
            }
          } ++ {

            a.firstAndLastTransfer.fold(NodeSeq.Empty) {
              firstAndLastTransfer =>
                <FirAndLasTraMES23>{firstAndLastTransfer}</FirAndLasTraMES23>
            }
          }
    )

  implicit val reads: XmlReader[Meta] = (
    (__ \ "IntConRefMES11").read[InterchangeControlReference],
    (__ \ "DatOfPreMES9").read[LocalDate],
    (__ \ "TimOfPreMES10").read[LocalTime],
    (__ \ "SenIdeCodQuaMES4").read[String].optional,
    (__ \ "RecIdeCodQuaMES7").read[String].optional,
    (__ \ "RecRefMES12").read[String].optional,
    (__ \ "RecRefQuaMES13").read[String].optional,
    (__ \ "PriMES15").read[String].optional,
    (__ \ "AckReqMES16").read[String].optional,
    (__ \ "ComAgrIdMES17").read[String].optional,
    (__ \ "ComAccRefMES21").read[String].optional,
    (__ \ "MesSeqNumMES22").read[String].optional,
    (__ \ "FirAndLasTraMES23").read[String].optional
  ).mapN(apply)

}
