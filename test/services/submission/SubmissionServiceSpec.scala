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
import models.{Index, UnloadingType}
import models.reference.AdditionalReferenceType
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
          messageRecipient = "NTA.GB",
          preparationDateAndTime = XMLCalendar("2020-01-01T09:30:00"),
          messageIdentification = "foo",
          messageType = CC044C,
          correlationIdentifier = None
        )
      }

      "when XI office of destination" in {
        val result = service.messageSequence(eoriNumber, "XI00001")

        result mustBe MESSAGESequence(
          messageSender = eoriNumber.value,
          messageRecipient = "NTA.XI",
          preparationDateAndTime = XMLCalendar("2020-01-01T09:30:00"),
          messageIdentification = "foo",
          messageType = CC044C,
          correlationIdentifier = None
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
        "when seals can be read and no seals broken" - {
          "and AddUnloadingCommentsYesNo is true" in {
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

          "and AddUnloadingCommentsYesNo is false" in {
            forAll(Gen.alphaNumStr) {
              unloadingRemark =>
                val userAnswers = emptyUserAnswers
                  .setValue(UnloadingTypePage, UnloadingType.Fully)
                  .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                  .setValue(CanSealsBeReadPage, true)
                  .setValue(AreAnySealsBrokenPage, false)
                  .setValue(AddUnloadingCommentsYesNoPage, false)
                  .setValue(UnloadingCommentsPage, unloadingRemark)

                val reads  = service.unloadingRemarkReads
                val result = userAnswers.data.as[UnloadingRemarkType](reads)

                result mustBe UnloadingRemarkType(
                  conform = Number1,
                  unloadingCompletion = Number1,
                  unloadingDate = XMLCalendar("2020-01-01"),
                  stateOfSeals = Some(Number1),
                  unloadingRemark = Some(unloadingRemark)
                )
            }
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

        "when there are no seal numbers in the IE043 message" - {
          "and AddUnloadingCommentsYesNo is false" in {
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

          "and AddUnloadingCommentsYesNo is true" in {
            val userAnswers = emptyUserAnswers
              .setValue(UnloadingTypePage, UnloadingType.Partially)
              .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
              .setValue(AddUnloadingCommentsYesNoPage, true)

            val reads  = service.unloadingRemarkReads
            val result = userAnswers.data.as[UnloadingRemarkType](reads)

            result mustBe UnloadingRemarkType(
              conform = Number0,
              unloadingCompletion = Number0,
              unloadingDate = XMLCalendar("2020-01-01"),
              stateOfSeals = None,
              unloadingRemark = None
            )
          }
        }
      }
    }
  }

  "consignmentReads" - {
    "must create consignment" in {
      import pages.grossMass.GrossMassPage

      forAll(arbitrary[CC043CType]) {
        ie043 =>
          val grossMass = Gen.option(arbitrary[BigDecimal]).sample.value

          val userAnswers = emptyUserAnswers
            .setValue(GrossMassPage, grossMass)

          val reads  = service.consignmentReads(ie043)
          val result = userAnswers.data.as[ConsignmentType06](reads)

          result.grossMass mustBe grossMass
      }
    }

    "must create transport equipment" - {
      import pages.ContainerIdentificationNumberPage
      import pages.sections.transport.equipment.ItemSection
      import pages.sections.{SealSection, TransportEquipmentSection}
      import pages.transportEquipment.index.seals.SealIdentificationNumberPage

      "when there are discrepancies" in {
        forAll(arbitrary[CC043CType], arbitrary[ConsignmentType05]) {
          (a, b) =>
            val transportEquipment = Seq(
              TransportEquipmentType05(
                sequenceNumber = 1,
                containerIdentificationNumber = Some("originalTransportEquipment1ContainerIdentificationNumber"),
                numberOfSeals = 2,
                Seal = Seq(
                  SealType04(
                    sequenceNumber = 1,
                    identifier = "originalTransportEquipment1SealIdentifier1"
                  ),
                  SealType04(
                    sequenceNumber = 2,
                    identifier = "originalTransportEquipment1SealIdentifier2"
                  )
                ),
                GoodsReference = Seq(
                  GoodsReferenceType02(
                    sequenceNumber = 1,
                    declarationGoodsItemNumber = 1
                  ),
                  GoodsReferenceType02(
                    sequenceNumber = 2,
                    declarationGoodsItemNumber = 2
                  )
                )
              ),
              TransportEquipmentType05(
                sequenceNumber = 2,
                containerIdentificationNumber = Some("originalTransportEquipment2ContainerIdentificationNumber"),
                numberOfSeals = 2,
                Seal = Seq(
                  SealType04(
                    sequenceNumber = 1,
                    identifier = "originalTransportEquipment2SealIdentifier1"
                  ),
                  SealType04(
                    sequenceNumber = 2,
                    identifier = "originalTransportEquipment2SealIdentifier2"
                  )
                ),
                GoodsReference = Seq(
                  GoodsReferenceType02(
                    sequenceNumber = 1,
                    declarationGoodsItemNumber = 3
                  ),
                  GoodsReferenceType02(
                    sequenceNumber = 2,
                    declarationGoodsItemNumber = 4
                  )
                )
              )
            )
            val consignment = b.copy(TransportEquipment = transportEquipment)
            val ie043       = a.copy(Consignment = Some(consignment))

            val userAnswers = emptyUserAnswers
              // Transport equipment 1 - Changed
              .setSequenceNumber(TransportEquipmentSection(Index(0)), 1)
              .setNotRemoved(TransportEquipmentSection(Index(0)))
              .setValue(ContainerIdentificationNumberPage(Index(0)), "originalTransportEquipment1ContainerIdentificationNumber")
              /// Transport equipment 1 - Seal 1 - Unchanged
              .setSequenceNumber(SealSection(Index(0), Index(0)), 1)
              .setNotRemoved(SealSection(Index(0), Index(0)))
              .setValue(SealIdentificationNumberPage(Index(0), Index(0)), "originalTransportEquipment1SealIdentifier1")
              /// Transport equipment 1 - Seal 2 - Changed
              .setSequenceNumber(SealSection(Index(0), Index(1)), 2)
              .setNotRemoved(SealSection(Index(0), Index(1)))
              .setValue(SealIdentificationNumberPage(Index(0), Index(1)), "newTransportEquipment1SealIdentifier2")
              /// Transport equipment 1 - Seal 3 - Added
              .setValue(SealIdentificationNumberPage(Index(0), Index(2)), "newTransportEquipment1SealIdentifier3")
              /// Transport equipment 1 - Goods reference 1 - Unchanged
              .setSequenceNumber(ItemSection(Index(0), Index(0)), 1)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(ItemPage(Index(0), Index(0)), BigInt(1))
              /// Transport equipment 1 - Goods reference 2 - Changed
              .setSequenceNumber(ItemSection(Index(0), Index(1)), 2)
              .setNotRemoved(ItemSection(Index(0), Index(1)))
              .setValue(ItemPage(Index(0), Index(1)), BigInt(5))
              /// Transport equipment 1 - Goods reference 3 - Added
              .setValue(ItemPage(Index(0), Index(2)), BigInt(6))

              // Transport equipment 2 - Changed
              .setSequenceNumber(TransportEquipmentSection(Index(1)), 2)
              .setNotRemoved(TransportEquipmentSection(Index(1)))
              .setValue(ContainerIdentificationNumberPage(Index(1)), "newTransportEquipment2ContainerIdentificationNumber")
              /// Transport equipment 2 - Seal 1 - Changed
              .setSequenceNumber(SealSection(Index(1), Index(0)), 1)
              .setNotRemoved(SealSection(Index(1), Index(0)))
              .setValue(SealIdentificationNumberPage(Index(1), Index(0)), "newTransportEquipment2SealIdentifier1")
              /// Transport equipment 2 - Seal 2 - Unchanged
              .setSequenceNumber(SealSection(Index(1), Index(1)), 2)
              .setNotRemoved(SealSection(Index(1), Index(1)))
              .setValue(SealIdentificationNumberPage(Index(1), Index(1)), "originalTransportEquipment2SealIdentifier2")
              /// Transport equipment 2 - Seal 3 - Added
              .setValue(SealIdentificationNumberPage(Index(1), Index(2)), "newTransportEquipment2SealIdentifier3")
              /// Transport equipment 2 - Goods reference 1 - Unchanged
              .setSequenceNumber(ItemSection(Index(1), Index(0)), 1)
              .setNotRemoved(ItemSection(Index(1), Index(0)))
              .setValue(ItemPage(Index(1), Index(0)), BigInt(3))
              /// Transport equipment 2 - Goods reference 2 - Changed
              .setSequenceNumber(ItemSection(Index(1), Index(1)), 2)
              .setNotRemoved(ItemSection(Index(1), Index(1)))
              .setValue(ItemPage(Index(1), Index(1)), BigInt(7))
              /// Transport equipment 2 - Goods reference 3 - Added
              .setValue(ItemPage(Index(1), Index(2)), BigInt(8))

              // Transport equipment 3 - Removed
              .setSequenceNumber(TransportEquipmentSection(Index(2)), 3)
              .setRemoved(TransportEquipmentSection(Index(2)))

              // Transport equipment 4 - Added
              .setValue(ContainerIdentificationNumberPage(Index(3)), "newTransportEquipment4ContainerIdentificationNumber")
              /// Transport equipment 4 - Seal 1 - Added
              .setValue(SealIdentificationNumberPage(Index(3), Index(0)), "newTransportEquipment4SealIdentifier1")
              /// Transport equipment 4 - Goods reference 1 - Added
              .setValue(ItemPage(Index(3), Index(0)), BigInt(9))

            val reads  = service.consignmentReads(ie043)
            val result = userAnswers.data.as[ConsignmentType06](reads).TransportEquipment

            result mustBe Seq(
              TransportEquipmentType03(
                sequenceNumber = 1,
                containerIdentificationNumber = None,
                numberOfSeals = Some(2),
                Seal = Seq(
                  SealType02(
                    sequenceNumber = 2,
                    identifier = "newTransportEquipment1SealIdentifier2"
                  ),
                  SealType02(
                    sequenceNumber = 3,
                    identifier = "newTransportEquipment1SealIdentifier3"
                  )
                ),
                GoodsReference = Seq(
                  GoodsReferenceType01(
                    sequenceNumber = 2,
                    declarationGoodsItemNumber = 5
                  ),
                  GoodsReferenceType01(
                    sequenceNumber = 3,
                    declarationGoodsItemNumber = 6
                  )
                )
              ),
              TransportEquipmentType03(
                sequenceNumber = 2,
                containerIdentificationNumber = Some("newTransportEquipment2ContainerIdentificationNumber"),
                numberOfSeals = Some(2),
                Seal = Seq(
                  SealType02(
                    sequenceNumber = 1,
                    identifier = "newTransportEquipment2SealIdentifier1"
                  ),
                  SealType02(
                    sequenceNumber = 3,
                    identifier = "newTransportEquipment2SealIdentifier3"
                  )
                ),
                GoodsReference = Seq(
                  GoodsReferenceType01(
                    sequenceNumber = 2,
                    declarationGoodsItemNumber = 7
                  ),
                  GoodsReferenceType01(
                    sequenceNumber = 3,
                    declarationGoodsItemNumber = 8
                  )
                )
              ),
              TransportEquipmentType03(
                sequenceNumber = 3,
                containerIdentificationNumber = None,
                numberOfSeals = None,
                Seal = Nil,
                GoodsReference = Nil
              ),
              TransportEquipmentType03(
                sequenceNumber = 4,
                containerIdentificationNumber = Some("newTransportEquipment4ContainerIdentificationNumber"),
                numberOfSeals = Some(1),
                Seal = Seq(
                  SealType02(
                    sequenceNumber = 1,
                    identifier = "newTransportEquipment4SealIdentifier1"
                  )
                ),
                GoodsReference = Seq(
                  GoodsReferenceType01(
                    sequenceNumber = 1,
                    declarationGoodsItemNumber = 9
                  )
                )
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[CC043CType], arbitrary[ConsignmentType05]) {
          (a, b) =>
            val transportEquipment = Seq(
              TransportEquipmentType05(
                sequenceNumber = 1,
                containerIdentificationNumber = Some("originalTransportEquipment1ContainerIdentificationNumber"),
                numberOfSeals = 2,
                Seal = Seq(
                  SealType04(
                    sequenceNumber = 1,
                    identifier = "originalTransportEquipment1SealIdentifier1"
                  ),
                  SealType04(
                    sequenceNumber = 2,
                    identifier = "originalTransportEquipment1SealIdentifier2"
                  )
                ),
                GoodsReference = Seq(
                  GoodsReferenceType02(
                    sequenceNumber = 1,
                    declarationGoodsItemNumber = 1
                  ),
                  GoodsReferenceType02(
                    sequenceNumber = 2,
                    declarationGoodsItemNumber = 2
                  )
                )
              ),
              TransportEquipmentType05(
                sequenceNumber = 2,
                containerIdentificationNumber = Some("originalTransportEquipment2ContainerIdentificationNumber"),
                numberOfSeals = 2,
                Seal = Seq(
                  SealType04(
                    sequenceNumber = 1,
                    identifier = "originalTransportEquipment2SealIdentifier1"
                  ),
                  SealType04(
                    sequenceNumber = 2,
                    identifier = "originalTransportEquipment2SealIdentifier2"
                  )
                ),
                GoodsReference = Seq(
                  GoodsReferenceType02(
                    sequenceNumber = 1,
                    declarationGoodsItemNumber = 3
                  ),
                  GoodsReferenceType02(
                    sequenceNumber = 2,
                    declarationGoodsItemNumber = 4
                  )
                )
              )
            )
            val consignment = b.copy(TransportEquipment = transportEquipment)
            val ie043       = a.copy(Consignment = Some(consignment))

            val userAnswers = emptyUserAnswers
              // Transport equipment 1 - Unchanged
              .setSequenceNumber(TransportEquipmentSection(Index(0)), 1)
              .setNotRemoved(TransportEquipmentSection(Index(0)))
              .setValue(ContainerIdentificationNumberPage(Index(0)), "originalTransportEquipment1ContainerIdentificationNumber")
              /// Transport equipment 1 - Seal 1 - Unchanged
              .setSequenceNumber(SealSection(Index(0), Index(0)), 1)
              .setNotRemoved(SealSection(Index(0), Index(0)))
              .setValue(SealIdentificationNumberPage(Index(0), Index(0)), "originalTransportEquipment1SealIdentifier1")
              /// Transport equipment 1 - Seal 2 - Unchanged
              .setSequenceNumber(SealSection(Index(0), Index(1)), 2)
              .setNotRemoved(SealSection(Index(0), Index(1)))
              .setValue(SealIdentificationNumberPage(Index(0), Index(1)), "originalTransportEquipment1SealIdentifier2")
              /// Transport equipment 1 - Goods reference 1 - Unchanged
              .setSequenceNumber(ItemSection(Index(0), Index(0)), 1)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(ItemPage(Index(0), Index(0)), BigInt(1))
              /// Transport equipment 1 - Goods reference 2 - Unchanged
              .setSequenceNumber(ItemSection(Index(0), Index(1)), 2)
              .setNotRemoved(ItemSection(Index(0), Index(1)))
              .setValue(ItemPage(Index(0), Index(1)), BigInt(2))

              // Transport equipment 2 - Unchanged
              .setSequenceNumber(TransportEquipmentSection(Index(1)), 2)
              .setNotRemoved(TransportEquipmentSection(Index(1)))
              .setValue(ContainerIdentificationNumberPage(Index(1)), "originalTransportEquipment2ContainerIdentificationNumber")
              /// Transport equipment 2 - Seal 1 - Unchanged
              .setSequenceNumber(SealSection(Index(1), Index(0)), 1)
              .setNotRemoved(SealSection(Index(1), Index(0)))
              .setValue(SealIdentificationNumberPage(Index(1), Index(0)), "originalTransportEquipment2SealIdentifier1")
              /// Transport equipment 2 - Seal 2 - Unchanged
              .setSequenceNumber(SealSection(Index(1), Index(1)), 2)
              .setNotRemoved(SealSection(Index(1), Index(1)))
              .setValue(SealIdentificationNumberPage(Index(1), Index(1)), "originalTransportEquipment2SealIdentifier2")
              /// Transport equipment 2 - Goods reference 1 - Unchanged
              .setSequenceNumber(ItemSection(Index(1), Index(0)), 1)
              .setNotRemoved(ItemSection(Index(1), Index(0)))
              .setValue(ItemPage(Index(1), Index(0)), BigInt(3))
              /// Transport equipment 2 - Goods reference 2 - Unchanged
              .setSequenceNumber(ItemSection(Index(1), Index(1)), 2)
              .setNotRemoved(ItemSection(Index(1), Index(1)))
              .setValue(ItemPage(Index(1), Index(1)), BigInt(4))

            val reads  = service.consignmentReads(ie043)
            val result = userAnswers.data.as[ConsignmentType06](reads).TransportEquipment

            result mustBe Nil
        }
      }
    }

    "must create additional references" - {
      import pages.additionalReference._
      import pages.sections.additionalReference.AdditionalReferenceSection

      "when there are discrepancies" in {
        forAll(arbitrary[CC043CType], arbitrary[ConsignmentType05]) {
          (a, b) =>
            val additionalReferences = Seq(
              AdditionalReferenceType03(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = Some("originalReferenceNumber1")
              ),
              AdditionalReferenceType03(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = Some("originalReferenceNumber2")
              ),
              AdditionalReferenceType03(
                sequenceNumber = 3,
                typeValue = "originalTypeValue3",
                referenceNumber = Some("originalReferenceNumber3")
              ),
              AdditionalReferenceType03(
                sequenceNumber = 4,
                typeValue = "originalTypeValue4",
                referenceNumber = Some("originalReferenceNumber4")
              )
            )
            val consignment = b.copy(AdditionalReference = additionalReferences)
            val ie043       = a.copy(Consignment = Some(consignment))

            val userAnswers = emptyUserAnswers
              // Additional reference 1 - Changed type value
              .setSequenceNumber(AdditionalReferenceSection(Index(0)), 1)
              .setNotRemoved(AdditionalReferenceSection(Index(0)))
              .setValue(AdditionalReferenceTypePage(Index(0)), AdditionalReferenceType("newTypeValue1", "newTypeValue1Description"))
              .setValue(AdditionalReferenceNumberPage(Index(0)), "originalReferenceNumber1")
              // Additional reference 2 - Changed reference number
              .setSequenceNumber(AdditionalReferenceSection(Index(1)), 2)
              .setNotRemoved(AdditionalReferenceSection(Index(1)))
              .setValue(AdditionalReferenceTypePage(Index(1)), AdditionalReferenceType("originalTypeValue2", "originalTypeValue2Description"))
              .setValue(AdditionalReferenceNumberPage(Index(1)), "newReferenceNumber2")
              // Additional reference 3 - Removed
              .setSequenceNumber(AdditionalReferenceSection(Index(2)), 3)
              .setRemoved(AdditionalReferenceSection(Index(2)))
              // Additional reference 4 - Unchanged
              .setSequenceNumber(AdditionalReferenceSection(Index(3)), 4)
              .setNotRemoved(AdditionalReferenceSection(Index(3)))
              .setValue(AdditionalReferenceTypePage(Index(3)), AdditionalReferenceType("originalTypeValue4", "originalTypeValue4Description"))
              .setValue(AdditionalReferenceNumberPage(Index(3)), "originalReferenceNumber4")
              // Additional reference 5 - Added
              .setValue(AdditionalReferenceTypePage(Index(4)), AdditionalReferenceType("newTypeValue5", "newTypeValue5Description"))
              .setValue(AdditionalReferenceNumberPage(Index(4)), "newReferenceNumber5")

            val reads  = service.consignmentReads(ie043)
            val result = userAnswers.data.as[ConsignmentType06](reads).AdditionalReference

            result mustBe Seq(
              AdditionalReferenceType06(
                sequenceNumber = 1,
                typeValue = Some("newTypeValue1"),
                referenceNumber = None
              ),
              AdditionalReferenceType06(
                sequenceNumber = 2,
                typeValue = None,
                referenceNumber = Some("newReferenceNumber2")
              ),
              AdditionalReferenceType06(
                sequenceNumber = 3,
                typeValue = None,
                referenceNumber = None
              ),
              AdditionalReferenceType06(
                sequenceNumber = 5,
                typeValue = Some("newTypeValue5"),
                referenceNumber = Some("newReferenceNumber5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[CC043CType], arbitrary[ConsignmentType05]) {
          (a, b) =>
            val additionalReferences = Seq(
              AdditionalReferenceType03(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = Some("originalReferenceNumber1")
              ),
              AdditionalReferenceType03(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = Some("originalReferenceNumber2")
              )
            )
            val consignment = b.copy(AdditionalReference = additionalReferences)
            val ie043       = a.copy(Consignment = Some(consignment))

            val userAnswers = emptyUserAnswers
              // Additional reference 1 - Unchanged
              .setSequenceNumber(AdditionalReferenceSection(Index(0)), 1)
              .setNotRemoved(AdditionalReferenceSection(Index(0)))
              .setValue(AdditionalReferenceTypePage(Index(0)), AdditionalReferenceType("originalTypeValue1", "originalTypeValue1Description"))
              .setValue(AdditionalReferenceNumberPage(Index(0)), "originalReferenceNumber1")
              // Additional reference 2 - Unchanged
              .setSequenceNumber(AdditionalReferenceSection(Index(1)), 2)
              .setNotRemoved(AdditionalReferenceSection(Index(1)))
              .setValue(AdditionalReferenceTypePage(Index(1)), AdditionalReferenceType("originalTypeValue2", "originalTypeValue2Description"))
              .setValue(AdditionalReferenceNumberPage(Index(1)), "originalReferenceNumber2")

            val reads  = service.consignmentReads(ie043)
            val result = userAnswers.data.as[ConsignmentType06](reads).AdditionalReference

            result mustBe Nil
        }
      }
    }
  }
}
