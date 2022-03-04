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

package generators

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators {

  implicit lazy val arbitraryChangesToReportEntry: Arbitrary[(ChangesToReportPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChangesToReportPage.type]
        value <- arbitrary[String].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAnythingElseToReportUserAnswersEntry: Arbitrary[(AnythingElseToReportPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AnythingElseToReportPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAreAnySealsBrokenUserAnswersEntry: Arbitrary[(AreAnySealsBrokenPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AreAnySealsBrokenPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCanSealsBeReadUserAnswersEntry: Arbitrary[(CanSealsBeReadPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CanSealsBeReadPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySealNumberReference: Arbitrary[(NewSealNumberPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[NewSealNumberPage]
        value <- arbitrary[String].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryGrossMassAmountReference: Arbitrary[(GrossMassAmountPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[GrossMassAmountPage.type]
        value <- arbitrary[String].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryVehicleRegistrationCountryReference: Arbitrary[(VehicleRegistrationCountryPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[VehicleRegistrationCountryPage.type]
        value <- arbitrary[String].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryVehicleNameRegistrationReference: Arbitrary[(VehicleNameRegistrationReferencePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[VehicleNameRegistrationReferencePage.type]
        value <- arbitrary[String].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDateGoodsUnloadedUserAnswersEntry: Arbitrary[(DateGoodsUnloadedPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DateGoodsUnloadedPage.type]
        value <- arbitrary[String].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryConfirmRemoveCommentsEntry: Arbitrary[(ConfirmRemoveCommentsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ConfirmRemoveCommentsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

}
