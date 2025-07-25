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
import generated.*
import generators.Generators
import models.reference.*
import models.{DocType, Index, UnloadingType, UserAnswers}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.NewAuthYesNoPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{__, Reads}
import play.api.test.Helpers.running
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

  "attributes" - {
    "must assign phase ID" - {
      "when phase6 disabled" in {
        running(phase5App) {
          app =>
            val service = app.injector.instanceOf[SubmissionService]
            val result  = service.attributes
            result.keys.size mustEqual 1
            result.get("@PhaseID").value.value.toString mustEqual "NCTS5.1"
        }
      }

      "when phase6 enabled" in {
        running(phase6App) {
          app =>
            val service = app.injector.instanceOf[SubmissionService]
            val result  = service.attributes
            result.keys.size mustEqual 1
            result.get("@PhaseID").value.value.toString mustEqual "NCTS6"
        }
      }
    }
  }

  "messageSequence" - {
    "must create message sequence" - {
      "when GB office of destination" in {
        val result = service.messageSequence(eoriNumber, "GB00001")

        result mustEqual MESSAGESequence(
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

        result mustEqual MESSAGESequence(
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
            val result = userAnswers.data.as[TransitOperationType11](reads)

            result mustEqual TransitOperationType11(
              MRN = userAnswers.mrn.value,
              otherThingsToReport = Some(otherThingsToReport)
            )
        }
      }

      "when there are no other things to report" in {
        val userAnswers = emptyUserAnswers

        val reads  = service.transitOperationReads(userAnswers)
        val result = userAnswers.data.as[TransitOperationType11](reads)

        result mustEqual TransitOperationType11(
          MRN = userAnswers.mrn.value,
          otherThingsToReport = None
        )
      }
    }
  }

  "unloadingRemarkReads" - {
    import pages.*

    "must create unloading remark" - {
      "when RevisedUnloadingPermission is true" - {
        "and seals exist" - {
          "revised procedure route" in {
            forAll(arbitrary[ConsignmentType05], arbitrary[TransportEquipmentType03], arbitrary[SealType01]) {
              (consignment, transportEquipment, seal) =>
                val ie043Data = basicIe043
                  .copy(
                    Consignment = Some(
                      consignment.copy(
                        TransportEquipment = Seq(
                          transportEquipment.copy(
                            Seal = Seq(seal)
                          )
                        )
                      )
                    )
                  )

                val userAnswers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, true)
                  .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
                  .setValue(GoodsTooLargeForContainerYesNoPage, false)
                  .setValue(SealsReplacedByCustomsAuthorityYesNoPage, false)
                  .setValue(UnloadingCommentsPage, "comments")
                  .copy(ie043Data = ie043Data)

                val reads  = service.unloadingRemarkReads(userAnswers)
                val result = userAnswers.data.as[UnloadingRemarkType](reads)

                result mustEqual UnloadingRemarkType(
                  conform = Number1,
                  unloadingCompletion = Number1,
                  unloadingDate = XMLCalendar("2020-01-01"),
                  stateOfSeals = Some(Number1),
                  unloadingRemark = Some("comments")
                )
            }
          }

          "unrevised procedure route" in {
            forAll(arbitrary[ConsignmentType05], arbitrary[TransportEquipmentType03], arbitrary[SealType01]) {
              (consignment, transportEquipment, seal) =>
                val ie043Data = basicIe043
                  .copy(
                    Consignment = Some(
                      consignment.copy(
                        TransportEquipment = Seq(
                          transportEquipment.copy(
                            Seal = Seq(seal)
                          )
                        )
                      )
                    )
                  )

                val userAnswers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, false)
                  .setValue(UnloadingTypePage, UnloadingType.Fully)
                  .setValue(DateGoodsUnloadedPage, LocalDate.of(2020, 1, 1))
                  .setValue(CanSealsBeReadPage, true)
                  .setValue(AreAnySealsBrokenPage, false)
                  .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
                  .setValue(UnloadingCommentsPage, "comments")
                  .copy(ie043Data = ie043Data)

                val reads  = service.unloadingRemarkReads(userAnswers)
                val result = userAnswers.data.as[UnloadingRemarkType](reads)

                result mustEqual UnloadingRemarkType(
                  conform = Number0,
                  unloadingCompletion = Number1,
                  unloadingDate = XMLCalendar("2020-01-01"),
                  stateOfSeals = Some(Number1),
                  unloadingRemark = Some("comments")
                )
            }
          }

          "cannot use revised unloading procedure due to conditions route" in {
            forAll(arbitrary[ConsignmentType05], arbitrary[TransportEquipmentType03], arbitrary[SealType01]) {
              (consignment, transportEquipment, seal) =>
                val ie043Data = basicIe043
                  .copy(
                    Consignment = Some(
                      consignment.copy(
                        TransportEquipment = Seq(
                          transportEquipment.copy(
                            Seal = Seq(seal)
                          )
                        )
                      )
                    )
                  )

                val userAnswers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, true)
                  .setValue(RevisedUnloadingProcedureConditionsYesNoPage, false)
                  .setValue(UnloadingTypePage, UnloadingType.Fully)
                  .setValue(DateGoodsUnloadedPage, LocalDate.of(2020, 1, 1))
                  .setValue(CanSealsBeReadPage, true)
                  .setValue(AreAnySealsBrokenPage, false)
                  .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
                  .setValue(UnloadingCommentsPage, "comments")
                  .copy(ie043Data = ie043Data)

                val reads  = service.unloadingRemarkReads(userAnswers)
                val result = userAnswers.data.as[UnloadingRemarkType](reads)

                result mustEqual UnloadingRemarkType(
                  conform = Number0,
                  unloadingCompletion = Number1,
                  unloadingDate = XMLCalendar("2020-01-01"),
                  stateOfSeals = Some(Number1),
                  unloadingRemark = Some("comments")
                )
            }
          }

          "cannot use revised unloading procedure due to discrepancies route" in {
            forAll(arbitrary[ConsignmentType05], arbitrary[TransportEquipmentType03], arbitrary[SealType01]) {
              (consignment, transportEquipment, seal) =>
                val ie043Data = basicIe043
                  .copy(
                    Consignment = Some(
                      consignment.copy(
                        TransportEquipment = Seq(
                          transportEquipment.copy(
                            Seal = Seq(seal)
                          )
                        )
                      )
                    )
                  )

                val userAnswers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, true)
                  .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
                  .setValue(GoodsTooLargeForContainerYesNoPage, true)
                  .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
                  .setValue(CanSealsBeReadPage, true)
                  .setValue(AreAnySealsBrokenPage, false)
                  .setValue(UnloadingCommentsPage, "comments")
                  .copy(ie043Data = ie043Data)

                val reads  = service.unloadingRemarkReads(userAnswers)
                val result = userAnswers.data.as[UnloadingRemarkType](reads)

                result mustEqual UnloadingRemarkType(
                  conform = Number0,
                  unloadingCompletion = Number1,
                  unloadingDate = XMLCalendar("2020-01-01"),
                  stateOfSeals = Some(Number1),
                  unloadingRemark = Some("comments")
                )
            }
          }

          "at incident level" in {
            forAll(arbitrary[ConsignmentType05], arbitrary[IncidentType03], arbitrary[TransportEquipmentType06], arbitrary[SealType01]) {
              (consignment, incident, transportEquipment, seal) =>
                val ie043Data = basicIe043
                  .copy(
                    Consignment = Some(
                      consignment.copy(
                        Incident = Seq(
                          incident.copy(
                            TransportEquipment = Seq(
                              transportEquipment.copy(
                                Seal = Seq(seal)
                              )
                            )
                          )
                        )
                      )
                    )
                  )

                val userAnswers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, true)
                  .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
                  .setValue(GoodsTooLargeForContainerYesNoPage, false)
                  .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
                  .copy(ie043Data = ie043Data)

                val reads  = service.unloadingRemarkReads(userAnswers)
                val result = userAnswers.data.as[UnloadingRemarkType](reads)

                result mustEqual UnloadingRemarkType(
                  conform = Number1,
                  unloadingCompletion = Number1,
                  unloadingDate = XMLCalendar("2020-01-01"),
                  stateOfSeals = Some(Number1),
                  unloadingRemark = None
                )
            }
          }
        }

        "and seals do not exist" in {
          val ie043Data = basicIe043
            .copy(Consignment = None)

          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, false)
            .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
            .copy(ie043Data = ie043Data)

          val reads  = service.unloadingRemarkReads(userAnswers)
          val result = userAnswers.data.as[UnloadingRemarkType](reads)

          result mustEqual UnloadingRemarkType(
            conform = Number1,
            unloadingCompletion = Number1,
            unloadingDate = XMLCalendar("2020-01-01"),
            stateOfSeals = None,
            unloadingRemark = None
          )
        }
      }

      "when RevisedUnloadingPermission is false" - {

        "when there are seal numbers in the IE043 message" - {
          "when seals can be read and no seals broken" - {
            "and AddTransitUnloadingPermissionDiscrepanciesYesNo is true" in {
              forAll(Gen.alphaNumStr) {
                unloadingRemark =>
                  val userAnswers = emptyUserAnswers
                    .setValue(NewAuthYesNoPage, false)
                    .setValue(UnloadingTypePage, UnloadingType.Fully)
                    .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                    .setValue(CanSealsBeReadPage, true)
                    .setValue(AreAnySealsBrokenPage, false)
                    .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
                    .setValue(UnloadingCommentsPage, unloadingRemark)

                  val reads  = service.unloadingRemarkReads(userAnswers)
                  val result = userAnswers.data.as[UnloadingRemarkType](reads)

                  result mustEqual UnloadingRemarkType(
                    conform = Number0,
                    unloadingCompletion = Number1,
                    unloadingDate = XMLCalendar("2020-01-01"),
                    stateOfSeals = Some(Number1),
                    unloadingRemark = Some(unloadingRemark)
                  )
              }
            }

            "and AddTransitUnloadingPermissionDiscrepanciesYesNo is false" in {
              forAll(Gen.alphaNumStr) {
                unloadingRemark =>
                  val userAnswers = emptyUserAnswers
                    .setValue(NewAuthYesNoPage, false)
                    .setValue(UnloadingTypePage, UnloadingType.Fully)
                    .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                    .setValue(CanSealsBeReadPage, true)
                    .setValue(AreAnySealsBrokenPage, false)
                    .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)
                    .setValue(UnloadingCommentsPage, unloadingRemark)

                  val reads  = service.unloadingRemarkReads(userAnswers)
                  val result = userAnswers.data.as[UnloadingRemarkType](reads)

                  result mustEqual UnloadingRemarkType(
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
                  .setValue(NewAuthYesNoPage, false)
                  .setValue(UnloadingTypePage, UnloadingType.Fully)
                  .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                  .setValue(CanSealsBeReadPage, true)
                  .setValue(AreAnySealsBrokenPage, true)
                  .setValue(UnloadingCommentsPage, unloadingRemark)

                val reads  = service.unloadingRemarkReads(userAnswers)
                val result = userAnswers.data.as[UnloadingRemarkType](reads)

                result mustEqual UnloadingRemarkType(
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
                  .setValue(NewAuthYesNoPage, false)
                  .setValue(UnloadingTypePage, UnloadingType.Fully)
                  .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                  .setValue(CanSealsBeReadPage, false)
                  .setValue(AreAnySealsBrokenPage, true)
                  .setValue(UnloadingCommentsPage, unloadingRemark)

                val reads  = service.unloadingRemarkReads(userAnswers)
                val result = userAnswers.data.as[UnloadingRemarkType](reads)

                result mustEqual UnloadingRemarkType(
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
                  .setValue(NewAuthYesNoPage, false)
                  .setValue(UnloadingTypePage, UnloadingType.Fully)
                  .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                  .setValue(CanSealsBeReadPage, false)
                  .setValue(AreAnySealsBrokenPage, false)
                  .setValue(UnloadingCommentsPage, unloadingRemark)

                val reads  = service.unloadingRemarkReads(userAnswers)
                val result = userAnswers.data.as[UnloadingRemarkType](reads)

                result mustEqual UnloadingRemarkType(
                  conform = Number0,
                  unloadingCompletion = Number1,
                  unloadingDate = XMLCalendar("2020-01-01"),
                  stateOfSeals = Some(Number0),
                  unloadingRemark = Some(unloadingRemark)
                )
            }
          }

          "when there are no seal numbers in the IE043 message" - {
            "and AddTransitUnloadingPermissionDiscrepanciesYesNo is false" in {
              val userAnswers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, false)
                .setValue(UnloadingTypePage, UnloadingType.Partially)
                .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

              val reads  = service.unloadingRemarkReads(userAnswers)
              val result = userAnswers.data.as[UnloadingRemarkType](reads)

              result mustEqual UnloadingRemarkType(
                conform = Number1,
                unloadingCompletion = Number0,
                unloadingDate = XMLCalendar("2020-01-01"),
                stateOfSeals = None,
                unloadingRemark = None
              )
            }

            "and AddTransitUnloadingPermissionDiscrepanciesYesNo is true" in {
              val userAnswers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, false)
                .setValue(UnloadingTypePage, UnloadingType.Partially)
                .setValue(DateGoodsUnloadedPage, LocalDate.of(2020: Int, 1, 1))
                .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

              val reads  = service.unloadingRemarkReads(userAnswers)
              val result = userAnswers.data.as[UnloadingRemarkType](reads)

              result mustEqual UnloadingRemarkType(
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

      "when cannotUseRevisedUnloadingProcedure" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)
          .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)
          .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

        val reads  = service.unloadingRemarkReads(userAnswers)
        val result = userAnswers.data.as[UnloadingRemarkType](reads)

        result mustEqual UnloadingRemarkType(
          conform = Number0,
          unloadingCompletion = Number1,
          unloadingDate = XMLCalendar("2020-01-01"),
          stateOfSeals = None,
          unloadingRemark = None
        )
      }
    }
  }

  "consignmentReads" - {

    def getResult(userAnswers: UserAnswers, reads: Reads[Option[ConsignmentType06]]): Option[ConsignmentType06] =
      (userAnswers.data \ "Consignment").get.as[Option[ConsignmentType06]](reads)

    "must create consignment" - {
      import pages.{GrossWeightPage, NewAuthYesNoPage, UniqueConsignmentReferencePage}

      val grossMass          = BigDecimal(100)
      val referenceNumberUCR = "bar"

      "when there are discrepancies" - {
        "when values defined in IE043" in {
          forAll(arbitrary[ConsignmentType05]) {
            consignment =>
              val ie043 = consignment.copy(
                grossMass = Some(50),
                referenceNumberUCR = Some("foo")
              )

              val userAnswers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, false)
                .setValue(GrossWeightPage, grossMass)
                .setValue(UniqueConsignmentReferencePage, referenceNumberUCR)

              val reads  = service.consignmentReads(Some(ie043))
              val result = getResult(userAnswers, reads)

              result.value.grossMass.value mustEqual grossMass
              result.value.referenceNumberUCR.value mustEqual referenceNumberUCR
          }
        }

        "when values undefined in IE043" in {
          forAll(arbitrary[ConsignmentType05]) {
            consignment =>
              val ie043 = consignment.copy(
                grossMass = None,
                referenceNumberUCR = None
              )

              val userAnswers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, false)
                .setValue(GrossWeightPage, grossMass)
                .setValue(UniqueConsignmentReferencePage, referenceNumberUCR)

              val reads  = service.consignmentReads(Some(ie043))
              val result = getResult(userAnswers, reads)

              result.value.grossMass.value mustEqual grossMass
              result.value.referenceNumberUCR.value mustEqual referenceNumberUCR
          }
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val ie043 = consignment.copy(
              grossMass = Some(grossMass),
              referenceNumberUCR = Some(referenceNumberUCR)
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setValue(GrossWeightPage, grossMass)
              .setValue(UniqueConsignmentReferencePage, referenceNumberUCR)

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create transport equipment" - {
      import pages.sections.transport.equipment.ItemSection
      import pages.sections.{SealSection, TransportEquipmentSection}
      import pages.transportEquipment.index.ItemPage
      import pages.transportEquipment.index.seals.SealIdentificationNumberPage
      import pages.{ContainerIdentificationNumberPage, NewAuthYesNoPage}

      "when there are discrepancies" - {
        "when phase 5" in {
          running(phase5App) {
            app =>
              val service = app.injector.instanceOf[SubmissionService]
              forAll(arbitrary[ConsignmentType05]) {
                consignment =>
                  val transportEquipment = Seq(
                    TransportEquipmentType03(
                      sequenceNumber = 1,
                      containerIdentificationNumber = Some("originalTransportEquipment1ContainerIdentificationNumber"),
                      numberOfSeals = 2,
                      Seal = Seq(
                        SealType01(
                          sequenceNumber = 1,
                          identifier = "originalTransportEquipment1SealIdentifier1"
                        ),
                        SealType01(
                          sequenceNumber = 2,
                          identifier = "originalTransportEquipment1SealIdentifier2"
                        ),
                        SealType01(
                          sequenceNumber = 3,
                          identifier = "originalTransportEquipment1SealIdentifier3"
                        )
                      ),
                      GoodsReference = Seq(
                        GoodsReferenceType01(
                          sequenceNumber = 1,
                          declarationGoodsItemNumber = 1
                        ),
                        GoodsReferenceType01(
                          sequenceNumber = 2,
                          declarationGoodsItemNumber = 2
                        ),
                        GoodsReferenceType01(
                          sequenceNumber = 3,
                          declarationGoodsItemNumber = 3
                        )
                      )
                    ),
                    TransportEquipmentType03(
                      sequenceNumber = 2,
                      containerIdentificationNumber = Some("originalTransportEquipment2ContainerIdentificationNumber"),
                      numberOfSeals = 2,
                      Seal = Seq(
                        SealType01(
                          sequenceNumber = 1,
                          identifier = "originalTransportEquipment2SealIdentifier1"
                        ),
                        SealType01(
                          sequenceNumber = 2,
                          identifier = "originalTransportEquipment2SealIdentifier2"
                        ),
                        SealType01(
                          sequenceNumber = 3,
                          identifier = "originalTransportEquipment2SealIdentifier3"
                        )
                      ),
                      GoodsReference = Seq(
                        GoodsReferenceType01(
                          sequenceNumber = 1,
                          declarationGoodsItemNumber = 4
                        ),
                        GoodsReferenceType01(
                          sequenceNumber = 2,
                          declarationGoodsItemNumber = 5
                        ),
                        GoodsReferenceType01(
                          sequenceNumber = 3,
                          declarationGoodsItemNumber = 6
                        )
                      )
                    )
                  )
                  val ie043 = consignment.copy(TransportEquipment = transportEquipment)

                  val userAnswers = emptyUserAnswers
                    .setValue(NewAuthYesNoPage, false)
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
                    /// Transport equipment 1 - Seal 3 - Removed
                    .setSequenceNumber(SealSection(Index(0), Index(2)), 3)
                    .setRemoved(SealSection(Index(0), Index(2)))
                    .setValue(SealIdentificationNumberPage(Index(0), Index(2)), "originalTransportEquipment1SealIdentifier3")
                    /// Transport equipment 1 - Seal 4 - Added
                    .setValue(SealIdentificationNumberPage(Index(0), Index(3)), "newTransportEquipment1SealIdentifier4")
                    /// Transport equipment 1 - Goods reference 1 - Unchanged
                    .setSequenceNumber(ItemSection(Index(0), Index(0)), 1)
                    .setNotRemoved(ItemSection(Index(0), Index(0)))
                    .setValue(ItemPage(Index(0), Index(0)), BigInt(1))
                    /// Transport equipment 1 - Goods reference 2 - Changed
                    .setSequenceNumber(ItemSection(Index(0), Index(1)), 2)
                    .setNotRemoved(ItemSection(Index(0), Index(1)))
                    .setValue(ItemPage(Index(0), Index(1)), BigInt(7))
                    /// Transport equipment 1 - Goods reference 3 - Removed
                    .setSequenceNumber(ItemSection(Index(0), Index(2)), 3)
                    .setRemoved(ItemSection(Index(0), Index(2)))
                    .setValue(ItemPage(Index(0), Index(2)), BigInt(3))
                    /// Transport equipment 1 - Goods reference 4 - Added
                    .setValue(ItemPage(Index(0), Index(3)), BigInt(8))

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
                    /// Transport equipment 2 - Seal 3 - Removed
                    .setSequenceNumber(SealSection(Index(1), Index(2)), 3)
                    .setRemoved(SealSection(Index(1), Index(2)))
                    .setValue(SealIdentificationNumberPage(Index(1), Index(2)), "originalTransportEquipment2SealIdentifier3")
                    /// Transport equipment 2 - Seal 4 - Added
                    .setValue(SealIdentificationNumberPage(Index(1), Index(3)), "newTransportEquipment2SealIdentifier4")
                    /// Transport equipment 2 - Goods reference 1 - Unchanged
                    .setSequenceNumber(ItemSection(Index(1), Index(0)), 1)
                    .setNotRemoved(ItemSection(Index(1), Index(0)))
                    .setValue(ItemPage(Index(1), Index(0)), BigInt(4))
                    /// Transport equipment 2 - Goods reference 2 - Changed
                    .setSequenceNumber(ItemSection(Index(1), Index(1)), 2)
                    .setNotRemoved(ItemSection(Index(1), Index(1)))
                    .setValue(ItemPage(Index(1), Index(1)), BigInt(9))
                    /// Transport equipment 2 - Goods reference 3 - Removed
                    .setSequenceNumber(ItemSection(Index(1), Index(2)), 3)
                    .setRemoved(ItemSection(Index(1), Index(2)))
                    .setValue(ItemPage(Index(1), Index(2)), BigInt(6))
                    /// Transport equipment 2 - Goods reference 4 - Added
                    .setValue(ItemPage(Index(1), Index(3)), BigInt(10))

                    // Transport equipment 3 - Removed
                    .setSequenceNumber(TransportEquipmentSection(Index(2)), 3)
                    .setRemoved(TransportEquipmentSection(Index(2)))

                    // Transport equipment 4 - Added
                    .setValue(ContainerIdentificationNumberPage(Index(3)), "newTransportEquipment4ContainerIdentificationNumber")
                    /// Transport equipment 4 - Seal 1 - Added
                    .setValue(SealIdentificationNumberPage(Index(3), Index(0)), "newTransportEquipment4SealIdentifier1")
                    /// Transport equipment 4 - Goods reference 1 - Added
                    .setValue(ItemPage(Index(3), Index(0)), BigInt(9))

                  val reads  = service.consignmentReads(Some(ie043))
                  val result = getResult(userAnswers, reads).value.TransportEquipment

                  result mustEqual Seq(
                    TransportEquipmentType04(
                      sequenceNumber = 1,
                      containerIdentificationNumber = None,
                      numberOfSeals = Some(3),
                      Seal = Seq(
                        SealType02(
                          sequenceNumber = 2,
                          identifier = Some("newTransportEquipment1SealIdentifier2")
                        ),
                        SealType02(
                          sequenceNumber = 3,
                          identifier = Some("originalTransportEquipment1SealIdentifier3")
                        ),
                        SealType02(
                          sequenceNumber = 4,
                          identifier = Some("newTransportEquipment1SealIdentifier4")
                        )
                      ),
                      GoodsReference = Seq(
                        GoodsReferenceType02(
                          sequenceNumber = 2,
                          declarationGoodsItemNumber = Some(7)
                        ),
                        GoodsReferenceType02(
                          sequenceNumber = 3,
                          declarationGoodsItemNumber = Some(3)
                        ),
                        GoodsReferenceType02(
                          sequenceNumber = 4,
                          declarationGoodsItemNumber = Some(8)
                        )
                      )
                    ),
                    TransportEquipmentType04(
                      sequenceNumber = 2,
                      containerIdentificationNumber = Some("newTransportEquipment2ContainerIdentificationNumber"),
                      numberOfSeals = Some(3),
                      Seal = Seq(
                        SealType02(
                          sequenceNumber = 1,
                          identifier = Some("newTransportEquipment2SealIdentifier1")
                        ),
                        SealType02(
                          sequenceNumber = 3,
                          identifier = Some("originalTransportEquipment2SealIdentifier3")
                        ),
                        SealType02(
                          sequenceNumber = 4,
                          identifier = Some("newTransportEquipment2SealIdentifier4")
                        )
                      ),
                      GoodsReference = Seq(
                        GoodsReferenceType02(
                          sequenceNumber = 2,
                          declarationGoodsItemNumber = Some(9)
                        ),
                        GoodsReferenceType02(
                          sequenceNumber = 3,
                          declarationGoodsItemNumber = Some(6)
                        ),
                        GoodsReferenceType02(
                          sequenceNumber = 4,
                          declarationGoodsItemNumber = Some(10)
                        )
                      )
                    ),
                    TransportEquipmentType04(
                      sequenceNumber = 3,
                      containerIdentificationNumber = None,
                      numberOfSeals = None,
                      Seal = Nil,
                      GoodsReference = Nil
                    ),
                    TransportEquipmentType04(
                      sequenceNumber = 4,
                      containerIdentificationNumber = Some("newTransportEquipment4ContainerIdentificationNumber"),
                      numberOfSeals = Some(1),
                      Seal = Seq(
                        SealType02(
                          sequenceNumber = 1,
                          identifier = Some("newTransportEquipment4SealIdentifier1")
                        )
                      ),
                      GoodsReference = Seq(
                        GoodsReferenceType02(
                          sequenceNumber = 1,
                          declarationGoodsItemNumber = Some(9)
                        )
                      )
                    )
                  )
              }
          }
        }

        "when phase 6" in {
          running(phase6App) {
            app =>
              val service = app.injector.instanceOf[SubmissionService]
              forAll(arbitrary[ConsignmentType05]) {
                consignment =>
                  val transportEquipment = Seq(
                    TransportEquipmentType03(
                      sequenceNumber = 1,
                      containerIdentificationNumber = Some("originalTransportEquipment1ContainerIdentificationNumber"),
                      numberOfSeals = 2,
                      Seal = Seq(
                        SealType01(
                          sequenceNumber = 1,
                          identifier = "originalTransportEquipment1SealIdentifier1"
                        ),
                        SealType01(
                          sequenceNumber = 2,
                          identifier = "originalTransportEquipment1SealIdentifier2"
                        ),
                        SealType01(
                          sequenceNumber = 3,
                          identifier = "originalTransportEquipment1SealIdentifier3"
                        )
                      ),
                      GoodsReference = Seq(
                        GoodsReferenceType01(
                          sequenceNumber = 1,
                          declarationGoodsItemNumber = 1
                        ),
                        GoodsReferenceType01(
                          sequenceNumber = 2,
                          declarationGoodsItemNumber = 2
                        ),
                        GoodsReferenceType01(
                          sequenceNumber = 3,
                          declarationGoodsItemNumber = 3
                        )
                      )
                    ),
                    TransportEquipmentType03(
                      sequenceNumber = 2,
                      containerIdentificationNumber = Some("originalTransportEquipment2ContainerIdentificationNumber"),
                      numberOfSeals = 2,
                      Seal = Seq(
                        SealType01(
                          sequenceNumber = 1,
                          identifier = "originalTransportEquipment2SealIdentifier1"
                        ),
                        SealType01(
                          sequenceNumber = 2,
                          identifier = "originalTransportEquipment2SealIdentifier2"
                        ),
                        SealType01(
                          sequenceNumber = 3,
                          identifier = "originalTransportEquipment2SealIdentifier3"
                        )
                      ),
                      GoodsReference = Seq(
                        GoodsReferenceType01(
                          sequenceNumber = 1,
                          declarationGoodsItemNumber = 4
                        ),
                        GoodsReferenceType01(
                          sequenceNumber = 2,
                          declarationGoodsItemNumber = 5
                        ),
                        GoodsReferenceType01(
                          sequenceNumber = 3,
                          declarationGoodsItemNumber = 6
                        )
                      )
                    )
                  )
                  val ie043 = consignment.copy(TransportEquipment = transportEquipment)

                  val userAnswers = emptyUserAnswers
                    .setValue(NewAuthYesNoPage, false)
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
                    /// Transport equipment 1 - Seal 3 - Removed
                    .setSequenceNumber(SealSection(Index(0), Index(2)), 3)
                    .setRemoved(SealSection(Index(0), Index(2)))
                    .setValue(SealIdentificationNumberPage(Index(0), Index(2)), "originalTransportEquipment1SealIdentifier3")
                    /// Transport equipment 1 - Seal 4 - Added
                    .setValue(SealIdentificationNumberPage(Index(0), Index(3)), "newTransportEquipment1SealIdentifier4")
                    /// Transport equipment 1 - Goods reference 1 - Unchanged
                    .setSequenceNumber(ItemSection(Index(0), Index(0)), 1)
                    .setNotRemoved(ItemSection(Index(0), Index(0)))
                    .setValue(ItemPage(Index(0), Index(0)), BigInt(1))
                    /// Transport equipment 1 - Goods reference 2 - Changed
                    .setSequenceNumber(ItemSection(Index(0), Index(1)), 2)
                    .setNotRemoved(ItemSection(Index(0), Index(1)))
                    .setValue(ItemPage(Index(0), Index(1)), BigInt(7))
                    /// Transport equipment 1 - Goods reference 3 - Removed
                    .setSequenceNumber(ItemSection(Index(0), Index(2)), 3)
                    .setRemoved(ItemSection(Index(0), Index(2)))
                    .setValue(ItemPage(Index(0), Index(2)), BigInt(3))
                    /// Transport equipment 1 - Goods reference 4 - Added
                    .setValue(ItemPage(Index(0), Index(3)), BigInt(8))

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
                    /// Transport equipment 2 - Seal 3 - Removed
                    .setSequenceNumber(SealSection(Index(1), Index(2)), 3)
                    .setRemoved(SealSection(Index(1), Index(2)))
                    .setValue(SealIdentificationNumberPage(Index(1), Index(2)), "originalTransportEquipment2SealIdentifier3")
                    /// Transport equipment 2 - Seal 4 - Added
                    .setValue(SealIdentificationNumberPage(Index(1), Index(3)), "newTransportEquipment2SealIdentifier4")
                    /// Transport equipment 2 - Goods reference 1 - Unchanged
                    .setSequenceNumber(ItemSection(Index(1), Index(0)), 1)
                    .setNotRemoved(ItemSection(Index(1), Index(0)))
                    .setValue(ItemPage(Index(1), Index(0)), BigInt(4))
                    /// Transport equipment 2 - Goods reference 2 - Changed
                    .setSequenceNumber(ItemSection(Index(1), Index(1)), 2)
                    .setNotRemoved(ItemSection(Index(1), Index(1)))
                    .setValue(ItemPage(Index(1), Index(1)), BigInt(9))
                    /// Transport equipment 2 - Goods reference 3 - Removed
                    .setSequenceNumber(ItemSection(Index(1), Index(2)), 3)
                    .setRemoved(ItemSection(Index(1), Index(2)))
                    .setValue(ItemPage(Index(1), Index(2)), BigInt(6))
                    /// Transport equipment 2 - Goods reference 4 - Added
                    .setValue(ItemPage(Index(1), Index(3)), BigInt(10))

                    // Transport equipment 3 - Removed
                    .setSequenceNumber(TransportEquipmentSection(Index(2)), 3)
                    .setRemoved(TransportEquipmentSection(Index(2)))

                    // Transport equipment 4 - Added
                    .setValue(ContainerIdentificationNumberPage(Index(3)), "newTransportEquipment4ContainerIdentificationNumber")
                    /// Transport equipment 4 - Seal 1 - Added
                    .setValue(SealIdentificationNumberPage(Index(3), Index(0)), "newTransportEquipment4SealIdentifier1")
                    /// Transport equipment 4 - Goods reference 1 - Added
                    .setValue(ItemPage(Index(3), Index(0)), BigInt(9))

                  val reads  = service.consignmentReads(Some(ie043))
                  val result = getResult(userAnswers, reads).value.TransportEquipment

                  result mustEqual Seq(
                    TransportEquipmentType04(
                      sequenceNumber = 1,
                      containerIdentificationNumber = None,
                      numberOfSeals = Some(3),
                      Seal = Seq(
                        SealType02(
                          sequenceNumber = 2,
                          identifier = Some("newTransportEquipment1SealIdentifier2")
                        ),
                        SealType02(
                          sequenceNumber = 3,
                          identifier = None
                        ),
                        SealType02(
                          sequenceNumber = 4,
                          identifier = Some("newTransportEquipment1SealIdentifier4")
                        )
                      ),
                      GoodsReference = Seq(
                        GoodsReferenceType02(
                          sequenceNumber = 2,
                          declarationGoodsItemNumber = Some(7)
                        ),
                        GoodsReferenceType02(
                          sequenceNumber = 3,
                          declarationGoodsItemNumber = None
                        ),
                        GoodsReferenceType02(
                          sequenceNumber = 4,
                          declarationGoodsItemNumber = Some(8)
                        )
                      )
                    ),
                    TransportEquipmentType04(
                      sequenceNumber = 2,
                      containerIdentificationNumber = Some("newTransportEquipment2ContainerIdentificationNumber"),
                      numberOfSeals = Some(3),
                      Seal = Seq(
                        SealType02(
                          sequenceNumber = 1,
                          identifier = Some("newTransportEquipment2SealIdentifier1")
                        ),
                        SealType02(
                          sequenceNumber = 3,
                          identifier = None
                        ),
                        SealType02(
                          sequenceNumber = 4,
                          identifier = Some("newTransportEquipment2SealIdentifier4")
                        )
                      ),
                      GoodsReference = Seq(
                        GoodsReferenceType02(
                          sequenceNumber = 2,
                          declarationGoodsItemNumber = Some(9)
                        ),
                        GoodsReferenceType02(
                          sequenceNumber = 3,
                          declarationGoodsItemNumber = None
                        ),
                        GoodsReferenceType02(
                          sequenceNumber = 4,
                          declarationGoodsItemNumber = Some(10)
                        )
                      )
                    ),
                    TransportEquipmentType04(
                      sequenceNumber = 3,
                      containerIdentificationNumber = None,
                      numberOfSeals = None,
                      Seal = Nil,
                      GoodsReference = Nil
                    ),
                    TransportEquipmentType04(
                      sequenceNumber = 4,
                      containerIdentificationNumber = Some("newTransportEquipment4ContainerIdentificationNumber"),
                      numberOfSeals = Some(1),
                      Seal = Seq(
                        SealType02(
                          sequenceNumber = 1,
                          identifier = Some("newTransportEquipment4SealIdentifier1")
                        )
                      ),
                      GoodsReference = Seq(
                        GoodsReferenceType02(
                          sequenceNumber = 1,
                          declarationGoodsItemNumber = Some(9)
                        )
                      )
                    )
                  )
              }
          }
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val transportEquipment = Seq(
              TransportEquipmentType03(
                sequenceNumber = 1,
                containerIdentificationNumber = Some("originalTransportEquipment1ContainerIdentificationNumber"),
                numberOfSeals = 2,
                Seal = Seq(
                  SealType01(
                    sequenceNumber = 1,
                    identifier = "originalTransportEquipment1SealIdentifier1"
                  ),
                  SealType01(
                    sequenceNumber = 2,
                    identifier = "originalTransportEquipment1SealIdentifier2"
                  )
                ),
                GoodsReference = Seq(
                  GoodsReferenceType01(
                    sequenceNumber = 1,
                    declarationGoodsItemNumber = 1
                  ),
                  GoodsReferenceType01(
                    sequenceNumber = 2,
                    declarationGoodsItemNumber = 2
                  )
                )
              ),
              TransportEquipmentType03(
                sequenceNumber = 2,
                containerIdentificationNumber = Some("originalTransportEquipment2ContainerIdentificationNumber"),
                numberOfSeals = 2,
                Seal = Seq(
                  SealType01(
                    sequenceNumber = 1,
                    identifier = "originalTransportEquipment2SealIdentifier1"
                  ),
                  SealType01(
                    sequenceNumber = 2,
                    identifier = "originalTransportEquipment2SealIdentifier2"
                  )
                ),
                GoodsReference = Seq(
                  GoodsReferenceType01(
                    sequenceNumber = 1,
                    declarationGoodsItemNumber = 3
                  ),
                  GoodsReferenceType01(
                    sequenceNumber = 2,
                    declarationGoodsItemNumber = 4
                  )
                )
              )
            )
            val ie043 = consignment.copy(TransportEquipment = transportEquipment)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
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

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create departure transport means" - {
      import pages.NewAuthYesNoPage
      import pages.departureMeansOfTransport.*
      import pages.sections.TransportMeansSection

      "when there are discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val departureTransportMeans = Seq(
              CUSTOM_DepartureTransportMeansType01(
                sequenceNumber = 1,
                typeOfIdentification = Some("originalTypeOfIdentification1"),
                identificationNumber = Some("originalIdentificationNumber1"),
                nationality = Some("originalNationality1")
              ),
              CUSTOM_DepartureTransportMeansType01(
                sequenceNumber = 2,
                typeOfIdentification = Some("originalTypeOfIdentification2"),
                identificationNumber = Some("originalIdentificationNumber2"),
                nationality = Some("originalNationality2")
              ),
              CUSTOM_DepartureTransportMeansType01(
                sequenceNumber = 3,
                typeOfIdentification = Some("originalTypeOfIdentification3"),
                identificationNumber = Some("originalIdentificationNumber3"),
                nationality = Some("originalNationality3")
              ),
              CUSTOM_DepartureTransportMeansType01(
                sequenceNumber = 4,
                typeOfIdentification = Some("originalTypeOfIdentification4"),
                identificationNumber = Some("originalIdentificationNumber4"),
                nationality = Some("originalNationality4")
              )
            )
            val ie043 = consignment.copy(DepartureTransportMeans = departureTransportMeans)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              // Departure transport means 1 - Changed type value
              .setSequenceNumber(TransportMeansSection(Index(0)), 1)
              .setNotRemoved(TransportMeansSection(Index(0)))
              .setValue(TransportMeansIdentificationPage(Index(0)),
                        TransportMeansIdentification("newTypeOfIdentification1", "newTypeOfIdentification1Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(0)), "originalIdentificationNumber1")
              .setValue(CountryPage(Index(0)), Country("originalNationality1", "originalNationalityDescription1"))
              // Departure transport means 2 - Changed identification number and nationality
              .setSequenceNumber(TransportMeansSection(Index(1)), 2)
              .setNotRemoved(TransportMeansSection(Index(1)))
              .setValue(TransportMeansIdentificationPage(Index(1)),
                        TransportMeansIdentification("originalTypeOfIdentification2", "originalTypeOfIdentification2Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(1)), "newIdentificationNumber2")
              .setValue(CountryPage(Index(1)), Country("newNationality2", "newNationalityDescription2"))
              // Departure transport means 3 - Removed
              .setSequenceNumber(TransportMeansSection(Index(2)), 3)
              .setRemoved(TransportMeansSection(Index(2)))
              // Departure transport means 4 - Unchanged
              .setSequenceNumber(TransportMeansSection(Index(3)), 4)
              .setNotRemoved(TransportMeansSection(Index(3)))
              .setValue(TransportMeansIdentificationPage(Index(3)),
                        TransportMeansIdentification("originalTypeOfIdentification4", "originalTypeOfIdentification4Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(3)), "originalIdentificationNumber4")
              .setValue(CountryPage(Index(3)), Country("originalNationality4", "originalNationalityDescription4"))
              // Departure transport means 5 - Added
              .setValue(TransportMeansIdentificationPage(Index(4)),
                        TransportMeansIdentification("newTypeOfIdentification5", "newTypeOfIdentification5Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(4)), "newIdentificationNumber5")
              .setValue(CountryPage(Index(4)), Country("newNationality5", "newNationalityDescription5"))

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads).value.DepartureTransportMeans

            result mustEqual Seq(
              DepartureTransportMeansType02(
                sequenceNumber = 1,
                typeOfIdentification = Some("newTypeOfIdentification1"),
                identificationNumber = None,
                nationality = None
              ),
              DepartureTransportMeansType02(
                sequenceNumber = 2,
                typeOfIdentification = None,
                identificationNumber = Some("newIdentificationNumber2"),
                nationality = Some("newNationality2")
              ),
              DepartureTransportMeansType02(
                sequenceNumber = 3,
                typeOfIdentification = None,
                identificationNumber = None,
                nationality = None
              ),
              DepartureTransportMeansType02(
                sequenceNumber = 5,
                typeOfIdentification = Some("newTypeOfIdentification5"),
                identificationNumber = Some("newIdentificationNumber5"),
                nationality = Some("newNationality5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val departureTransportMeans = Seq(
              CUSTOM_DepartureTransportMeansType01(
                sequenceNumber = 1,
                typeOfIdentification = Some("originalTypeOfIdentification1"),
                identificationNumber = Some("originalIdentificationNumber1"),
                nationality = Some("originalNationality1")
              ),
              CUSTOM_DepartureTransportMeansType01(
                sequenceNumber = 2,
                typeOfIdentification = Some("originalTypeOfIdentification2"),
                identificationNumber = Some("originalIdentificationNumber2"),
                nationality = Some("originalNationality2")
              )
            )
            val ie043 = consignment.copy(DepartureTransportMeans = departureTransportMeans)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              // Departure transport means 1 - Unchanged
              .setSequenceNumber(TransportMeansSection(Index(0)), 1)
              .setNotRemoved(TransportMeansSection(Index(0)))
              .setValue(TransportMeansIdentificationPage(Index(0)),
                        TransportMeansIdentification("originalTypeOfIdentification1", "originalTypeOfIdentification1Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(0)), "originalIdentificationNumber1")
              // Departure transport means 2 - Unchanged
              .setSequenceNumber(TransportMeansSection(Index(1)), 2)
              .setNotRemoved(TransportMeansSection(Index(1)))
              .setValue(TransportMeansIdentificationPage(Index(1)),
                        TransportMeansIdentification("originalTypeOfIdentification2", "originalTypeOfIdentification2Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(1)), "originalIdentificationNumber2")

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create countries of routing" - {
      import pages.NewAuthYesNoPage
      import pages.countriesOfRouting.*
      import pages.sections.CountryOfRoutingSection

      "when there are discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val countriesOfRouting = Seq(
              CountryOfRoutingOfConsignmentType02(
                sequenceNumber = 1,
                country = "originalCountry1"
              ),
              CountryOfRoutingOfConsignmentType02(
                sequenceNumber = 2,
                country = "originalCountry2"
              ),
              CountryOfRoutingOfConsignmentType02(
                sequenceNumber = 3,
                country = "originalCountry3"
              ),
              CountryOfRoutingOfConsignmentType02(
                sequenceNumber = 4,
                country = "originalCountry4"
              )
            )
            val ie043 = consignment.copy(CountryOfRoutingOfConsignment = countriesOfRouting)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              // Country of routing 1 - Changed country value
              .setSequenceNumber(CountryOfRoutingSection(Index(0)), 1)
              .setNotRemoved(CountryOfRoutingSection(Index(0)))
              .setValue(CountryOfRoutingPage(Index(0)), Country("newCountry1", "newCountry1Description"))
              // Country of routing 2 - Removed
              .setSequenceNumber(CountryOfRoutingSection(Index(1)), 2)
              .setRemoved(CountryOfRoutingSection(Index(1)))
              // Country of routing 3 - Unchanged
              .setSequenceNumber(CountryOfRoutingSection(Index(2)), 3)
              .setNotRemoved(CountryOfRoutingSection(Index(2)))
              .setValue(CountryOfRoutingPage(Index(2)), Country("originalCountry3", "originalCountry3Description"))
              // Country of routing 4 - Added
              .setValue(CountryOfRoutingPage(Index(3)), Country("newCountry4", "newCountry4Description"))

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads).value.CountryOfRoutingOfConsignment

            result mustEqual Seq(
              CountryOfRoutingOfConsignmentType03(
                sequenceNumber = 1,
                country = Some("newCountry1")
              ),
              CountryOfRoutingOfConsignmentType03(
                sequenceNumber = 2,
                country = None
              ),
              CountryOfRoutingOfConsignmentType03(
                sequenceNumber = 4,
                country = Some("newCountry4")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val countriesOfRouting = Seq(
              CountryOfRoutingOfConsignmentType02(
                sequenceNumber = 1,
                country = "originalCountry1"
              ),
              CountryOfRoutingOfConsignmentType02(
                sequenceNumber = 2,
                country = "originalCountry2"
              )
            )
            val ie043 = consignment.copy(CountryOfRoutingOfConsignment = countriesOfRouting)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              // Country of routing 1 - Unchanged
              .setSequenceNumber(CountryOfRoutingSection(Index(0)), 1)
              .setNotRemoved(CountryOfRoutingSection(Index(0)))
              .setValue(CountryOfRoutingPage(Index(0)), Country("originalCountry1", "originalCountry1Description"))
              // Country of routing 2 - Unchanged
              .setSequenceNumber(CountryOfRoutingSection(Index(1)), 2)
              .setNotRemoved(CountryOfRoutingSection(Index(1)))
              .setValue(CountryOfRoutingPage(Index(1)), Country("originalCountry2", "originalCountry2Description"))

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create supporting documents" - {
      import pages.NewAuthYesNoPage
      import pages.documents.*
      import pages.sections.documents.*

      "when there are discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val supportingDocuments = Seq(
              SupportingDocumentType02(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1",
                complementOfInformation = Some("originalComplementOfInformation1")
              ),
              SupportingDocumentType02(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2",
                complementOfInformation = Some("originalComplementOfInformation2")
              ),
              SupportingDocumentType02(
                sequenceNumber = 3,
                typeValue = "originalTypeValue3",
                referenceNumber = "originalReferenceNumber3",
                complementOfInformation = Some("originalComplementOfInformation3")
              ),
              SupportingDocumentType02(
                sequenceNumber = 4,
                typeValue = "originalTypeValue4",
                referenceNumber = "originalReferenceNumber4",
                complementOfInformation = Some("originalComplementOfInformation4")
              )
            )
            val ie043 = consignment.copy(SupportingDocument = supportingDocuments)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              // Supporting document 1 - Changed type value
              .setSequenceNumber(DocumentSection(Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0)))
              .setValue(TypePage(Index(0)), DocumentType(DocType.Support, "newTypeValue1", "newTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0)), "originalReferenceNumber1")
              .setValue(AdditionalInformationPage(Index(0)), "originalComplementOfInformation1")
              // Supporting document 2 - Changed reference number
              .setSequenceNumber(DocumentSection(Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(1)))
              .setValue(TypePage(Index(1)), DocumentType(DocType.Support, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(1)), "newReferenceNumber2")
              .setValue(AdditionalInformationPage(Index(1)), "newComplementOfInformation2")
              // Supporting document 3 - Removed
              .setSequenceNumber(DocumentSection(Index(2)), 3)
              .setRemoved(DocumentSection(Index(2)))
              .setValue(DocumentSection(Index(2)), __ \ "type" \ "type", DocType.Support.display)
              // Supporting document 4 - Unchanged
              .setSequenceNumber(DocumentSection(Index(3)), 4)
              .setNotRemoved(DocumentSection(Index(3)))
              .setValue(TypePage(Index(3)), DocumentType(DocType.Support, "originalTypeValue4", "originalTypeValue4Description"))
              .setValue(DocumentReferenceNumberPage(Index(3)), "originalReferenceNumber4")
              .setValue(AdditionalInformationPage(Index(3)), "originalComplementOfInformation4")
              // Supporting document 5 - Added
              .setValue(TypePage(Index(4)), DocumentType(DocType.Support, "newTypeValue5", "newTypeValue5Description"))
              .setValue(DocumentReferenceNumberPage(Index(4)), "newReferenceNumber5")
              .setValue(AdditionalInformationPage(Index(4)), "newComplementOfInformation5")

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads).value.SupportingDocument

            result mustEqual Seq(
              SupportingDocumentType04(
                sequenceNumber = 1,
                typeValue = Some("newTypeValue1"),
                referenceNumber = None,
                complementOfInformation = None
              ),
              SupportingDocumentType04(
                sequenceNumber = 2,
                typeValue = None,
                referenceNumber = Some("newReferenceNumber2"),
                complementOfInformation = Some("newComplementOfInformation2")
              ),
              SupportingDocumentType04(
                sequenceNumber = 3,
                typeValue = None,
                referenceNumber = None,
                complementOfInformation = None
              ),
              SupportingDocumentType04(
                sequenceNumber = 5,
                typeValue = Some("newTypeValue5"),
                referenceNumber = Some("newReferenceNumber5"),
                complementOfInformation = Some("newComplementOfInformation5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val supportingDocuments = Seq(
              SupportingDocumentType02(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1",
                complementOfInformation = Some("originalComplementOfInformation1")
              ),
              SupportingDocumentType02(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2",
                complementOfInformation = Some("originalComplementOfInformation2")
              )
            )
            val ie043 = consignment.copy(SupportingDocument = supportingDocuments)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              // Supporting document 1 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0)))
              .setValue(TypePage(Index(0)), DocumentType(DocType.Support, "originalTypeValue1", "originalTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0)), "originalReferenceNumber1")
              .setValue(AdditionalInformationPage(Index(0)), "originalComplementOfInformation1")
              // Supporting document 2 - Unchanged
              .setSequenceNumber(DocumentSection(Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(1)))
              .setValue(TypePage(Index(1)), DocumentType(DocType.Support, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(1)), "originalReferenceNumber2")
              .setValue(AdditionalInformationPage(Index(1)), "originalComplementOfInformation2")

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create transport documents" - {
      import pages.NewAuthYesNoPage
      import pages.documents.*
      import pages.sections.documents.*

      "when there are discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val transportDocuments = Seq(
              TransportDocumentType01(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1"
              ),
              TransportDocumentType01(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2"
              ),
              TransportDocumentType01(
                sequenceNumber = 3,
                typeValue = "originalTypeValue3",
                referenceNumber = "originalReferenceNumber3"
              ),
              TransportDocumentType01(
                sequenceNumber = 4,
                typeValue = "originalTypeValue4",
                referenceNumber = "originalReferenceNumber4"
              )
            )
            val ie043 = consignment.copy(TransportDocument = transportDocuments)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              // Transport document 1 - Changed type value
              .setSequenceNumber(DocumentSection(Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0)))
              .setValue(TypePage(Index(0)), DocumentType(DocType.Transport, "newTypeValue1", "newTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0)), "originalReferenceNumber1")
              // Transport document 2 - Changed reference number
              .setSequenceNumber(DocumentSection(Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(1)))
              .setValue(TypePage(Index(1)), DocumentType(DocType.Transport, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(1)), "newReferenceNumber2")
              // Transport document 3 - Removed
              .setSequenceNumber(DocumentSection(Index(2)), 3)
              .setRemoved(DocumentSection(Index(2)))
              .setValue(DocumentSection(Index(2)), __ \ "type" \ "type", DocType.Transport.display)
              // Transport document 4 - Unchanged
              .setSequenceNumber(DocumentSection(Index(3)), 4)
              .setNotRemoved(DocumentSection(Index(3)))
              .setValue(TypePage(Index(3)), DocumentType(DocType.Transport, "originalTypeValue4", "originalTypeValue4Description"))
              .setValue(DocumentReferenceNumberPage(Index(3)), "originalReferenceNumber4")
              // Transport document 5 - Added
              .setValue(TypePage(Index(4)), DocumentType(DocType.Transport, "newTypeValue5", "newTypeValue5Description"))
              .setValue(DocumentReferenceNumberPage(Index(4)), "newReferenceNumber5")

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads).value.TransportDocument

            result mustEqual Seq(
              TransportDocumentType04(
                sequenceNumber = 1,
                typeValue = Some("newTypeValue1"),
                referenceNumber = None
              ),
              TransportDocumentType04(
                sequenceNumber = 2,
                typeValue = None,
                referenceNumber = Some("newReferenceNumber2")
              ),
              TransportDocumentType04(
                sequenceNumber = 3,
                typeValue = None,
                referenceNumber = None
              ),
              TransportDocumentType04(
                sequenceNumber = 5,
                typeValue = Some("newTypeValue5"),
                referenceNumber = Some("newReferenceNumber5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val transportDocuments = Seq(
              TransportDocumentType01(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1"
              ),
              TransportDocumentType01(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2"
              )
            )
            val ie043 = consignment.copy(TransportDocument = transportDocuments)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              // Transport document 1 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0)))
              .setValue(TypePage(Index(0)), DocumentType(DocType.Transport, "originalTypeValue1", "originalTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0)), "originalReferenceNumber1")
              // Transport document 2 - Unchanged
              .setSequenceNumber(DocumentSection(Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(1)))
              .setValue(TypePage(Index(1)), DocumentType(DocType.Transport, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(1)), "originalReferenceNumber2")

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create additional references" - {
      import pages.NewAuthYesNoPage
      import pages.additionalReference.*
      import pages.sections.additionalReference.AdditionalReferenceSection

      "when there are discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val additionalReferences = Seq(
              AdditionalReferenceType02(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = Some("originalReferenceNumber1")
              ),
              AdditionalReferenceType02(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = Some("originalReferenceNumber2")
              ),
              AdditionalReferenceType02(
                sequenceNumber = 3,
                typeValue = "originalTypeValue3",
                referenceNumber = Some("originalReferenceNumber3")
              ),
              AdditionalReferenceType02(
                sequenceNumber = 4,
                typeValue = "originalTypeValue4",
                referenceNumber = Some("originalReferenceNumber4")
              )
            )
            val ie043 = consignment.copy(AdditionalReference = additionalReferences)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
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

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads).value.AdditionalReference

            result mustEqual Seq(
              AdditionalReferenceType03(
                sequenceNumber = 1,
                typeValue = Some("newTypeValue1"),
                referenceNumber = None
              ),
              AdditionalReferenceType03(
                sequenceNumber = 2,
                typeValue = None,
                referenceNumber = Some("newReferenceNumber2")
              ),
              AdditionalReferenceType03(
                sequenceNumber = 3,
                typeValue = None,
                referenceNumber = None
              ),
              AdditionalReferenceType03(
                sequenceNumber = 5,
                typeValue = Some("newTypeValue5"),
                referenceNumber = Some("newReferenceNumber5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentType05]) {
          consignment =>
            val additionalReferences = Seq(
              AdditionalReferenceType02(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = Some("originalReferenceNumber1")
              ),
              AdditionalReferenceType02(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = Some("originalReferenceNumber2")
              )
            )
            val ie043 = consignment.copy(AdditionalReference = additionalReferences)

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
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

            val reads  = service.consignmentReads(Some(ie043))
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }
  }

  "houseConsignmentReads" - {
    import pages.houseConsignment.index.*
    import pages.sections.HouseConsignmentSection

    val sequenceNumber = 1

    def getResult(userAnswers: UserAnswers, reads: Reads[Option[HouseConsignmentType05]]): Option[HouseConsignmentType05] =
      (userAnswers.data \ "Consignment" \ "HouseConsignment" \ 0).get.as[Option[HouseConsignmentType05]](reads)

    "must create house consignment" - {

      val grossMass          = BigDecimal(100)
      val referenceNumberUCR = "bar"

      "when there are discrepancies" - {
        "when values defined in IE043" in {
          forAll(arbitrary[HouseConsignmentType04]) {
            houseConsignment =>
              val ie043 = houseConsignment.copy(
                sequenceNumber = sequenceNumber,
                grossMass = 50,
                referenceNumberUCR = Some("foo")
              )

              val userAnswers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, false)
                .setSequenceNumber(HouseConsignmentSection(Index(0)), 1)
                .setValue(GrossWeightPage(Index(0)), grossMass)
                .setValue(UniqueConsignmentReferencePage(Index(0)), referenceNumberUCR)

              val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
              val result = getResult(userAnswers, reads)

              result.value.grossMass.value mustEqual grossMass
              result.value.referenceNumberUCR.value mustEqual referenceNumberUCR
          }
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              grossMass = grossMass,
              referenceNumberUCR = Some(referenceNumberUCR)
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setSequenceNumber(HouseConsignmentSection(Index(0)), 1)
              .setValue(GrossWeightPage(Index(0)), grossMass)
              .setValue(UniqueConsignmentReferencePage(Index(0)), referenceNumberUCR)

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create departure transport means" - {
      import pages.houseConsignment.index.departureMeansOfTransport.*
      import pages.sections.houseConsignment.index.departureTransportMeans.*

      "when there are discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val departureTransportMeans = Seq(
              DepartureTransportMeansType01(
                sequenceNumber = 1,
                typeOfIdentification = "originalTypeOfIdentification1",
                identificationNumber = "originalIdentificationNumber1",
                nationality = "originalNationality1"
              ),
              DepartureTransportMeansType01(
                sequenceNumber = 2,
                typeOfIdentification = "originalTypeOfIdentification2",
                identificationNumber = "originalIdentificationNumber2",
                nationality = "originalNationality2"
              ),
              DepartureTransportMeansType01(
                sequenceNumber = 3,
                typeOfIdentification = "originalTypeOfIdentification3",
                identificationNumber = "originalIdentificationNumber3",
                nationality = "originalNationality3"
              ),
              DepartureTransportMeansType01(
                sequenceNumber = 4,
                typeOfIdentification = "originalTypeOfIdentification4",
                identificationNumber = "originalIdentificationNumber4",
                nationality = "originalNationality4"
              )
            )
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              DepartureTransportMeans = departureTransportMeans
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(HouseConsignmentSection(Index(0)))
              .setSequenceNumber(HouseConsignmentSection(Index(0)), BigInt(1))
              // Departure transport means 1 - Changed type value
              .setSequenceNumber(TransportMeansSection(Index(0), Index(0)), 1)
              .setNotRemoved(TransportMeansSection(Index(0), Index(0)))
              .setValue(TransportMeansIdentificationPage(Index(0), Index(0)),
                        TransportMeansIdentification("newTypeOfIdentification1", "newTypeOfIdentification1Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(0), Index(0)), "originalIdentificationNumber1")
              .setValue(CountryPage(Index(0), Index(0)), Country("originalNationality1", "originalNationalityDescription1"))
              // Departure transport means 2 - Changed identification number and nationality
              .setSequenceNumber(TransportMeansSection(Index(0), Index(1)), 2)
              .setNotRemoved(TransportMeansSection(Index(0), Index(1)))
              .setValue(
                TransportMeansIdentificationPage(Index(0), Index(1)),
                TransportMeansIdentification("originalTypeOfIdentification2", "originalTypeOfIdentification2Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(0), Index(1)), "newIdentificationNumber2")
              .setValue(CountryPage(Index(0), Index(1)), Country("newNationality2", "newNationalityDescription2"))
              // Departure transport means 3 - Removed
              .setSequenceNumber(TransportMeansSection(Index(0), Index(2)), 3)
              .setRemoved(TransportMeansSection(Index(0), Index(2)))
              // Departure transport means 4 - Unchanged
              .setSequenceNumber(TransportMeansSection(Index(0), Index(3)), 4)
              .setNotRemoved(TransportMeansSection(Index(0), Index(3)))
              .setValue(
                TransportMeansIdentificationPage(Index(0), Index(3)),
                TransportMeansIdentification("originalTypeOfIdentification4", "originalTypeOfIdentification4Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(0), Index(3)), "originalIdentificationNumber4")
              .setValue(CountryPage(Index(0), Index(3)), Country("originalNationality4", "originalNationalityDescription4"))
              // Departure transport means 5 - Added
              .setValue(TransportMeansIdentificationPage(Index(0), Index(4)),
                        TransportMeansIdentification("newTypeOfIdentification5", "newTypeOfIdentification5Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(0), Index(4)), "newIdentificationNumber5")
              .setValue(CountryPage(Index(0), Index(4)), Country("newNationality5", "newNationalityDescription5"))

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads).value.DepartureTransportMeans

            result mustEqual Seq(
              DepartureTransportMeansType02(
                sequenceNumber = 1,
                typeOfIdentification = Some("newTypeOfIdentification1"),
                identificationNumber = None,
                nationality = None
              ),
              DepartureTransportMeansType02(
                sequenceNumber = 2,
                typeOfIdentification = None,
                identificationNumber = Some("newIdentificationNumber2"),
                nationality = Some("newNationality2")
              ),
              DepartureTransportMeansType02(
                sequenceNumber = 3,
                typeOfIdentification = None,
                identificationNumber = None,
                nationality = None
              ),
              DepartureTransportMeansType02(
                sequenceNumber = 5,
                typeOfIdentification = Some("newTypeOfIdentification5"),
                identificationNumber = Some("newIdentificationNumber5"),
                nationality = Some("newNationality5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val departureTransportMeans = Seq(
              DepartureTransportMeansType01(
                sequenceNumber = 1,
                typeOfIdentification = "originalTypeOfIdentification1",
                identificationNumber = "originalIdentificationNumber1",
                nationality = "originalNationality1"
              ),
              DepartureTransportMeansType01(
                sequenceNumber = 2,
                typeOfIdentification = "originalTypeOfIdentification2",
                identificationNumber = "originalIdentificationNumber2",
                nationality = "originalNationality2"
              )
            )
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              DepartureTransportMeans = departureTransportMeans
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(HouseConsignmentSection(Index(0)))
              .setSequenceNumber(HouseConsignmentSection(Index(0)), BigInt(1))
              // Departure transport means 1 - Unchanged
              .setSequenceNumber(TransportMeansSection(Index(0), Index(0)), 1)
              .setNotRemoved(TransportMeansSection(Index(0), Index(0)))
              .setValue(
                TransportMeansIdentificationPage(Index(0), Index(0)),
                TransportMeansIdentification("originalTypeOfIdentification1", "originalTypeOfIdentification1Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(0), Index(0)), "originalIdentificationNumber1")
              // Departure transport means 2 - Unchanged
              .setSequenceNumber(TransportMeansSection(Index(0), Index(1)), 2)
              .setNotRemoved(TransportMeansSection(Index(0), Index(1)))
              .setValue(
                TransportMeansIdentificationPage(Index(0), Index(1)),
                TransportMeansIdentification("originalTypeOfIdentification2", "originalTypeOfIdentification2Description")
              )
              .setValue(VehicleIdentificationNumberPage(Index(0), Index(1)), "originalIdentificationNumber2")

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create supporting documents" - {
      import pages.houseConsignment.index.documents.*
      import pages.sections.houseConsignment.index.documents.*

      "when there are discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val supportingDocuments = Seq(
              SupportingDocumentType02(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1",
                complementOfInformation = Some("originalComplementOfInformation1")
              ),
              SupportingDocumentType02(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2",
                complementOfInformation = Some("originalComplementOfInformation2")
              ),
              SupportingDocumentType02(
                sequenceNumber = 3,
                typeValue = "originalTypeValue3",
                referenceNumber = "originalReferenceNumber3",
                complementOfInformation = Some("originalComplementOfInformation3")
              ),
              SupportingDocumentType02(
                sequenceNumber = 4,
                typeValue = "originalTypeValue4",
                referenceNumber = "originalReferenceNumber4",
                complementOfInformation = Some("originalComplementOfInformation4")
              )
            )
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              SupportingDocument = supportingDocuments
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(HouseConsignmentSection(Index(0)))
              .setSequenceNumber(HouseConsignmentSection(Index(0)), BigInt(1))
              // Supporting document 1 - Changed type value
              .setSequenceNumber(DocumentSection(Index(0), Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0), Index(0)))
              .setValue(TypePage(Index(0), Index(0)), DocumentType(DocType.Support, "newTypeValue1", "newTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0)), "originalReferenceNumber1")
              .setValue(AdditionalInformationPage(Index(0), Index(0)), "originalComplementOfInformation1")
              // Supporting document 2 - Changed reference number
              .setSequenceNumber(DocumentSection(Index(0), Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(0), Index(1)))
              .setValue(TypePage(Index(0), Index(1)), DocumentType(DocType.Support, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(1)), "newReferenceNumber2")
              .setValue(AdditionalInformationPage(Index(0), Index(1)), "newComplementOfInformation2")
              // Supporting document 3 - Removed
              .setSequenceNumber(DocumentSection(Index(0), Index(2)), 3)
              .setRemoved(DocumentSection(Index(0), Index(2)))
              .setValue(DocumentSection(Index(0), Index(2)), __ \ "type" \ "type", DocType.Support.display)
              // Supporting document 4 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(3)), 4)
              .setNotRemoved(DocumentSection(Index(0), Index(3)))
              .setValue(TypePage(Index(0), Index(3)), DocumentType(DocType.Support, "originalTypeValue4", "originalTypeValue4Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(3)), "originalReferenceNumber4")
              .setValue(AdditionalInformationPage(Index(0), Index(3)), "originalComplementOfInformation4")
              // Supporting document 5 - Added
              .setValue(TypePage(Index(0), Index(4)), DocumentType(DocType.Support, "newTypeValue5", "newTypeValue5Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(4)), "newReferenceNumber5")
              .setValue(AdditionalInformationPage(Index(0), Index(4)), "newComplementOfInformation5")

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads).value.SupportingDocument

            result mustEqual Seq(
              SupportingDocumentType04(
                sequenceNumber = 1,
                typeValue = Some("newTypeValue1"),
                referenceNumber = None,
                complementOfInformation = None
              ),
              SupportingDocumentType04(
                sequenceNumber = 2,
                typeValue = None,
                referenceNumber = Some("newReferenceNumber2"),
                complementOfInformation = Some("newComplementOfInformation2")
              ),
              SupportingDocumentType04(
                sequenceNumber = 3,
                typeValue = None,
                referenceNumber = None,
                complementOfInformation = None
              ),
              SupportingDocumentType04(
                sequenceNumber = 5,
                typeValue = Some("newTypeValue5"),
                referenceNumber = Some("newReferenceNumber5"),
                complementOfInformation = Some("newComplementOfInformation5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val supportingDocuments = Seq(
              SupportingDocumentType02(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1",
                complementOfInformation = Some("originalComplementOfInformation1")
              ),
              SupportingDocumentType02(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2",
                complementOfInformation = Some("originalComplementOfInformation2")
              )
            )
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              SupportingDocument = supportingDocuments
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(HouseConsignmentSection(Index(0)))
              .setSequenceNumber(HouseConsignmentSection(Index(0)), BigInt(1))
              // Supporting document 1 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0), Index(0)))
              .setValue(TypePage(Index(0), Index(0)), DocumentType(DocType.Support, "originalTypeValue1", "originalTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0)), "originalReferenceNumber1")
              .setValue(AdditionalInformationPage(Index(0), Index(0)), "originalComplementOfInformation1")
              // Supporting document 2 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(0), Index(1)))
              .setValue(TypePage(Index(0), Index(1)), DocumentType(DocType.Support, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(1)), "originalReferenceNumber2")
              .setValue(AdditionalInformationPage(Index(0), Index(1)), "originalComplementOfInformation2")

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create transport documents" - {
      import pages.houseConsignment.index.documents.*
      import pages.sections.houseConsignment.index.documents.*

      "when there are discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val transportDocuments = Seq(
              TransportDocumentType01(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1"
              ),
              TransportDocumentType01(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2"
              ),
              TransportDocumentType01(
                sequenceNumber = 3,
                typeValue = "originalTypeValue3",
                referenceNumber = "originalReferenceNumber3"
              ),
              TransportDocumentType01(
                sequenceNumber = 4,
                typeValue = "originalTypeValue4",
                referenceNumber = "originalReferenceNumber4"
              )
            )
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              TransportDocument = transportDocuments
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(HouseConsignmentSection(Index(0)))
              .setSequenceNumber(HouseConsignmentSection(Index(0)), BigInt(1))
              // Transport document 1 - Changed type value
              .setSequenceNumber(DocumentSection(Index(0), Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0), Index(0)))
              .setValue(TypePage(Index(0), Index(0)), DocumentType(DocType.Transport, "newTypeValue1", "newTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0)), "originalReferenceNumber1")
              // Transport document 2 - Changed reference number
              .setSequenceNumber(DocumentSection(Index(0), Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(0), Index(1)))
              .setValue(TypePage(Index(0), Index(1)), DocumentType(DocType.Transport, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(1)), "newReferenceNumber2")
              // Transport document 3 - Removed
              .setSequenceNumber(DocumentSection(Index(0), Index(2)), 3)
              .setRemoved(DocumentSection(Index(0), Index(2)))
              .setValue(DocumentSection(Index(0), Index(2)), __ \ "type" \ "type", DocType.Transport.display)
              // Transport document 4 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(3)), 4)
              .setNotRemoved(DocumentSection(Index(0), Index(3)))
              .setValue(TypePage(Index(0), Index(3)), DocumentType(DocType.Transport, "originalTypeValue4", "originalTypeValue4Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(3)), "originalReferenceNumber4")
              // Transport document 5 - Added
              .setValue(TypePage(Index(0), Index(4)), DocumentType(DocType.Transport, "newTypeValue5", "newTypeValue5Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(4)), "newReferenceNumber5")

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads).value.TransportDocument

            result mustEqual Seq(
              TransportDocumentType04(
                sequenceNumber = 1,
                typeValue = Some("newTypeValue1"),
                referenceNumber = None
              ),
              TransportDocumentType04(
                sequenceNumber = 2,
                typeValue = None,
                referenceNumber = Some("newReferenceNumber2")
              ),
              TransportDocumentType04(
                sequenceNumber = 3,
                typeValue = None,
                referenceNumber = None
              ),
              TransportDocumentType04(
                sequenceNumber = 5,
                typeValue = Some("newTypeValue5"),
                referenceNumber = Some("newReferenceNumber5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val transportDocuments = Seq(
              TransportDocumentType01(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1"
              ),
              TransportDocumentType01(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2"
              )
            )
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              TransportDocument = transportDocuments
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(HouseConsignmentSection(Index(0)))
              .setSequenceNumber(HouseConsignmentSection(Index(0)), BigInt(1))
              // Transport document 1 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0), Index(0)))
              .setValue(TypePage(Index(0), Index(0)), DocumentType(DocType.Transport, "originalTypeValue1", "originalTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0)), "originalReferenceNumber1")
              // Transport document 2 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(0), Index(1)))
              .setValue(TypePage(Index(0), Index(1)), DocumentType(DocType.Transport, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(1)), "originalReferenceNumber2")

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create additional references" - {
      import pages.houseConsignment.index.additionalReference.*
      import pages.sections.houseConsignment.index.additionalReference.AdditionalReferenceSection

      "when there are discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val additionalReferences = Seq(
              AdditionalReferenceType02(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = Some("originalReferenceNumber1")
              ),
              AdditionalReferenceType02(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = Some("originalReferenceNumber2")
              ),
              AdditionalReferenceType02(
                sequenceNumber = 3,
                typeValue = "originalTypeValue3",
                referenceNumber = Some("originalReferenceNumber3")
              ),
              AdditionalReferenceType02(
                sequenceNumber = 4,
                typeValue = "originalTypeValue4",
                referenceNumber = Some("originalReferenceNumber4")
              )
            )
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              AdditionalReference = additionalReferences
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(HouseConsignmentSection(Index(0)))
              .setSequenceNumber(HouseConsignmentSection(Index(0)), BigInt(1))
              // Additional reference 1 - Changed type value
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(0)), 1)
              .setNotRemoved(AdditionalReferenceSection(Index(0), Index(0)))
              .setValue(HouseConsignmentAdditionalReferenceTypePage(Index(0), Index(0)), AdditionalReferenceType("newTypeValue1", "newTypeValue1Description"))
              .setValue(HouseConsignmentAdditionalReferenceNumberPage(Index(0), Index(0)), "originalReferenceNumber1")
              // Additional reference 2 - Changed reference number
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(1)), 2)
              .setNotRemoved(AdditionalReferenceSection(Index(0), Index(1)))
              .setValue(HouseConsignmentAdditionalReferenceTypePage(Index(0), Index(1)),
                        AdditionalReferenceType("originalTypeValue2", "originalTypeValue2Description")
              )
              .setValue(HouseConsignmentAdditionalReferenceNumberPage(Index(0), Index(1)), "newReferenceNumber2")
              // Additional reference 3 - Removed
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(2)), 3)
              .setRemoved(AdditionalReferenceSection(Index(0), Index(2)))
              // Additional reference 4 - Unchanged
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(3)), 4)
              .setNotRemoved(AdditionalReferenceSection(Index(0), Index(3)))
              .setValue(HouseConsignmentAdditionalReferenceTypePage(Index(0), Index(3)),
                        AdditionalReferenceType("originalTypeValue4", "originalTypeValue4Description")
              )
              .setValue(HouseConsignmentAdditionalReferenceNumberPage(Index(0), Index(3)), "originalReferenceNumber4")
              // Additional reference 5 - Added
              .setValue(HouseConsignmentAdditionalReferenceTypePage(Index(0), Index(4)), AdditionalReferenceType("newTypeValue5", "newTypeValue5Description"))
              .setValue(HouseConsignmentAdditionalReferenceNumberPage(Index(0), Index(4)), "newReferenceNumber5")

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads).value.AdditionalReference

            result mustEqual Seq(
              AdditionalReferenceType03(
                sequenceNumber = 1,
                typeValue = Some("newTypeValue1"),
                referenceNumber = None
              ),
              AdditionalReferenceType03(
                sequenceNumber = 2,
                typeValue = None,
                referenceNumber = Some("newReferenceNumber2")
              ),
              AdditionalReferenceType03(
                sequenceNumber = 3,
                typeValue = None,
                referenceNumber = None
              ),
              AdditionalReferenceType03(
                sequenceNumber = 5,
                typeValue = Some("newTypeValue5"),
                referenceNumber = Some("newReferenceNumber5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val additionalReferences = Seq(
              AdditionalReferenceType02(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = Some("originalReferenceNumber1")
              ),
              AdditionalReferenceType02(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = Some("originalReferenceNumber2")
              )
            )
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              AdditionalReference = additionalReferences
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(HouseConsignmentSection(Index(0)))
              .setSequenceNumber(HouseConsignmentSection(Index(0)), BigInt(1))
              // Additional reference 1 - Unchanged
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(0)), 1)
              .setNotRemoved(AdditionalReferenceSection(Index(0), Index(0)))
              .setValue(HouseConsignmentAdditionalReferenceTypePage(Index(0), Index(0)),
                        AdditionalReferenceType("originalTypeValue1", "originalTypeValue1Description")
              )
              .setValue(HouseConsignmentAdditionalReferenceNumberPage(Index(0), Index(0)), "originalReferenceNumber1")
              // Additional reference 2 - Unchanged
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(1)), 2)
              .setNotRemoved(AdditionalReferenceSection(Index(0), Index(1)))
              .setValue(HouseConsignmentAdditionalReferenceTypePage(Index(0), Index(1)),
                        AdditionalReferenceType("originalTypeValue2", "originalTypeValue2Description")
              )
              .setValue(HouseConsignmentAdditionalReferenceNumberPage(Index(0), Index(1)), "originalReferenceNumber2")

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create consignment items" - {
      import pages.houseConsignment.index.items.*
      import pages.sections.ItemSection

      "when there are discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val consignmentItems = Seq(
              ConsignmentItemType04(
                goodsItemNumber = 1,
                declarationGoodsItemNumber = 1,
                Commodity = CommodityType09(
                  descriptionOfGoods = "originalDescriptionOfGoods1"
                )
              ),
              ConsignmentItemType04(
                goodsItemNumber = 2,
                declarationGoodsItemNumber = 2,
                Commodity = CommodityType09(
                  descriptionOfGoods = "originalDescriptionOfGoods2"
                )
              ),
              ConsignmentItemType04(
                goodsItemNumber = 3,
                declarationGoodsItemNumber = 3,
                Commodity = CommodityType09(
                  descriptionOfGoods = "originalDescriptionOfGoods3"
                )
              )
            )
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              ConsignmentItem = consignmentItems
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(HouseConsignmentSection(Index(0)))
              .setSequenceNumber(HouseConsignmentSection(Index(0)), BigInt(1))
              // Consignment item 1 - Changed description of goods
              .setSequenceNumber(ItemSection(Index(0), Index(0)), 1)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              .setValue(ItemDescriptionPage(Index(0), Index(0)), "newDescriptionOfGoods1")
              // Consignment item 2 - Removed
              .setSequenceNumber(ItemSection(Index(0), Index(1)), 2)
              .setRemoved(ItemSection(Index(0), Index(1)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
              // Consignment item 3 - Unchanged
              .setSequenceNumber(ItemSection(Index(0), Index(2)), 3)
              .setNotRemoved(ItemSection(Index(0), Index(2)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(2)), BigInt(3))
              .setValue(ItemDescriptionPage(Index(0), Index(2)), "originalDescriptionOfGoods3")
              // Consignment item 4 - Added
              .setValue(ItemDescriptionPage(Index(0), Index(3)), "newDescriptionOfGoods4")
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(3)), BigInt(4))
              // Consignment item 4 - Semi-added (user clicks Yes to add another item then clicks Back before adding a description)
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(4)), BigInt(5))

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads).value.ConsignmentItem

            result mustEqual Seq(
              ConsignmentItemType05(
                goodsItemNumber = 1,
                declarationGoodsItemNumber = 1,
                Commodity = Some(
                  CommodityType02(
                    descriptionOfGoods = Some("newDescriptionOfGoods1")
                  )
                )
              ),
              ConsignmentItemType05(
                goodsItemNumber = 2,
                declarationGoodsItemNumber = 2
              ),
              ConsignmentItemType05(
                goodsItemNumber = 4,
                declarationGoodsItemNumber = 4,
                Commodity = Some(
                  CommodityType02(
                    descriptionOfGoods = Some("newDescriptionOfGoods4")
                  )
                )
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[HouseConsignmentType04]) {
          houseConsignment =>
            val consignmentItems = Seq(
              ConsignmentItemType04(
                goodsItemNumber = 1,
                declarationGoodsItemNumber = 1,
                Commodity = CommodityType09(
                  descriptionOfGoods = "originalDescriptionOfGoods1"
                )
              ),
              ConsignmentItemType04(
                goodsItemNumber = 2,
                declarationGoodsItemNumber = 2,
                Commodity = CommodityType09(
                  descriptionOfGoods = "originalDescriptionOfGoods2"
                )
              )
            )
            val ie043 = houseConsignment.copy(
              sequenceNumber = sequenceNumber,
              ConsignmentItem = consignmentItems
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(HouseConsignmentSection(Index(0)))
              .setSequenceNumber(HouseConsignmentSection(Index(0)), BigInt(1))
              // Consignment item 1 - Unchanged
              .setSequenceNumber(ItemSection(Index(0), Index(0)), 1)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              .setValue(ItemDescriptionPage(Index(0), Index(0)), "originalDescriptionOfGoods1")
              // Consignment item 2 - Unchanged
              .setSequenceNumber(ItemSection(Index(0), Index(1)), 2)
              .setNotRemoved(ItemSection(Index(0), Index(1)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(1)), BigInt(2))
              .setValue(ItemDescriptionPage(Index(0), Index(1)), "originalDescriptionOfGoods2")

            val reads  = service.houseConsignmentReads(Seq(ie043))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }
  }

  "consignmentItemReads" - {
    import pages.houseConsignment.index.items.*
    import pages.sections.ItemSection

    val sequenceNumber = 1

    def getResult(userAnswers: UserAnswers, reads: Reads[Option[ConsignmentItemType05]]): Option[ConsignmentItemType05] =
      (userAnswers.data \ "Consignment" \ "HouseConsignment" \ 0 \ "ConsignmentItem" \ 0).get.as[Option[ConsignmentItemType05]](reads)

    "must create consignment item" - {

      val referenceNumberUCR = "bar"

      "when there are discrepancies" - {
        "when values defined in IE043" in {
          forAll(arbitrary[ConsignmentItemType04]) {
            consignmentItem =>
              val ie043 = consignmentItem.copy(
                goodsItemNumber = sequenceNumber,
                referenceNumberUCR = Some("foo")
              )

              val userAnswers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, false)
                .setNotRemoved(ItemSection(Index(0), Index(0)))
                .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
                .setValue(UniqueConsignmentReferencePage(Index(0), Index(0)), referenceNumberUCR)

              val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
              val result = getResult(userAnswers, reads).value

              result mustEqual ConsignmentItemType05(
                goodsItemNumber = sequenceNumber,
                declarationGoodsItemNumber = 1,
                referenceNumberUCR = Some(referenceNumberUCR)
              )
          }
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              referenceNumberUCR = Some(referenceNumberUCR)
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              .setValue(UniqueConsignmentReferencePage(Index(0), Index(0)), referenceNumberUCR)

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create commodity" - {

      "when there are discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val commodity = CommodityType09(
              descriptionOfGoods = "originalDescriptionOfGoods",
              cusCode = Some("originalCusCode"),
              CommodityCode = Some(
                CommodityCodeType05(
                  harmonizedSystemSubHeadingCode = "originalHarmonizedSystemSubHeadingCode",
                  combinedNomenclatureCode = Some("originalCombinedNomenclatureCode")
                )
              ),
              DangerousGoods = Nil,
              GoodsMeasure = Some(
                CUSTOM_GoodsMeasureType05(
                  grossMass = Some(100),
                  netMass = Some(50)
                )
              )
            )
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              Commodity = commodity
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              .setValue(ItemDescriptionPage(Index(0), Index(0)), "newDescriptionOfGoods")
              .setValue(CustomsUnionAndStatisticsCodePage(Index(0), Index(0)), "newCusCode")
              .setValue(CommodityCodePage(Index(0), Index(0)), "newHarmonizedSystemSubHeadingCode")
              .setValue(CombinedNomenclatureCodePage(Index(0), Index(0)), "newCombinedNomenclatureCode")
              .setValue(GrossWeightPage(Index(0), Index(0)), BigDecimal(1000))
              .setValue(NetWeightPage(Index(0), Index(0)), BigDecimal(500))

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads).value.Commodity.value

            result mustEqual CommodityType02(
              descriptionOfGoods = Some("newDescriptionOfGoods"),
              cusCode = Some("newCusCode"),
              CommodityCode = Some(
                CommodityCodeType01(
                  harmonizedSystemSubHeadingCode = "newHarmonizedSystemSubHeadingCode",
                  combinedNomenclatureCode = Some("newCombinedNomenclatureCode")
                )
              ),
              GoodsMeasure = Some(
                GoodsMeasureType02(
                  grossMass = Some(1000),
                  netMass = Some(500)
                )
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val commodity = CommodityType09(
              descriptionOfGoods = "originalDescriptionOfGoods",
              cusCode = Some("originalCusCode"),
              CommodityCode = Some(
                CommodityCodeType05(
                  harmonizedSystemSubHeadingCode = "originalHarmonizedSystemSubHeadingCode",
                  combinedNomenclatureCode = Some("originalCombinedNomenclatureCode")
                )
              ),
              DangerousGoods = Nil,
              GoodsMeasure = Some(
                CUSTOM_GoodsMeasureType05(
                  grossMass = Some(100),
                  netMass = Some(50)
                )
              )
            )
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              Commodity = commodity
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              .setValue(ItemDescriptionPage(Index(0), Index(0)), "originalDescriptionOfGoods")
              .setValue(CustomsUnionAndStatisticsCodePage(Index(0), Index(0)), "originalCusCode")
              .setValue(CommodityCodePage(Index(0), Index(0)), "originalHarmonizedSystemSubHeadingCode")
              .setValue(CombinedNomenclatureCodePage(Index(0), Index(0)), "originalCombinedNomenclatureCode")
              .setValue(GrossWeightPage(Index(0), Index(0)), BigDecimal(100))
              .setValue(NetWeightPage(Index(0), Index(0)), BigDecimal(50))

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create packaging" - {
      import pages.houseConsignment.index.items.packages.*
      import pages.sections.PackagingSection

      "when there are discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val packaging = Seq(
              PackagingType01(
                sequenceNumber = 1,
                typeOfPackages = "originalTypeOfPackages1",
                numberOfPackages = Some(100),
                shippingMarks = Some("originalShippingMarks1")
              ),
              PackagingType01(
                sequenceNumber = 2,
                typeOfPackages = "originalTypeOfPackages2",
                numberOfPackages = Some(200),
                shippingMarks = Some("originalShippingMarks2")
              ),
              PackagingType01(
                sequenceNumber = 3,
                typeOfPackages = "originalTypeOfPackages3",
                numberOfPackages = Some(300),
                shippingMarks = Some("originalShippingMarks3")
              ),
              PackagingType01(
                sequenceNumber = 4,
                typeOfPackages = "originalTypeOfPackages4",
                numberOfPackages = Some(400),
                shippingMarks = Some("originalShippingMarks4")
              )
            )
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              Packaging = packaging
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              // Packaging 1 - Changed type value
              .setSequenceNumber(PackagingSection(Index(0), Index(0), Index(0)), 1)
              .setNotRemoved(PackagingSection(Index(0), Index(0), Index(0)))
              .setValue(PackageTypePage(Index(0), Index(0), Index(0)), PackageType("newTypeOfPackages1", "newTypeOfPackages1Description"))
              .setValue(NumberOfPackagesPage(Index(0), Index(0), Index(0)), BigInt(100))
              .setValue(PackageShippingMarkPage(Index(0), Index(0), Index(0)), "originalShippingMarks1")
              // Packaging 2 - Changed number of packages and shipping marks
              .setSequenceNumber(PackagingSection(Index(0), Index(0), Index(1)), 2)
              .setNotRemoved(PackagingSection(Index(0), Index(0), Index(1)))
              .setValue(PackageTypePage(Index(0), Index(0), Index(1)), PackageType("originalTypeOfPackages2", "originalTypeOfPackages2Description"))
              .setValue(NumberOfPackagesPage(Index(0), Index(0), Index(1)), BigInt(2000))
              .setValue(PackageShippingMarkPage(Index(0), Index(0), Index(1)), "newShippingMarks2")
              // Packaging 3 - Removed
              .setSequenceNumber(PackagingSection(Index(0), Index(0), Index(2)), 3)
              .setRemoved(PackagingSection(Index(0), Index(0), Index(2)))
              // Packaging 4 - Unchanged
              .setSequenceNumber(PackagingSection(Index(0), Index(0), Index(3)), 4)
              .setNotRemoved(PackagingSection(Index(0), Index(0), Index(3)))
              .setValue(PackageTypePage(Index(0), Index(0), Index(3)), PackageType("originalTypeOfPackages4", "originalTypeOfPackages4Description"))
              .setValue(NumberOfPackagesPage(Index(0), Index(0), Index(3)), BigInt(400))
              .setValue(PackageShippingMarkPage(Index(0), Index(0), Index(3)), "originalShippingMarks4")
              // Packaging 5 - Added
              .setValue(PackageTypePage(Index(0), Index(0), Index(4)), PackageType("newTypeOfPackages5", "newTypeOfPackages5Description"))
              .setValue(NumberOfPackagesPage(Index(0), Index(0), Index(4)), BigInt(5000))
              .setValue(PackageShippingMarkPage(Index(0), Index(0), Index(4)), "newShippingMarks5")

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads).value.Packaging

            result mustEqual Seq(
              PackagingType02(
                sequenceNumber = 1,
                typeOfPackages = Some("newTypeOfPackages1"),
                numberOfPackages = None,
                shippingMarks = None
              ),
              PackagingType02(
                sequenceNumber = 2,
                typeOfPackages = None,
                numberOfPackages = Some(2000),
                shippingMarks = Some("newShippingMarks2")
              ),
              PackagingType02(
                sequenceNumber = 3,
                typeOfPackages = None,
                numberOfPackages = None,
                shippingMarks = None
              ),
              PackagingType02(
                sequenceNumber = 5,
                typeOfPackages = Some("newTypeOfPackages5"),
                numberOfPackages = Some(5000),
                shippingMarks = Some("newShippingMarks5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val packaging = Seq(
              PackagingType01(
                sequenceNumber = 1,
                typeOfPackages = "originalTypeOfPackages1",
                numberOfPackages = Some(100),
                shippingMarks = Some("originalShippingMarks1")
              ),
              PackagingType01(
                sequenceNumber = 2,
                typeOfPackages = "originalTypeOfPackages2",
                numberOfPackages = Some(200),
                shippingMarks = Some("originalShippingMarks2")
              )
            )
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              Packaging = packaging
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              // Packaging 1 - Unchanged
              .setSequenceNumber(PackagingSection(Index(0), Index(0), Index(0)), 1)
              .setNotRemoved(PackagingSection(Index(0), Index(0), Index(0)))
              .setValue(PackageTypePage(Index(0), Index(0), Index(0)), PackageType("originalTypeOfPackages1", "originalTypeOfPackages1Description"))
              .setValue(NumberOfPackagesPage(Index(0), Index(0), Index(0)), BigInt(100))
              .setValue(PackageShippingMarkPage(Index(0), Index(0), Index(0)), "originalShippingMarks1")
              // Packaging 2 - Unchanged
              .setSequenceNumber(PackagingSection(Index(0), Index(0), Index(1)), 2)
              .setNotRemoved(PackagingSection(Index(0), Index(0), Index(1)))
              .setValue(PackageTypePage(Index(0), Index(0), Index(1)), PackageType("originalTypeOfPackages2", "originalTypeOfPackages2Description"))
              .setValue(NumberOfPackagesPage(Index(0), Index(0), Index(1)), BigInt(200))
              .setValue(PackageShippingMarkPage(Index(0), Index(0), Index(1)), "originalShippingMarks2")

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create supporting documents" - {
      import pages.houseConsignment.index.items.document.*
      import pages.sections.houseConsignment.index.items.documents.*

      "when there are discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val supportingDocuments = Seq(
              SupportingDocumentType02(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1",
                complementOfInformation = Some("originalComplementOfInformation1")
              ),
              SupportingDocumentType02(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2",
                complementOfInformation = Some("originalComplementOfInformation2")
              ),
              SupportingDocumentType02(
                sequenceNumber = 3,
                typeValue = "originalTypeValue3",
                referenceNumber = "originalReferenceNumber3",
                complementOfInformation = Some("originalComplementOfInformation3")
              ),
              SupportingDocumentType02(
                sequenceNumber = 4,
                typeValue = "originalTypeValue4",
                referenceNumber = "originalReferenceNumber4",
                complementOfInformation = Some("originalComplementOfInformation4")
              )
            )
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              SupportingDocument = supportingDocuments
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              // Supporting document 1 - Changed type value
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0), Index(0), Index(0)))
              .setValue(TypePage(Index(0), Index(0), Index(0)), DocumentType(DocType.Support, "newTypeValue1", "newTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(0)), "originalReferenceNumber1")
              .setValue(AdditionalInformationPage(Index(0), Index(0), Index(0)), "originalComplementOfInformation1")
              // Supporting document 2 - Changed reference number
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(0), Index(0), Index(1)))
              .setValue(TypePage(Index(0), Index(0), Index(1)), DocumentType(DocType.Support, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(1)), "newReferenceNumber2")
              .setValue(AdditionalInformationPage(Index(0), Index(0), Index(1)), "newComplementOfInformation2")
              // Supporting document 3 - Removed
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(2)), 3)
              .setRemoved(DocumentSection(Index(0), Index(0), Index(2)))
              .setValue(DocumentSection(Index(0), Index(0), Index(2)), __ \ "type" \ "type", DocType.Support.display)
              // Supporting document 4 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(3)), 4)
              .setNotRemoved(DocumentSection(Index(0), Index(0), Index(3)))
              .setValue(TypePage(Index(0), Index(0), Index(3)), DocumentType(DocType.Support, "originalTypeValue4", "originalTypeValue4Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(3)), "originalReferenceNumber4")
              .setValue(AdditionalInformationPage(Index(0), Index(0), Index(3)), "originalComplementOfInformation4")
              // Supporting document 5 - Added
              .setValue(TypePage(Index(0), Index(0), Index(4)), DocumentType(DocType.Support, "newTypeValue5", "newTypeValue5Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(4)), "newReferenceNumber5")
              .setValue(AdditionalInformationPage(Index(0), Index(0), Index(4)), "newComplementOfInformation5")

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads).value.SupportingDocument

            result mustEqual Seq(
              SupportingDocumentType04(
                sequenceNumber = 1,
                typeValue = Some("newTypeValue1"),
                referenceNumber = None,
                complementOfInformation = None
              ),
              SupportingDocumentType04(
                sequenceNumber = 2,
                typeValue = None,
                referenceNumber = Some("newReferenceNumber2"),
                complementOfInformation = Some("newComplementOfInformation2")
              ),
              SupportingDocumentType04(
                sequenceNumber = 3,
                typeValue = None,
                referenceNumber = None,
                complementOfInformation = None
              ),
              SupportingDocumentType04(
                sequenceNumber = 5,
                typeValue = Some("newTypeValue5"),
                referenceNumber = Some("newReferenceNumber5"),
                complementOfInformation = Some("newComplementOfInformation5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val supportingDocuments = Seq(
              SupportingDocumentType02(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1",
                complementOfInformation = Some("originalComplementOfInformation1")
              ),
              SupportingDocumentType02(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2",
                complementOfInformation = Some("originalComplementOfInformation2")
              )
            )
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              SupportingDocument = supportingDocuments
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              // Supporting document 1 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0), Index(0), Index(0)))
              .setValue(TypePage(Index(0), Index(0), Index(0)), DocumentType(DocType.Support, "originalTypeValue1", "originalTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(0)), "originalReferenceNumber1")
              .setValue(AdditionalInformationPage(Index(0), Index(0), Index(0)), "originalComplementOfInformation1")
              // Supporting document 2 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(0), Index(0), Index(1)))
              .setValue(TypePage(Index(0), Index(0), Index(1)), DocumentType(DocType.Support, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(1)), "originalReferenceNumber2")
              .setValue(AdditionalInformationPage(Index(0), Index(0), Index(1)), "originalComplementOfInformation2")

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create transport documents" - {
      import pages.houseConsignment.index.items.document.*
      import pages.sections.houseConsignment.index.items.documents.*

      "when there are discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val transportDocuments = Seq(
              TransportDocumentType01(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1"
              ),
              TransportDocumentType01(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2"
              ),
              TransportDocumentType01(
                sequenceNumber = 3,
                typeValue = "originalTypeValue3",
                referenceNumber = "originalReferenceNumber3"
              ),
              TransportDocumentType01(
                sequenceNumber = 4,
                typeValue = "originalTypeValue4",
                referenceNumber = "originalReferenceNumber4"
              )
            )
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              TransportDocument = transportDocuments
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              // Transport document 1 - Changed type value
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0), Index(0), Index(0)))
              .setValue(TypePage(Index(0), Index(0), Index(0)), DocumentType(DocType.Transport, "newTypeValue1", "newTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(0)), "originalReferenceNumber1")
              // Transport document 2 - Changed reference number
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(0), Index(0), Index(1)))
              .setValue(TypePage(Index(0), Index(0), Index(1)), DocumentType(DocType.Transport, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(1)), "newReferenceNumber2")
              // Transport document 3 - Removed
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(2)), 3)
              .setRemoved(DocumentSection(Index(0), Index(0), Index(2)))
              .setValue(DocumentSection(Index(0), Index(0), Index(2)), __ \ "type" \ "type", DocType.Transport.display)
              // Transport document 4 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(3)), 4)
              .setNotRemoved(DocumentSection(Index(0), Index(0), Index(3)))
              .setValue(TypePage(Index(0), Index(0), Index(3)), DocumentType(DocType.Transport, "originalTypeValue4", "originalTypeValue4Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(3)), "originalReferenceNumber4")
              // Transport document 5 - Added
              .setValue(TypePage(Index(0), Index(0), Index(4)), DocumentType(DocType.Transport, "newTypeValue5", "newTypeValue5Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(4)), "newReferenceNumber5")

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads).value.TransportDocument

            result mustEqual Seq(
              TransportDocumentType04(
                sequenceNumber = 1,
                typeValue = Some("newTypeValue1"),
                referenceNumber = None
              ),
              TransportDocumentType04(
                sequenceNumber = 2,
                typeValue = None,
                referenceNumber = Some("newReferenceNumber2")
              ),
              TransportDocumentType04(
                sequenceNumber = 3,
                typeValue = None,
                referenceNumber = None
              ),
              TransportDocumentType04(
                sequenceNumber = 5,
                typeValue = Some("newTypeValue5"),
                referenceNumber = Some("newReferenceNumber5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val transportDocuments = Seq(
              TransportDocumentType01(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = "originalReferenceNumber1"
              ),
              TransportDocumentType01(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = "originalReferenceNumber2"
              )
            )
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              TransportDocument = transportDocuments
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              // Transport document 1 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(0)), 1)
              .setNotRemoved(DocumentSection(Index(0), Index(0), Index(0)))
              .setValue(TypePage(Index(0), Index(0), Index(0)), DocumentType(DocType.Transport, "originalTypeValue1", "originalTypeValue1Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(0)), "originalReferenceNumber1")
              // Transport document 2 - Unchanged
              .setSequenceNumber(DocumentSection(Index(0), Index(0), Index(1)), 2)
              .setNotRemoved(DocumentSection(Index(0), Index(0), Index(1)))
              .setValue(TypePage(Index(0), Index(0), Index(1)), DocumentType(DocType.Transport, "originalTypeValue2", "originalTypeValue2Description"))
              .setValue(DocumentReferenceNumberPage(Index(0), Index(0), Index(1)), "originalReferenceNumber2")

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }

    "must create additional references" - {
      import pages.houseConsignment.index.items.additionalReference.*
      import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferenceSection

      "when there are discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val additionalReferences = Seq(
              AdditionalReferenceType01(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = Some("originalReferenceNumber1")
              ),
              AdditionalReferenceType01(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = Some("originalReferenceNumber2")
              ),
              AdditionalReferenceType01(
                sequenceNumber = 3,
                typeValue = "originalTypeValue3",
                referenceNumber = Some("originalReferenceNumber3")
              ),
              AdditionalReferenceType01(
                sequenceNumber = 4,
                typeValue = "originalTypeValue4",
                referenceNumber = Some("originalReferenceNumber4")
              )
            )
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              AdditionalReference = additionalReferences
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              // Additional reference 1 - Changed type value
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(0), Index(0)), 1)
              .setNotRemoved(AdditionalReferenceSection(Index(0), Index(0), Index(0)))
              .setValue(AdditionalReferenceTypePage(Index(0), Index(0), Index(0)), AdditionalReferenceType("newTypeValue1", "newTypeValue1Description"))
              .setValue(AdditionalReferenceNumberPage(Index(0), Index(0), Index(0)), "originalReferenceNumber1")
              // Additional reference 2 - Changed reference number
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(0), Index(1)), 2)
              .setNotRemoved(AdditionalReferenceSection(Index(0), Index(0), Index(1)))
              .setValue(AdditionalReferenceTypePage(Index(0), Index(0), Index(1)),
                        AdditionalReferenceType("originalTypeValue2", "originalTypeValue2Description")
              )
              .setValue(AdditionalReferenceNumberPage(Index(0), Index(0), Index(1)), "newReferenceNumber2")
              // Additional reference 3 - Removed
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(0), Index(2)), 3)
              .setRemoved(AdditionalReferenceSection(Index(0), Index(0), Index(2)))
              // Additional reference 4 - Unchanged
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(0), Index(3)), 4)
              .setNotRemoved(AdditionalReferenceSection(Index(0), Index(0), Index(3)))
              .setValue(AdditionalReferenceTypePage(Index(0), Index(0), Index(3)),
                        AdditionalReferenceType("originalTypeValue4", "originalTypeValue4Description")
              )
              .setValue(AdditionalReferenceNumberPage(Index(0), Index(0), Index(3)), "originalReferenceNumber4")
              // Additional reference 5 - Added
              .setValue(AdditionalReferenceTypePage(Index(0), Index(0), Index(4)), AdditionalReferenceType("newTypeValue5", "newTypeValue5Description"))
              .setValue(AdditionalReferenceNumberPage(Index(0), Index(0), Index(4)), "newReferenceNumber5")

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads).value.AdditionalReference

            result mustEqual Seq(
              AdditionalReferenceType03(
                sequenceNumber = 1,
                typeValue = Some("newTypeValue1"),
                referenceNumber = None
              ),
              AdditionalReferenceType03(
                sequenceNumber = 2,
                typeValue = None,
                referenceNumber = Some("newReferenceNumber2")
              ),
              AdditionalReferenceType03(
                sequenceNumber = 3,
                typeValue = None,
                referenceNumber = None
              ),
              AdditionalReferenceType03(
                sequenceNumber = 5,
                typeValue = Some("newTypeValue5"),
                referenceNumber = Some("newReferenceNumber5")
              )
            )
        }
      }

      "when there are no discrepancies" in {
        forAll(arbitrary[ConsignmentItemType04]) {
          consignmentItem =>
            val additionalReferences = Seq(
              AdditionalReferenceType01(
                sequenceNumber = 1,
                typeValue = "originalTypeValue1",
                referenceNumber = Some("originalReferenceNumber1")
              ),
              AdditionalReferenceType01(
                sequenceNumber = 2,
                typeValue = "originalTypeValue2",
                referenceNumber = Some("originalReferenceNumber2")
              )
            )
            val ie043 = consignmentItem.copy(
              goodsItemNumber = sequenceNumber,
              AdditionalReference = additionalReferences
            )

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, false)
              .setNotRemoved(ItemSection(Index(0), Index(0)))
              .setValue(DeclarationGoodsItemNumberPage(Index(0), Index(0)), BigInt(1))
              // Additional reference 1 - Unchanged
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(0), Index(0)), 1)
              .setNotRemoved(AdditionalReferenceSection(Index(0), Index(0), Index(0)))
              .setValue(AdditionalReferenceTypePage(Index(0), Index(0), Index(0)),
                        AdditionalReferenceType("originalTypeValue1", "originalTypeValue1Description")
              )
              .setValue(AdditionalReferenceNumberPage(Index(0), Index(0), Index(0)), "originalReferenceNumber1")
              // Additional reference 2 - Unchanged
              .setSequenceNumber(AdditionalReferenceSection(Index(0), Index(0), Index(1)), 2)
              .setNotRemoved(AdditionalReferenceSection(Index(0), Index(0), Index(1)))
              .setValue(AdditionalReferenceTypePage(Index(0), Index(0), Index(1)),
                        AdditionalReferenceType("originalTypeValue2", "originalTypeValue2Description")
              )
              .setValue(AdditionalReferenceNumberPage(Index(0), Index(0), Index(1)), "originalReferenceNumber2")

            val reads  = service.consignmentItemReads(Seq(ie043))(Index(0))(Index(0), sequenceNumber)
            val result = getResult(userAnswers, reads)

            result must not be defined
        }
      }
    }
  }
}
