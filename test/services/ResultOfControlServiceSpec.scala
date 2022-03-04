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
import models.{Index, Seals, UnloadingPermission}
import models.messages._
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class ResultOfControlServiceSpec extends SpecBase with Generators with ScalaCheckPropertyChecks {

  val service = new ResultOfControlServiceImpl

  private val dateGoodsUnloaded = LocalDate.now(ZoneOffset.UTC)

  "ResultOfControlServiceSpec" - {

    "handle when UserAnswers" - {

      "are empty" in {
        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            service.build(emptyUserAnswers, unloadingPermission) mustBe Nil
        }
      }

      "contains VehicleNameRegistrationReference value" in {

        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            val userAnswersUpdated = emptyUserAnswers
              .set(VehicleNameRegistrationReferencePage, "reference")
              .success
              .value

            service.build(userAnswersUpdated, unloadingPermission) mustBe Seq(
              ResultsOfControlDifferentValues(
                PointerToAttribute(TransportIdentity),
                "reference"
              )
            )
        }
      }

      "contains VehicleRegistrationCountry value" in {
        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            val userAnswersUpdated = emptyUserAnswers
              .set(VehicleRegistrationCountryPage, Country("FR", "description"))
              .success
              .value

            service.build(userAnswersUpdated, unloadingPermission) mustBe Seq(
              ResultsOfControlDifferentValues(
                PointerToAttribute(TransportCountry),
                "FR"
              )
            )
        }
      }

      "contains NumberOfItems value" in {
        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            val userAnswersUpdated = emptyUserAnswers
              .set(TotalNumberOfItemsPage, 123: Int)
              .success
              .value

            service.build(userAnswersUpdated, unloadingPermission) mustBe Seq(
              ResultsOfControlDifferentValues(
                PointerToAttribute(NumberOfItems),
                "123"
              )
            )
        }
      }

      "contains NumberOfPackages value" in {
        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            val userAnswersUpdated = emptyUserAnswers
              .set(TotalNumberOfPackagesPage, 123: Int)
              .success
              .value

            service.build(userAnswersUpdated, unloadingPermission) mustBe Seq(
              ResultsOfControlDifferentValues(
                PointerToAttribute(NumberOfPackages),
                "123"
              )
            )
        }
      }

      "contains GrossMass value" in {
        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            val userAnswersUpdated = emptyUserAnswers
              .set(GrossMassAmountPage, "12234567")
              .success
              .value

            service.build(userAnswersUpdated, unloadingPermission) mustBe Seq(
              ResultsOfControlDifferentValues(
                PointerToAttribute(GrossMass),
                "12234567"
              )
            )
        }
      }

      "contains multiple values" in {
        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            val userAnswersUpdated = emptyUserAnswers
              .set(VehicleNameRegistrationReferencePage, "reference")
              .success
              .value
              .set(VehicleRegistrationCountryPage, Country("FR", "description"))
              .success
              .value

            service.build(userAnswersUpdated, unloadingPermission) mustBe Seq(
              ResultsOfControlDifferentValues(
                PointerToAttribute(TransportIdentity),
                "reference"
              ),
              ResultsOfControlDifferentValues(
                PointerToAttribute(TransportCountry),
                "FR"
              )
            )
        }
      }

      "don't contain seals" - {
        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            val userAnswersUpdated =
              emptyUserAnswers
                .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                .success
                .value

            service.build(userAnswersUpdated, unloadingPermission) mustBe Nil
        }
      }

      "contain seals" - {

        "that don't match existing seals in UnloadingPermission message" in {
          forAll(arbitrary[UnloadingPermission]) {
            unloadingPermission =>
              val unloadingPermissionUpdated = unloadingPermission.copy(
                seals = Some(
                  Seals(1, Seq("original value 1"))
                )
              )

              val userAnswersUpdated =
                emptyUserAnswers
                  .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                  .success
                  .value
                  .set(NewSealNumberPage(Index(0)), "changed value 1")
                  .success
                  .value

              service.build(userAnswersUpdated, unloadingPermissionUpdated) mustBe Seq(
                ResultsOfControlOther(
                  "Seals have been updated"
                )
              )
          }
        }

        "that match single seal in UnloadingPermission message" in {
          forAll(arbitrary[UnloadingPermission]) {
            unloadingPermission =>
              val unloadingPermissionUpdated = unloadingPermission.copy(
                seals = Some(
                  Seals(1, Seq("original value 1"))
                )
              )

              val userAnswersUpdated =
                emptyUserAnswers
                  .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                  .success
                  .value
                  .set(NewSealNumberPage(Index(0)), "original value 1")
                  .success
                  .value

              service.build(userAnswersUpdated, unloadingPermissionUpdated) mustBe Nil
          }
        }

        "that match multiple seals in UnloadingPermission message" in {
          forAll(arbitrary[UnloadingPermission]) {
            unloadingPermission =>
              val unloadingPermissionUpdated = unloadingPermission.copy(
                seals = Some(
                  Seals(2, Seq("original value 1", "original value 2"))
                )
              )

              val userAnswersUpdated =
                emptyUserAnswers
                  .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                  .success
                  .value
                  .set(NewSealNumberPage(Index(0)), "original value 2")
                  .success
                  .value
                  .set(NewSealNumberPage(Index(1)), "original value 1")
                  .success
                  .value

              service.build(userAnswersUpdated, unloadingPermissionUpdated) mustBe Nil
          }
        }

        "and UnloadingPermission contains no seals" in {
          forAll(arbitrary[UnloadingPermission]) {
            unloadingPermission =>
              val unloadingPermissionUpdated = unloadingPermission.copy(seals = None)

              val userAnswersUpdated =
                emptyUserAnswers
                  .set(DateGoodsUnloadedPage, dateGoodsUnloaded)
                  .success
                  .value
                  .set(NewSealNumberPage(Index(0)), "new seal")
                  .success
                  .value

              service.build(userAnswersUpdated, unloadingPermissionUpdated) mustBe Seq(
                ResultsOfControlOther(
                  "Seals have been updated"
                )
              )
          }
        }
      }

      "AreAnySealsBroken is true" in {
        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            val userAnswersUpdated = emptyUserAnswers
              .set(AreAnySealsBrokenPage, true)
              .success
              .value

            service.build(userAnswersUpdated, unloadingPermission) mustBe Seq(
              ResultsOfControlOther(
                "Some seals are broken"
              )
            )
        }
      }

      "CanSealsBeRead is false" in {

        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            val userAnswersUpdated = emptyUserAnswers
              .set(CanSealsBeReadPage, false)
              .success
              .value

            service.build(userAnswersUpdated, unloadingPermission) mustBe Seq(
              ResultsOfControlOther(
                "Some seals not readable"
              )
            )
        }
      }

      "AreAnySealsBroken is true and CanSealsBeRead is false" in {
        forAll(arbitrary[UnloadingPermission]) {
          unloadingPermission =>
            val userAnswersUpdated = emptyUserAnswers
              .set(AreAnySealsBrokenPage, true)
              .success
              .value
              .set(CanSealsBeReadPage, false)
              .success
              .value

            service.build(userAnswersUpdated, unloadingPermission) mustBe Seq(
              ResultsOfControlOther(
                "Some seals are broken"
              ),
              ResultsOfControlOther(
                "Some seals not readable"
              )
            )
        }
      }

    }

  }

}
