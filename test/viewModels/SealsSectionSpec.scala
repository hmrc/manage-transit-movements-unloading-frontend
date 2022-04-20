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

package viewModels

import base.SpecBase

class SealsSectionSpec extends SpecBase {

  "SealsSection" - {

    "contain section from unloading permission" in {

      /*val unloadingPermission = sampleUnloadingPermission.copy(seals = Some(Seals(1, Seq("seal 1", "seal 2"))))

      val section: Section = SealsSection(emptyUserAnswers).get
      section.rows(0).value.content mustBe Text("seal 1")
      section.rows(1).value.content mustBe Text("seal 2")*/
    }

    "contain section from user answers" in {

      /*val unloadingPermission = sampleUnloadingPermission.copy(seals = Some(Seals(1, Seq("seal 1", "seal 2"))))

      val updatedUserAnswers = emptyUserAnswers
        .setValue(NewSealNumberPage(Index(0)), "new seal value 1")
        .setValue(NewSealNumberPage(Index(1)), "new seal value 2")

      val section: Section = SealsSection(updatedUserAnswers).get
      section.rows(0).value.content mustBe Text("new seal value 1")
      section.rows(1).value.content mustBe Text("new seal value 2")*/
    }

    "return nothing if no seals exist" in {

      /*val unloadingPermission = sampleUnloadingPermission.copy(seals = None)

      val section: Option[Section] = SealsSection(emptyUserAnswers, unloadingPermission)
      section mustBe None*/
    }

  }

}
