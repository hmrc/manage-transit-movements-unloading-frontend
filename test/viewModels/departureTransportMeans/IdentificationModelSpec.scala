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

package viewModels.departureTransportMeans

import base.SpecBase
import generators.Generators
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.departureTransportMeans.IdentificationViewModel.IdentificationViewModelProvider

class IdentificationModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model" - {
    "when NormalMode" in {
      val viewModelProvider = new IdentificationViewModelProvider()
      val result            = viewModelProvider.apply(NormalMode)(messages)

      result.title mustBe "Which identification do you want to use for the departure means of transport?"
      result.heading mustBe "Which identification do you want to use for the departure means of transport?"
    }

    "when CheckMode" in {
      val viewModelProvider = new IdentificationViewModelProvider()

      val result = viewModelProvider.apply(CheckMode)(messages)

      result.title mustBe "Which identification do you want to use for the new departure means of transport?"
      result.heading mustBe "Which identification do you want to use for the new departure means of transport?"
    }
  }
}
