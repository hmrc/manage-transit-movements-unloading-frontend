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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.messages._
import models.{Index, Seal, Seals, UnloadingPermission}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.{SealPage, VehicleNameRegistrationReferencePage}

import java.time.LocalDateTime

class UnloadingRemarksRequestServiceSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  import UnloadingRemarksRequestServiceSpec._

  "UnloadingRemarksRequestServiceSpec" - {

    "return UnloadingRemarksRequest" - {

      "when unloading remarks conform with no seals" in {

        val unloadingRemarksRequestService = app.injector.instanceOf[UnloadingRemarksRequestService]

        forAll(
          arbitrary[UnloadingPermission],
          arbitrary[Meta],
          arbitrary[LocalDateTime],
          Gen.option(stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength))
        ) {
          (unloadingPermission, meta, localDateTime, unloadingRemark) =>
            val unloadingRemarks = RemarksConform(localDateTime.toLocalDate, unloadingRemark)

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, emptyUserAnswers) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.traderAtDestination,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                Nil,
                seals = None
              )
        }
      }

      "when unloading remarks conform with seals" in {

        val unloadingRemarksRequestService = app.injector.instanceOf[UnloadingRemarksRequestService]

        forAll(
          arbitrary[UnloadingPermission],
          arbitrary[Meta],
          arbitrary[LocalDateTime],
          Gen.option(stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength))
        ) {
          (unloadingPermission, meta, localDateTime, unloadingRemark) =>
            val unloadingRemarks = RemarksConformWithSeals(localDateTime.toLocalDate, unloadingRemark)

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, emptyUserAnswers) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.traderAtDestination,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                Nil,
                seals = unloadingPermission.seals
              )
        }
      }

      "when unloading remarks don't conform and stateOfSeals is None" in {

        val unloadingRemarksRequestService = app.injector.instanceOf[UnloadingRemarksRequestService]

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksNonConform(
              None,
              Some("unloading remarks"),
              localDateTime.toLocalDate
            )

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, emptyUserAnswers) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.traderAtDestination,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                Nil,
                seals = unloadingPermission.seals
              )
        }
      }

      "when unloading remarks don't conform and stateOfSeals is OK" in {

        val unloadingRemarksRequestService = app.injector.instanceOf[UnloadingRemarksRequestService]

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksNonConform(
              stateOfSeals = Some(1),
              Some("unloading remarks"),
              localDateTime.toLocalDate
            )

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, emptyUserAnswers) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.traderAtDestination,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                Nil,
                seals = unloadingPermission.seals
              )
        }
      }

      "when unloading remarks don't conform and stateOfSeals is NOT OK" in {

        val unloadingRemarksRequestService = app.injector.instanceOf[UnloadingRemarksRequestService]

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksNonConform(
              stateOfSeals = Some(0),
              Some("unloading remarks"),
              localDateTime.toLocalDate
            )

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, emptyUserAnswers) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.traderAtDestination,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                Nil,
                seals = unloadingPermission.seals
              )
        }
      }

      "when unloading remarks don't conform and stateOfSeals is NOT OK and seals exist in UserAnswers" in {

        val unloadingRemarksRequestService = app.injector.instanceOf[UnloadingRemarksRequestService]

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksNonConform(
              stateOfSeals = Some(0),
              Some("unloading remarks"),
              localDateTime.toLocalDate
            )

            val userAnswersUpdated = emptyUserAnswers
              .setValue(SealPage(Index(0)), Seal("seal 2", removable = false))
              .setValue(SealPage(Index(1)), Seal("seal 1", removable = false))
              .setValue(SealPage(Index(2)), Seal("seal 3", removable = false))

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswersUpdated) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.traderAtDestination,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                Seq(ResultsOfControlSealsUpdated),
                seals = Some(Seals(Seq("seal 2", "seal 1", "seal 3")))
              )
        }
      }

      "when unloading remarks don't conform and stateOfSeals is NOT OK and seals and result control exist in UserAnswers" in {

        val unloadingRemarksRequestService = app.injector.instanceOf[UnloadingRemarksRequestService]

        forAll(arbitrary[UnloadingPermission], arbitrary[Meta], arbitrary[LocalDateTime]) {
          (unloadingPermission, meta, localDateTime) =>
            val unloadingRemarks = RemarksNonConform(
              stateOfSeals = Some(0),
              Some("unloading remarks"),
              localDateTime.toLocalDate
            )

            val userAnswersUpdated = emptyUserAnswers
              .setValue(SealPage(Index(0)), Seal("seal 2", removable = false))
              .setValue(SealPage(Index(1)), Seal("seal 1", removable = false))
              .setValue(SealPage(Index(2)), Seal("seal 3", removable = false))
              .setValue(VehicleNameRegistrationReferencePage, "reference")

            unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswersUpdated) mustBe
              UnloadingRemarksRequest(
                meta,
                header(unloadingPermission),
                unloadingPermission.traderAtDestination,
                unloadingPermission.presentationOffice,
                unloadingRemarks,
                Seq(
                  ResultsOfControlDifferentValues(
                    PointerToAttribute(TransportIdentity),
                    "reference"
                  ),
                  ResultsOfControlSealsUpdated
                ),
                seals = Some(Seals(Seq("seal 2", "seal 1", "seal 3")))
              )
        }
      }
    }
  }
}

object UnloadingRemarksRequestServiceSpec {

  val header: UnloadingPermission => Header = unloadingPermission =>
    Header(
      movementReferenceNumber = unloadingPermission.movementReferenceNumber,
      transportIdentity = unloadingPermission.transportIdentity,
      transportCountry = unloadingPermission.transportCountry,
      numberOfItems = unloadingPermission.numberOfItems,
      numberOfPackages = unloadingPermission.numberOfPackages,
      grossMass = unloadingPermission.grossMass
    )
}
