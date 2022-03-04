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

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(ChangesToReportPage.type, JsValue)] ::
      arbitrary[(AnythingElseToReportPage.type, JsValue)] ::
      arbitrary[(AreAnySealsBrokenPage.type, JsValue)] ::
      arbitrary[(CanSealsBeReadPage.type, JsValue)] ::
      arbitrary[(NewSealNumberPage, JsValue)] ::
      arbitrary[(GrossMassAmountPage.type, JsValue)] ::
      arbitrary[(VehicleRegistrationCountryPage.type, JsValue)] ::
      arbitrary[(VehicleNameRegistrationReferencePage.type, JsValue)] ::
      arbitrary[(DateGoodsUnloadedPage.type, JsValue)] ::
      Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        mrn        <- arbitrary[MovementReferenceNumber]
        eoriNumber <- arbitrary[EoriNumber]
        data <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers(
        id = ArrivalId(1),
        mrn = mrn,
        eoriNumber = eoriNumber,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
      )
    }
  }
}
