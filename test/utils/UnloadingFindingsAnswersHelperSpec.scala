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

package utils.cyaHelpers

import base.SpecBase
import generators.Generators
import models.{Mode, NormalMode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.UnloadingFindingsAnswersHelper

class UnloadingFindingsAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "UnloadingFindingsAnswersHelper" - {

    "departureMeansID" - {

      "must return None" - {
        s"when $VehicleIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.departureMeansID
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $VehicleIdentificationNumberPage defined" in {
          val answers = emptyUserAnswers
            .setValue(VehicleIdentificationNumberPage, "123456")
            .setValue(VehicleIdentificationTypePage, "31")

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.departureMeansID

          result mustBe Some(
            SummaryListRow(
              key = Key("Registration number of a road trailer".toText),
              value = Value("123456".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.VehicleIdentificationNumberController.onPageLoad(arrivalId, NormalMode).url,
                      visuallyHiddenText = Some("registration number of a road trailer"),
                      attributes = Map("id" -> "change-departure-means-id")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }
    "departureRegisteredCountry" - {

      "must return None" - {
        s"when $VehicleRegistrationCountryPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.departureRegisteredCountry
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $VehicleRegistrationCountryPage defined" in {
          val answers = emptyUserAnswers
            .setValue(VehicleRegistrationCountryPage, "DE")

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.departureRegisteredCountry

          result mustBe Some(
            SummaryListRow(
              key = Key("Registered country".toText),
              value = Value("DE".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, NormalMode).url,
                      visuallyHiddenText = Some("registered country"),
                      attributes = Map("id" -> "change-departure-means-country")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }
    "containerIdentificationNumber" - {

      "must return None" - {
        s"when $ContainerIdentificationNumberPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.containerIdentificationNumber(index)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $ContainerIdentificationNumberPage defined" in {
          val answers = emptyUserAnswers
            .setValue(ContainerIdentificationNumberPage(index), "123456")

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.containerIdentificationNumber(index)

          result mustBe Some(
            SummaryListRow(
              key = Key("Container identification number".toText),
              value = Value("123456".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.NewContainerIdentificationNumberController.onPageLoad(arrivalId, index, NormalMode).url,
                      visuallyHiddenText = Some("container identification number 123456"),
                      attributes = Map("id" -> s"change-container-identification-number-${index.display}")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }
    "transportEquipmentSeal" - {

      "must return None" - {
        s"when $SealPage undefined" in {

          val helper = new UnloadingFindingsAnswersHelper(emptyUserAnswers)
          val result = helper.transportEquipmentSeal(index, index)
          result mustBe None
        }
      }

      "must return Some(Row)" - {
        s"when $SealPage defined" in {
          val answers = emptyUserAnswers
            .setValue(SealPage(equipmentIndex, sealIndex), "123456")

          val helper = new UnloadingFindingsAnswersHelper(answers)
          val result = helper.transportEquipmentSeal(equipmentIndex, sealIndex)

          result mustBe Some(
            SummaryListRow(
              key = Key(s"Seal $sealIndex".toText),
              value = Value("123456".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = controllers.routes.NewSealNumberController.onPageLoad(arrivalId, equipmentIndex, sealIndex, NormalMode).url,
                      visuallyHiddenText = Some(s"seal $sealIndex - 123456"),
                      attributes = Map("id" -> s"change-seal-identifier-${sealIndex.display}")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }
  }

}
