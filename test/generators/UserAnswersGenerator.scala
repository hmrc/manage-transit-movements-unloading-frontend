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

package generators

import generated.CC043CType
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import pages.departureMeansOfTransport.{CountryPage, VehicleIdentificationNumberPage}
import pages.houseConsignment.index.items.GrossWeightPage
import pages.transportEquipment.index.seals.SealIdentificationNumberPage
import play.api.libs.json.{JsValue, Json}

import java.time.Instant

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[?], JsValue)]] =
    arbitrary[(UnloadingCommentsPage.type, JsValue)] ::
      arbitrary[(AreAnySealsBrokenPage.type, JsValue)] ::
      arbitrary[(CanSealsBeReadPage.type, JsValue)] ::
      arbitrary[(SealIdentificationNumberPage, JsValue)] ::
      arbitrary[(GrossWeightPage, JsValue)] ::
      arbitrary[(CountryPage, JsValue)] ::
      arbitrary[(VehicleIdentificationNumberPage, JsValue)] ::
      arbitrary[(DateGoodsUnloadedPage.type, JsValue)] ::
      Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        mrn        <- arbitrary[MovementReferenceNumber]
        eoriNumber <- arbitrary[EoriNumber]
        ie043Data  <- arbitrary[CC043CType]
        data <- generators match {
          case Nil => Gen.const(Map[QuestionPage[?], JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers(
        id = ArrivalId("AB123"),
        mrn = mrn,
        eoriNumber = eoriNumber,
        ie043Data = ie043Data,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        },
        lastUpdated = Instant.now
      )
    }
  }
}
