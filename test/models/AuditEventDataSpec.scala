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

import audit.models.{AuditAutoInput, AuditEventData, AuditUserInput}
import base.SpecBase
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class AuditEventDataSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "AuditEventData" - {

    "must serialise" in {

      val originalSeals = Seq("Seals01", "Seals02", "Seals03")

      forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
        (userAnswers, unloadingPermission) =>
          val auditUserInput = AuditUserInput(userAnswers)
          val auditAutoInput = AuditAutoInput(unloadingPermission.copy(seals = Some(Seals(originalSeals))))
          val audit          = AuditEventData(auditUserInput, auditAutoInput)

          val expectedResult = Json.parse(s"""
              |{
              |  "fullUserAnswers": ${userAnswers.data},
              |  "prepopulatedUserAnswers": {
              |    "seals": [
              |      "Seals01",
              |      "Seals02",
              |      "Seals03"
              |    ]
              |  }
              |}
              |""".stripMargin)

          Json.toJson(audit) mustBe expectedResult
      }
    }
  }

}
