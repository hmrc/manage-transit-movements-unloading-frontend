/*
 * Copyright 2024 HM Revenue & Customs
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

package services.submission

import base.{AppWithDefaultMockFixtures, SpecBase}
import generated._
import generators.Generators
import models.UnloadingType
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.transportEquipment.index.ItemPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import scalaxb.XMLCalendar
import services.DateTimeService

import java.time.{LocalDate, LocalDateTime}

class SubmissionServiceSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val service = app.injector.instanceOf[SubmissionService]

  private lazy val mockDateTimeService              = mock[DateTimeService]
  private lazy val mockMessageIdentificationService = mock[MessageIdentificationService]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[DateTimeService].toInstance(mockDateTimeService),
        bind[MessageIdentificationService].toInstance(mockMessageIdentificationService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDateTimeService)
    reset(mockMessageIdentificationService)

    when(mockDateTimeService.currentDateTime)
      .thenReturn(LocalDateTime.of(2020, 1, 1, 9, 30, 0))

    when(mockMessageIdentificationService.randomIdentifier)
      .thenReturn("foo")
  }

  "messageSequence" - {
    "must create message sequence" - {
      "when GB office of destination" in {
        val result = service.messageSequence(eoriNumber, "GB00001")

        result mustBe MESSAGESequence(
          messageSender = eoriNumber.value,
          messagE_1Sequence2 = MESSAGE_1Sequence(
            messageRecipient = "NTA.GB",
            preparationDateAndTime = XMLCalendar("2020-01-01T09:30:00"),
            messageIdentification = "foo"
          ),
          messagE_TYPESequence3 = MESSAGE_TYPESequence(
            messageType = CC044C
          ),
          correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
            correlationIdentifier = None
          )
        )
      }

      "when XI office of destination" in {
        val result = service.messageSequence(eoriNumber, "XI00001")

        result mustBe MESSAGESequence(
          messageSender = eoriNumber.value,
          messagE_1Sequence2 = MESSAGE_1Sequence(
            messageRecipient = "NTA.XI",
            preparationDateAndTime = XMLCalendar("2020-01-01T09:30:00"),
            messageIdentification = "foo"
          ),
          messagE_TYPESequence3 = MESSAGE_TYPESequence(
            messageType = CC044C
          ),
          correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
            correlationIdentifier = None
          )
        )
      }
    }
  }

  "transitOperationReads" - {
    "must create transit operation" - {
      import pages.OtherThingsToReportPage

      "when there are other things to report" in {
        forAll(Gen.alphaNumStr) {
          otherThingsToReport =>
            val userAnswers = emptyUserAnswers
              .setValue(OtherThingsToReportPage, otherThingsToReport)

            val reads  = service.transitOperationReads(userAnswers)
            val result = userAnswers.data.as[TransitOperationType15](reads)

            result mustBe TransitOperationType15(
              MRN = userAnswers.mrn.value,
              otherThingsToReport = Some(otherThingsToReport)
            )
        }
      }

      "when there are no other things to report" in {
        val userAnswers = emptyUserAnswers

        val reads  = service.transitOperationReads(userAnswers)
        val result = userAnswers.data.as[TransitOperationType15](reads)

        result mustBe TransitOperationType15(
          MRN = userAnswers.mrn.value,
          otherThingsToReport = None
        )
      }
    }
  }

  "unloadingRemarkReads" - {
    "must create unloading remarks" - {
      import pages._

      "when there are seal numbers in the IE043 message" - {
        "when seals can be read and no seals broken" in {
          forAll(Gen.alphaNumStr) {
            unloadingRemark =>
              val userAnswers = emptyUserAnswers
                .setValue(UnloadingTypePage, UnloadingType.Fully)
                .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                .setValue(CanSealsBeReadPage, true)
                .setValue(AreAnySealsBrokenPage, false)
                .setValue(AddUnloadingCommentsYesNoPage, true)
                .setValue(UnloadingCommentsPage, unloadingRemark)

              val reads  = service.unloadingRemarkReads
              val result = userAnswers.data.as[UnloadingRemarkType](reads)

              result mustBe UnloadingRemarkType(
                conform = Number0,
                unloadingCompletion = Number1,
                unloadingDate = XMLCalendar("2020-01-01"),
                stateOfSeals = Some(Number1),
                unloadingRemark = Some(unloadingRemark)
              )
          }
        }

        "when seals can be read and seals broken" in {
          forAll(Gen.alphaNumStr) {
            unloadingRemark =>
              val userAnswers = emptyUserAnswers
                .setValue(UnloadingTypePage, UnloadingType.Fully)
                .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                .setValue(CanSealsBeReadPage, true)
                .setValue(AreAnySealsBrokenPage, true)
                .setValue(AddUnloadingCommentsYesNoPage, true)
                .setValue(UnloadingCommentsPage, unloadingRemark)

              val reads  = service.unloadingRemarkReads
              val result = userAnswers.data.as[UnloadingRemarkType](reads)

              result mustBe UnloadingRemarkType(
                conform = Number0,
                unloadingCompletion = Number1,
                unloadingDate = XMLCalendar("2020-01-01"),
                stateOfSeals = Some(Number0),
                unloadingRemark = Some(unloadingRemark)
              )
          }
        }

        "when seals can't be read and seals broken" in {
          forAll(Gen.alphaNumStr) {
            unloadingRemark =>
              val userAnswers = emptyUserAnswers
                .setValue(UnloadingTypePage, UnloadingType.Fully)
                .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                .setValue(CanSealsBeReadPage, false)
                .setValue(AreAnySealsBrokenPage, true)
                .setValue(AddUnloadingCommentsYesNoPage, true)
                .setValue(UnloadingCommentsPage, unloadingRemark)

              val reads  = service.unloadingRemarkReads
              val result = userAnswers.data.as[UnloadingRemarkType](reads)

              result mustBe UnloadingRemarkType(
                conform = Number0,
                unloadingCompletion = Number1,
                unloadingDate = XMLCalendar("2020-01-01"),
                stateOfSeals = Some(Number0),
                unloadingRemark = Some(unloadingRemark)
              )
          }
        }

        "when seals can't be read and no seals broken" in {
          forAll(Gen.alphaNumStr) {
            unloadingRemark =>
              val userAnswers = emptyUserAnswers
                .setValue(UnloadingTypePage, UnloadingType.Fully)
                .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                .setValue(CanSealsBeReadPage, false)
                .setValue(AreAnySealsBrokenPage, false)
                .setValue(AddUnloadingCommentsYesNoPage, true)
                .setValue(UnloadingCommentsPage, unloadingRemark)

              val reads  = service.unloadingRemarkReads
              val result = userAnswers.data.as[UnloadingRemarkType](reads)

              result mustBe UnloadingRemarkType(
                conform = Number0,
                unloadingCompletion = Number1,
                unloadingDate = XMLCalendar("2020-01-01"),
                stateOfSeals = Some(Number0),
                unloadingRemark = Some(unloadingRemark)
              )
          }
        }
      }

      "when there are no seal numbers in the IE043 message" in {
        val userAnswers = emptyUserAnswers
          .setValue(UnloadingTypePage, UnloadingType.Partially)
          .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
          .setValue(AddUnloadingCommentsYesNoPage, false)

        val reads  = service.unloadingRemarkReads
        val result = userAnswers.data.as[UnloadingRemarkType](reads)

        result mustBe UnloadingRemarkType(
          conform = Number1,
          unloadingCompletion = Number0,
          unloadingDate = XMLCalendar("2020-01-01"),
          stateOfSeals = None,
          unloadingRemark = None
        )
      }
    }
  }

  "consignmentReads" - {
    "must create consignment" in {
      import pages.grossMass.GrossMassPage

      val grossMass = Gen.option(arbitrary[BigDecimal]).sample.value

      val userAnswers = emptyUserAnswers
        .setValue(GrossMassPage, grossMass)

      val reads  = service.consignmentReads
      val result = userAnswers.data.as[ConsignmentType06](reads)

      result.grossMass mustBe grossMass
    }

    "must create transport equipment" in {
      import pages.ContainerIdentificationNumberPage
      import pages.sections.transport.equipment.ItemSection
      import pages.sections.{SealSection, TransportEquipmentSection}
      import pages.transportEquipment.index.seals.SealIdentificationNumberPage

      val sequenceNumber                = Gen.alphaNumStr.sample.value
      val containerIdentificationNumber = Gen.option(Gen.alphaNumStr).sample.value
      val sealSequenceNumber            = Gen.alphaNumStr.sample.value
      val sealIdentifier                = Gen.alphaNumStr.sample.value
      val goodsReferenceSequenceNumber  = Gen.alphaNumStr.sample.value
      val declarationGoodsItemNumber    = arbitrary[BigInt].sample.value

      val userAnswers = emptyUserAnswers
        .setSequenceNumber(TransportEquipmentSection(index), sequenceNumber)
        .setValue(ContainerIdentificationNumberPage(index), containerIdentificationNumber)
        .setSequenceNumber(SealSection(index, sealIndex), sealSequenceNumber)
        .setValue(SealIdentificationNumberPage(index, sealIndex), sealIdentifier)
        .setSequenceNumber(ItemSection(index, itemIndex), goodsReferenceSequenceNumber)
        .setValue(ItemPage(index, sealIndex), declarationGoodsItemNumber)

      val reads  = service.consignmentReads
      val result = userAnswers.data.as[ConsignmentType06](reads).TransportEquipment

      result.size mustBe 1
      result.head.sequenceNumber mustBe sequenceNumber
      result.head.containerIdentificationNumber mustBe containerIdentificationNumber
      result.head.numberOfSeals.value mustBe 1
      result.head.Seal mustBe Seq(
        SealType02(sealSequenceNumber, sealIdentifier)
      )
      result.head.GoodsReference mustBe Seq(
        GoodsReferenceType01(goodsReferenceSequenceNumber, declarationGoodsItemNumber)
      )
    }
  }
}
