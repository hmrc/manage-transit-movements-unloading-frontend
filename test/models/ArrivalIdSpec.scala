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

package models

import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.mvc.PathBindable

class ArrivalIdSpec extends AnyFreeSpec with Matchers with EitherValues {

  "Arrival Id" - {
    "must bind from url" in {
      val pathBindable = implicitly[PathBindable[ArrivalId]]
      val arrivalId    = ArrivalId(12)

      val bind: Either[String, ArrivalId] = pathBindable.bind("arrivalId", "12")
      bind.value mustBe arrivalId
    }

    "unbind to path value" in {
      val pathBindable = implicitly[PathBindable[ArrivalId]]
      val arrivalId    = ArrivalId(12)

      val bindValue = pathBindable.unbind("arrivalId", arrivalId)
      bindValue mustBe "12"
    }
  }
}
