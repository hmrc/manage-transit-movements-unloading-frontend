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

package services

import base.SpecBase
import generators.Generators
import models.messages._
import models.{Index, Seal, Seals, UnloadingPermission, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen.choose
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

import java.time.{LocalDate, ZoneOffset}

class RemarksServiceSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val dateGoodsUnloaded = LocalDate.now(ZoneOffset.UTC)

  private val service = new RemarksServiceImpl()

  private def genDecimal(min: Double, max: Double): Gen[BigDecimal] =
    Gen.choose(min, max).map(BigDecimal(_).bigDecimal.setScale(3, BigDecimal.RoundingMode.DOWN))

  "RemarksServiceSpec" - {

    "must handle" - {

      "when unloading date doesn't exist" in {

        val unloadingPermissionObject = arbitrary[UnloadingPermission]

        val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

        service.build(emptyUserAnswers, unloadingPermission).failure.exception.getMessage mustBe
          "date goods unloaded not found"
      }

      "when there are seals" in {

        forAll(arbitrary[UnloadingPermission], stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength)) {
          (unloadingPermission, unloadingRemarks) =>
            val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(Seq("seal 1", "seal 2"))))

            val userAnswers = emptyUserAnswers
              .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
              .setValue(ChangesToReportPage, unloadingRemarks)

            service.build(userAnswers, unloadingPermissionWithSeals).get mustBe
              RemarksConformWithSeals(unloadingRemark = Some(unloadingRemarks), unloadingDate = dateGoodsUnloaded)
        }
      }

      "when there are no seals" in {

        forAll(arbitrary[UnloadingPermission], stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength)) {
          (unloadingPermission, unloadingRemarks) =>
            val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None)

            val userAnswers = emptyUserAnswers
              .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
              .setValue(ChangesToReportPage, unloadingRemarks)

            service.build(userAnswers, unloadingPermissionWithNoSeals).get mustBe
              RemarksConform(unloadingRemark = Some(unloadingRemarks), unloadingDate = dateGoodsUnloaded)
        }
      }

      "gross mass has been updated" - {

        "without seals" in {
          forAll(
            arbitrary[UnloadingPermission],
            genDecimal(0.0, 50000.999),
            genDecimal(60000.000, 99999999.999)
          ) {
            (unloadingPermission, GrossWeightUnloading, GrossWeight) =>
              val unloadingPermissionUpdated = unloadingPermission.copy(seals = None, GrossWeight = GrossWeightUnloading.toString)

              val userAnswers = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(GrossWeightPage, GrossWeight.toString())

              service.build(userAnswers, unloadingPermissionUpdated).get mustBe
                RemarksNonConform(stateOfSeals = None, unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
          }
        }

        "with seals" in {

          forAll(
            arbitrary[UnloadingPermission],
            genDecimal(0.0, 50000.999),
            genDecimal(60000.000, 99999999.999)
          ) {
            (unloadingPermission, GrossWeightUnloading, GrossWeight) =>
              val unloadingPermissionWithNoSeals =
                unloadingPermission.copy(GrossWeight = GrossWeightUnloading.toString(), seals = Some(Seals(Seq("seal 1", "seal 2"))))

              val userAnswers = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(GrossWeightPage, GrossWeight.toString())

              service.build(userAnswers, unloadingPermissionWithNoSeals).get mustBe
                RemarksNonConform(stateOfSeals = Some(1), unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
          }
        }

        "and has same value as unloading permission" in {

          forAll(
            arbitrary[UnloadingPermission],
            genDecimal(0.0, 50000.999)
          ) {
            (unloadingPermission, GrossWeight) =>
              val unloadingPermissionWithNoSeals =
                unloadingPermission.copy(GrossWeight = GrossWeight.toString(), seals = Some(Seals(Seq("seal 1", "seal 2"))))

              val userAnswers = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(GrossWeightPage, GrossWeight.toString())

              service.build(userAnswers, unloadingPermissionWithNoSeals).get mustBe
                RemarksConformWithSeals(unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
          }
        }
      }

      "number of items has been updated" - {

        "without seals" in {
          forAll(
            arbitrary[UnloadingPermission],
            choose(min = 1: Int, 49: Int),
            choose(min = 50: Int, 100: Int)
          ) {
            (unloadingPermission, numberOfItemsUnloadingPermission, numberOfItemsUpdated) =>
              val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None, numberOfItems = numberOfItemsUnloadingPermission)

              val userAnswers = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(TotalNumberOfItemsPage, numberOfItemsUpdated)

              service.build(userAnswers, unloadingPermissionWithNoSeals).get mustBe
                RemarksNonConform(stateOfSeals = None, unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
          }
        }

        "with seals" in {

          forAll(
            arbitrary[UnloadingPermission],
            choose(min = 1: Int, 49: Int),
            choose(min = 50: Int, 100: Int)
          ) {
            (unloadingPermission, numberOfItemsUnloadingPermission, numberOfItemsUpdated) =>
              val unloadingPermissionWithNoSeals =
                unloadingPermission.copy(seals = Some(Seals(Seq("seal 1", "seal 2"))), numberOfItems = numberOfItemsUnloadingPermission)

              val userAnswers = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(TotalNumberOfItemsPage, numberOfItemsUpdated)

              service.build(userAnswers, unloadingPermissionWithNoSeals).get mustBe
                RemarksNonConform(stateOfSeals = Some(1), unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
          }
        }

        "and has same value as unloading permission" in {
          forAll(
            arbitrary[UnloadingPermission],
            choose(min = 1: Int, 49: Int)
          ) {
            (unloadingPermission, numberOfItems) =>
              val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None, numberOfItems = numberOfItems)

              val userAnswers = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(TotalNumberOfItemsPage, numberOfItems)

              service.build(userAnswers, unloadingPermissionWithNoSeals).get mustBe
                RemarksConform(unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
          }
        }
      }

      "number of packages has been updated" - {

        "without seals" in {

          forAll(
            arbitrary[UnloadingPermission],
            choose(min = 1: Int, 49: Int),
            choose(min = 50: Int, 100: Int)
          ) {
            (unloadingPermission, numberOfPackagesUnloadingPermission, numberOfPackagesUpdated) =>
              val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None, numberOfPackages = Some(numberOfPackagesUnloadingPermission))

              val userAnswers = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(TotalNumberOfPackagesPage, numberOfPackagesUpdated)

              service.build(userAnswers, unloadingPermissionWithNoSeals).get mustBe
                RemarksNonConform(stateOfSeals = None, unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
          }
        }

        "with seals" in {

          forAll(
            arbitrary[UnloadingPermission],
            Gen.option(choose(min = 1: Int, 49: Int)),
            choose(min = 50: Int, 100: Int)
          ) {
            (unloadingPermission, numberOfPackagesUnloadingPermission, numberOfPackagesUpdated) =>
              val unloadingPermissionWithNoSeals =
                unloadingPermission.copy(seals = Some(Seals(Seq("seal 1", "seal 2"))), numberOfPackages = numberOfPackagesUnloadingPermission)

              val userAnswers = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(TotalNumberOfPackagesPage, numberOfPackagesUpdated)

              service.build(userAnswers, unloadingPermissionWithNoSeals).get mustBe
                RemarksNonConform(stateOfSeals = Some(1), unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
          }
        }

        "and has same value as unloading permission" in {

          forAll(
            arbitrary[UnloadingPermission],
            choose(min = 1: Int, 49: Int)
          ) {
            (unloadingPermission, numberOfPackages) =>
              val unloadingPermissionWithNoSeals =
                unloadingPermission.copy(seals = Some(Seals(Seq("seal 1", "seal 2"))), numberOfPackages = Some(numberOfPackages))

              val userAnswers = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(TotalNumberOfPackagesPage, numberOfPackages)

              service.build(userAnswers, unloadingPermissionWithNoSeals).get mustBe
                RemarksConformWithSeals(unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
          }
        }
      }

      "when seals" - {

        "have same values in unloading permission and user answers" in {

          forAll(arbitrary[UnloadingPermission]) {
            unloadingPermission =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(Seq("seal 1", "seal 2", "seal 3"))))

              val userAnswersUpdated = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(SealPage(Index(0)), Seal("seal 2", removable = false))
                .setValue(SealPage(Index(1)), Seal("seal 1", removable = false))
                .setValue(SealPage(Index(2)), Seal("seal 3", removable = false))

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).get mustBe
                RemarksConformWithSeals(dateGoodsUnloaded, None)
          }
        }

        "cannot be read" in {
          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (userAnswers, unloadingPermission) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(Seq("seal 1", "seal 2"))))

              val userAnswersUpdated = userAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(CanSealsBeReadPage, false)
                .setValue(AreAnySealsBrokenPage, false)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).get mustBe
                RemarksNonConform(
                  stateOfSeals = Some(0),
                  unloadingRemark = userAnswers.get(ChangesToReportPage),
                  unloadingDate = dateGoodsUnloaded
                )
          }
        }

        "are broken" in {
          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (userAnswers, unloadingPermission) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(Seq("seal 1", "seal 2"))))

              val userAnswersUpdated = userAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(CanSealsBeReadPage, true)
                .setValue(AreAnySealsBrokenPage, true)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).get mustBe
                RemarksNonConform(
                  stateOfSeals = Some(0),
                  unloadingRemark = userAnswersUpdated.get(ChangesToReportPage),
                  unloadingDate = dateGoodsUnloaded
                )
          }
        }

        "don't exist in unloading permission and user hasn't changed anything" in {

          forAll(stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength), arbitrary[UnloadingPermission]) {
            (unloadingRemarks, unloadingPermission) =>
              val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None)

              val userAnswersUpdated = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(ChangesToReportPage, unloadingRemarks)

              service.build(userAnswersUpdated, unloadingPermissionWithNoSeals).get mustBe
                RemarksConform(dateGoodsUnloaded, Some(unloadingRemarks))
          }
        }

        "exist in unloading permission and user hasn't changed anything" in {
          forAll(stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength), arbitrary[UnloadingPermission]) {
            (unloadingRemarks, unloadingPermission) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(Seq("seal 1", "seal 2"))))

              val userAnswersUpdated = emptyUserAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(ChangesToReportPage, unloadingRemarks)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).get mustBe
                RemarksConformWithSeals(dateGoodsUnloaded, Some(unloadingRemarks))
          }
        }

        "don't exist in unloading permission and user has added a new seal" in {

          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (userAnswers, unloadingPermission) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = None)

              val userAnswersUpdated = userAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(SealPage(Index(0)), Seal("new seal", removable = true))

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).get mustBe
                RemarksNonConform(
                  stateOfSeals = Some(0),
                  unloadingRemark = userAnswers.get(ChangesToReportPage),
                  unloadingDate = dateGoodsUnloaded
                )
          }
        }

        "have been updated" in {

          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (userAnswers, unloadingPermission) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(Seq("seal 1", "seal 2", "seal 3"))))

              val userAnswersUpdated = userAnswers
                .setValue(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .setValue(SealPage(Index(0)), Seal("seal 1", removable = false))
                .setValue(SealPage(Index(1)), Seal("updated seal", removable = false))
                .setValue(SealPage(Index(2)), Seal("seal 3", removable = false))

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).get mustBe
                RemarksNonConform(
                  stateOfSeals = Some(0),
                  unloadingRemark = userAnswers.get(ChangesToReportPage),
                  unloadingDate = dateGoodsUnloaded
                )
          }

        }

      }
    }
  }
}
