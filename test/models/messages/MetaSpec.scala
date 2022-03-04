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

import java.time.LocalTime

import com.lucidchart.open.xtract.XmlReader
import generators.MessagesModelGenerators
import models.XMLWrites._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, StreamlinedXmlEquality}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import utils.Format
import utils.Format.timeFormatter

import scala.xml.NodeSeq

class MetaSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with MessagesModelGenerators with StreamlinedXmlEquality with OptionValues {

  //format off
  "MetaSpec" - {

    val syntaxIdentifier     = "UNOC"
    val syntaxVersionNumber  = "3"
    val messageRecipient     = "NCTS"
    val applicationReference = "NCTS"
    val messageIndication    = "1"
    val testIndicator        = "0"
    val messageCode          = "GB044A"

    "must serialize Meta to xml" in {
      forAll(arbitrary[Meta]) {
        meta =>
          val senderIdentificationCodeQualifier = meta.senderIdentificationCodeQualifier.fold(NodeSeq.Empty)(
            senderIdentification => <SenIdeCodQuaMES4>{senderIdentification}</SenIdeCodQuaMES4>
          )

          val recipientIdentificationCodeQualifier =
            meta.recipientIdentificationCodeQualifier.fold(NodeSeq.Empty)(
              recipientIdentification => <RecIdeCodQuaMES7>{recipientIdentification}</RecIdeCodQuaMES7>
            )

          val recipientsReferencePassword = meta.recipientsReferencePassword.fold(NodeSeq.Empty)(
            recipientsReferencePassword => <RecRefMES12>{recipientsReferencePassword}</RecRefMES12>
          )

          val recipientsReferencePasswordQualifier = meta.recipientsReferencePasswordQualifier.fold(NodeSeq.Empty)(
            recipientsReferencePasswordQualifier => <RecRefQuaMES13>{recipientsReferencePasswordQualifier}</RecRefQuaMES13>
          )

          val priority = meta.priority.fold(NodeSeq.Empty)(
            priority => <PriMES15>{priority}</PriMES15>
          )

          val acknowledgementRequest = meta.acknowledgementRequest.fold(NodeSeq.Empty)(
            acknowledgementRequest => <AckReqMES16>{acknowledgementRequest}</AckReqMES16>
          )

          val communicationsAgreementId = meta.communicationsAgreementId.fold(NodeSeq.Empty)(
            communicationsAgreementId => <ComAgrIdMES17>{communicationsAgreementId}</ComAgrIdMES17>
          )

          val commonAccessReference = meta.commonAccessReference.fold(NodeSeq.Empty)(
            commonAccessReference => <ComAccRefMES21>{commonAccessReference}</ComAccRefMES21>
          )

          val messageSequenceNumber = meta.messageSequenceNumber.fold(NodeSeq.Empty)(
            messageSequenceNumber => <MesSeqNumMES22>{messageSequenceNumber}</MesSeqNumMES22>
          )

          val firstAndLastTransfer = meta.firstAndLastTransfer.fold(NodeSeq.Empty)(
            firstAndLastTransfer => <FirAndLasTraMES23>{firstAndLastTransfer}</FirAndLasTraMES23>
          )

          val expectedResult: NodeSeq = {

            <SynIdeMES1>{syntaxIdentifier}</SynIdeMES1> ++
              <SynVerNumMES2>{syntaxVersionNumber}</SynVerNumMES2> ++ {
                senderIdentificationCodeQualifier ++
                  recipientIdentificationCodeQualifier
              } ++
              <MesRecMES6>{messageRecipient}</MesRecMES6> ++
              <DatOfPreMES9>{Format.dateFormatted(meta.dateOfPreparation)}</DatOfPreMES9> ++
              <TimOfPreMES10>{Format.timeFormatted(meta.timeOfPreparation)}</TimOfPreMES10> ++ {
                meta.interchangeControlReference.toXml ++
                  recipientsReferencePassword ++
                  recipientsReferencePasswordQualifier
              } ++
              <AppRefMES14>{applicationReference}</AppRefMES14> ++ {
                priority ++
                  acknowledgementRequest ++
                  communicationsAgreementId
              } ++
              <TesIndMES18>{testIndicator}</TesIndMES18> ++
              <MesIdeMES19>{messageIndication}</MesIdeMES19> ++ {
                <MesTypMES20>{messageCode}</MesTypMES20> ++
                  commonAccessReference ++
                  messageSequenceNumber ++
                  firstAndLastTransfer
              }
          }

          meta.toXml mustEqual expectedResult
      }
    }
    "must deserialize from xml" in {
      forAll(arbitrary[UnloadingRemarksRequest]) {
        unloadingRemarksRequest =>
          val xml    = unloadingRemarksRequest.toXml
          val result = XmlReader.of[Meta].read(xml).toOption.value

          val formattedTimeOfPrep = Format.timeFormatted(unloadingRemarksRequest.meta.timeOfPreparation)
          val formatMeta          = unloadingRemarksRequest.meta.copy(timeOfPreparation = LocalTime.parse(formattedTimeOfPrep, timeFormatter))

          result mustBe formatMeta
      }
    }
  }
  // format: on
}
