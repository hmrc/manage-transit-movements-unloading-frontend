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

import java.time.{LocalDate, ZoneOffset}

import base.SpecBase
import generators.Generators
import models.messages._
import models.{Index, Seals, UnloadingPermission, UserAnswers}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen.choose
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{ChangesToReportPage, _}

class RemarksServiceSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  private val dateGoodsUnloaded = LocalDate.now(ZoneOffset.UTC)

  private val mockResultOfControlService: ResultOfControlService = mock[ResultOfControlService]

  private val service = new RemarksServiceImpl(mockResultOfControlService)

  private def genDecimal(min: Double, max: Double): Gen[BigDecimal] =
    Gen.choose(min, max).map(BigDecimal(_).bigDecimal.setScale(3, BigDecimal.RoundingMode.DOWN))

  "RemarksServiceSpec" - {

    "must handle" - {

      "when unloading date doesn't exist" in {

        val unloadingPermissionObject = arbitrary[UnloadingPermission]

        val unloadingPermission: UnloadingPermission = unloadingPermissionObject.sample.get

        when(mockResultOfControlService.build(emptyUserAnswers, unloadingPermission)).thenReturn(Nil)

        val message = intercept[RuntimeException] {
          service.build(emptyUserAnswers, unloadingPermission).futureValue
        }

        message.getCause.getMessage mustBe "date goods unloaded not found"
      }

      "results of control exist with seals" in {

        forAll(arbitrary[UnloadingPermission], arbitrary[ResultsOfControl], stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength)) {
          (unloadingPermission, resultsOfControlValues, unloadingRemarks) =>
            val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

            val userAnswers = emptyUserAnswers
              .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
              .success
              .value
              .set(ChangesToReportPage, unloadingRemarks)
              .success
              .value

            when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

            service.build(userAnswers, unloadingPermissionWithSeals).futureValue mustBe
              RemarksConformWithSeals(unloadingRemark = Some(unloadingRemarks), unloadingDate = dateGoodsUnloaded)
        }
      }

      "results of control exist" - {

        "without seals" in {

          forAll(arbitrary[UnloadingPermission], arbitrary[ResultsOfControl], stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength)) {
            (unloadingPermission, resultsOfControlValues, unloadingRemarks) =>
              val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None)
              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(ChangesToReportPage, unloadingRemarks)
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksConform(unloadingRemark = Some(unloadingRemarks), unloadingDate = dateGoodsUnloaded)

          }
        }
      }

      "gross mass has been updated" - {

        "without seals" in {
          forAll(
            arbitrary[UnloadingPermission],
            arbitrary[ResultsOfControl],
            genDecimal(0.0, 50000.999),
            genDecimal(60000.000, 99999999.999)
          ) {
            (unloadingPermission, resultsOfControlValues, grossMassUnloading, grossMass) =>
              val unloadingPermissionUpdated = unloadingPermission.copy(seals = None, grossMass = grossMassUnloading.toString)
              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(GrossMassAmountPage, grossMass.toString())
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

              service.build(userAnswers, unloadingPermissionUpdated).futureValue mustBe
                RemarksNonConform(stateOfSeals = None, unloadingRemark = None, unloadingDate = dateGoodsUnloaded)

          }
        }

        "with seals" in {

          forAll(
            arbitrary[UnloadingPermission],
            arbitrary[ResultsOfControl],
            genDecimal(0.0, 50000.999),
            genDecimal(60000.000, 99999999.999)
          ) {
            (unloadingPermission, resultsOfControlValues, grossMassUnloading, grossMass) =>
              val unloadingPermissionWithNoSeals =
                unloadingPermission.copy(grossMass = grossMassUnloading.toString(), seals = Some(Seals(2, Seq("seal 1", "seal 2"))))
              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(GrossMassAmountPage, grossMass.toString())
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksNonConform(stateOfSeals = Some(1), unloadingRemark = None, unloadingDate = dateGoodsUnloaded)

          }
        }

        "and has same value as unloading permission" in {

          forAll(
            arbitrary[UnloadingPermission],
            arbitrary[ResultsOfControl],
            genDecimal(0.0, 50000.999)
          ) {
            (unloadingPermission, resultsOfControlValues, grossMass) =>
              val unloadingPermissionWithNoSeals =
                unloadingPermission.copy(grossMass = grossMass.toString(), seals = Some(Seals(2, Seq("seal 1", "seal 2"))))
              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(GrossMassAmountPage, grossMass.toString())
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksConformWithSeals(unloadingRemark = None, unloadingDate = dateGoodsUnloaded)
          }
        }

      }

      "number of items has been updated" - {

        "without seals" in {
          forAll(
            arbitrary[UnloadingPermission],
            arbitrary[ResultsOfControl],
            choose(min = 1: Int, 49: Int),
            choose(min = 50: Int, 100: Int)
          ) {
            (unloadingPermission, resultsOfControlValues, numberOfItemsUnloadingPermission, numberOfItemsUpdated) =>
              val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None, numberOfItems = numberOfItemsUnloadingPermission)
              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(TotalNumberOfItemsPage, numberOfItemsUpdated)
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksNonConform(stateOfSeals = None, unloadingRemark = None, unloadingDate = dateGoodsUnloaded)

          }
        }

        "with seals" in {

          forAll(
            arbitrary[UnloadingPermission],
            arbitrary[ResultsOfControl],
            choose(min = 1: Int, 49: Int),
            choose(min = 50: Int, 100: Int)
          ) {
            (unloadingPermission, resultsOfControlValues, numberOfItemsUnloadingPermission, numberOfItemsUpdated) =>
              val unloadingPermissionWithNoSeals =
                unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))), numberOfItems = numberOfItemsUnloadingPermission)
              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(TotalNumberOfItemsPage, numberOfItemsUpdated)
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksNonConform(stateOfSeals = Some(1), unloadingRemark = None, unloadingDate = dateGoodsUnloaded)

          }
        }

        "and has same value as unloading permission" in {
          forAll(
            arbitrary[UnloadingPermission],
            arbitrary[ResultsOfControl],
            choose(min = 1: Int, 49: Int)
          ) {
            (unloadingPermission, resultsOfControlValues, numberOfItems) =>
              val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None, numberOfItems = numberOfItems)
              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(TotalNumberOfItemsPage, numberOfItems)
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksConform(unloadingRemark = None, unloadingDate = dateGoodsUnloaded)

          }
        }
      }

      "number of packages has been updated" - {

        "without seals" in {

          forAll(
            arbitrary[UnloadingPermission],
            arbitrary[ResultsOfControl],
            choose(min = 1: Int, 49: Int),
            choose(min = 50: Int, 100: Int)
          ) {
            (unloadingPermission, resultsOfControlValues, numberOfPackagesUnloadingPermission, numberOfPackagesUpdated) =>
              val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None, numberOfPackages = Some(numberOfPackagesUnloadingPermission))
              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(TotalNumberOfPackagesPage, numberOfPackagesUpdated)
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksNonConform(stateOfSeals = None, unloadingRemark = None, unloadingDate = dateGoodsUnloaded)

          }
        }

        "with seals" in {

          forAll(
            arbitrary[UnloadingPermission],
            arbitrary[ResultsOfControl],
            Gen.option(choose(min = 1: Int, 49: Int)),
            choose(min = 50: Int, 100: Int)
          ) {
            (unloadingPermission, resultsOfControlValues, numberOfPackagesUnloadingPermission, numberOfPackagesUpdated) =>
              val unloadingPermissionWithNoSeals =
                unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))), numberOfPackages = numberOfPackagesUnloadingPermission)
              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(TotalNumberOfPackagesPage, numberOfPackagesUpdated)
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksNonConform(stateOfSeals = Some(1), unloadingRemark = None, unloadingDate = dateGoodsUnloaded)

          }
        }

        "and has same value as unloading permission" in {

          forAll(
            arbitrary[UnloadingPermission],
            arbitrary[ResultsOfControl],
            choose(min = 1: Int, 49: Int)
          ) {
            (unloadingPermission, resultsOfControlValues, numberOfPackages) =>
              val unloadingPermissionWithNoSeals =
                unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))), numberOfPackages = Some(numberOfPackages))
              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(TotalNumberOfPackagesPage, numberOfPackages)
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(Seq(resultsOfControlValues))

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksConformWithSeals(unloadingRemark = None, unloadingDate = dateGoodsUnloaded)

          }
        }
      }

      "when unloading remarks exist" - {

        "without seals" in {

          forAll(
            arbitrary[UnloadingPermission],
            listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength),
            stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength)
          ) {
            (unloadingPermission, resultsOfControlValues, unloadingRemarks) =>
              val unloadingPermissionWithNoSeals = unloadingPermission.copy(seals = None)

              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(ChangesToReportPage, unloadingRemarks)
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(resultsOfControlValues)

              service.build(userAnswers, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksConform(unloadingRemark = Some(unloadingRemarks), unloadingDate = dateGoodsUnloaded)

          }

        }

        "with seals" in {

          forAll(
            arbitrary[UnloadingPermission],
            listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength),
            stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength)
          ) {
            (unloadingPermission, resultsOfControlValues, unloadingRemarks) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

              val userAnswers = emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(ChangesToReportPage, unloadingRemarks)
                .success
                .value

              when(mockResultOfControlService.build(userAnswers, unloadingPermission)).thenReturn(resultsOfControlValues)

              service.build(userAnswers, unloadingPermissionWithSeals).futureValue mustBe
                RemarksConformWithSeals(unloadingRemark = Some(unloadingRemarks), unloadingDate = dateGoodsUnloaded)

          }
        }

      }

      "when seals" - {

        "have same values in unloading permission and user answers" in {

          forAll(arbitrary[UnloadingPermission]) {
            unloadingPermission =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(1, Seq("seal 1", "seal 2", "seal 3"))))

              val userAnswersUpdated =
                emptyUserAnswers
                  .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                  .success
                  .value
                  .set(NewSealNumberPage(Index(0)), "seal 2")
                  .success
                  .value
                  .set(NewSealNumberPage(Index(1)), "seal 1")
                  .success
                  .value
                  .set(NewSealNumberPage(Index(2)), "seal 3")
                  .success
                  .value

              when(mockResultOfControlService.build(userAnswersUpdated, unloadingPermission)).thenReturn(Nil)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
                RemarksConformWithSeals(dateGoodsUnloaded, None)
          }
        }

        "cannot be read" in {
          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission], listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength)) {
            (userAnswers, unloadingPermission, resultsOfControlValues) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

              val userAnswersUpdated = userAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(CanSealsBeReadPage, false)
                .success
                .value
                .set(AreAnySealsBrokenPage, false)
                .success
                .value

              when(mockResultOfControlService.build(userAnswersUpdated, unloadingPermission)).thenReturn(resultsOfControlValues)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
                RemarksNonConform(
                  stateOfSeals = Some(0),
                  unloadingRemark = userAnswers.get(ChangesToReportPage),
                  unloadingDate = dateGoodsUnloaded
                )
          }
        }

        "are broken" in {
          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission], listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength)) {
            (userAnswers, unloadingPermission, resultsOfControlValues) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

              val userAnswersUpdated = userAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value
                .set(CanSealsBeReadPage, true)
                .success
                .value
                .set(AreAnySealsBrokenPage, true)
                .success
                .value

              when(mockResultOfControlService.build(userAnswersUpdated, unloadingPermission)).thenReturn(resultsOfControlValues)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
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

              val userAnswersUpdated =
                emptyUserAnswers
                  .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                  .success
                  .value
                  .set(ChangesToReportPage, unloadingRemarks)
                  .success
                  .value

              when(mockResultOfControlService.build(userAnswersUpdated, unloadingPermission)).thenReturn(Nil)

              service.build(userAnswersUpdated, unloadingPermissionWithNoSeals).futureValue mustBe
                RemarksConform(dateGoodsUnloaded, Some(unloadingRemarks))
          }
        }

        "exist in unloading permission and user hasn't changed anything" in {
          forAll(stringsWithMaxLength(RemarksNonConform.unloadingRemarkLength), arbitrary[UnloadingPermission]) {
            (unloadingRemarks, unloadingPermission) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(2, Seq("seal 1", "seal 2"))))

              val userAnswersUpdated =
                emptyUserAnswers
                  .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                  .success
                  .value
                  .set(ChangesToReportPage, unloadingRemarks)
                  .success
                  .value

              when(mockResultOfControlService.build(userAnswersUpdated, unloadingPermission)).thenReturn(Nil)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
                RemarksConformWithSeals(dateGoodsUnloaded, Some(unloadingRemarks))
          }
        }

        "don't exist in unloading permission and user has added a new seal" in {

          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
            (userAnswers, unloadingPermission) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = None)

              val userAnswersUpdated =
                userAnswers.set(DateGoodsUnloadedPage, dateGoodsUnloaded).success.value.set(NewSealNumberPage(Index(0)), "new seal").success.value

              when(mockResultOfControlService.build(userAnswersUpdated, unloadingPermission)).thenReturn(Nil)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
                RemarksNonConform(
                  stateOfSeals = Some(0),
                  unloadingRemark = userAnswers.get(ChangesToReportPage),
                  unloadingDate = dateGoodsUnloaded
                )
          }

        }

        "have been updated" in {

          forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission], listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength)) {
            (userAnswers, unloadingPermission, resultsOfControlValues) =>
              val unloadingPermissionWithSeals = unloadingPermission.copy(seals = Some(Seals(1, Seq("seal 1", "seal 2", "seal 3"))))

              val userAnswersUpdated =
                userAnswers
                  .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                  .success
                  .value
                  .set(NewSealNumberPage(Index(0)), "seal 1")
                  .success
                  .value
                  .set(NewSealNumberPage(Index(1)), "updated seal")
                  .success
                  .value
                  .set(NewSealNumberPage(Index(2)), "seal 3")
                  .success
                  .value

              when(mockResultOfControlService.build(userAnswersUpdated, unloadingPermission)).thenReturn(resultsOfControlValues)

              service.build(userAnswersUpdated, unloadingPermissionWithSeals).futureValue mustBe
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
