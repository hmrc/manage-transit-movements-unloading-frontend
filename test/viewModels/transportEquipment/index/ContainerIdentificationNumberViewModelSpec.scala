/*
 * Copyright 2024 HM Revenue & Customs
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

package viewModels.transportEquipment.index

import base.SpecBase
import generators.Generators
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.transportEquipment.index.ContainerIdentificationNumberViewModel.ContainerIdentificationNumberViewModelProvider

class ContainerIdentificationNumberViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "must create view model in " - {

    "when NormalMode" in {
      val viewModelProvider = new ContainerIdentificationNumberViewModelProvider()
      val result            = viewModelProvider.apply(NormalMode)

      result.title mustEqual "What is the container identification number?"
      result.heading mustEqual "What is the container identification number?"
      result.requiredError mustEqual "Enter the container identification number"
      result.paragraph must not be defined
    }

    "when CheckMode" in {
      val viewModelProvider = new ContainerIdentificationNumberViewModelProvider()
      val result            = viewModelProvider.apply(CheckMode)

      result.title mustEqual "What is the new container identification number?"
      result.heading mustEqual "What is the new container identification number?"
      result.requiredError mustEqual "Enter the new container identification number"
      result.paragraph.get mustEqual "This is a unique number used to identify the container."
    }

  }

}
