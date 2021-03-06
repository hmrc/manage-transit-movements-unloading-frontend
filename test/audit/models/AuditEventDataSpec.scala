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

package audit.models

import base.SpecBase
import generators.Generators
import models.{Seals, UnloadingPermission, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class AuditEventDataSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "AuditEventData" - {
    "must serialise" - {
      "when unloading permission has seals" in {
        val originalSeals = Seq("Seals01", "Seals02", "Seals03")

        forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
          (userAnswers, unloadingPermission) =>
            val auditUserInput = AuditUserInput(userAnswers)
            val auditAutoInput = Some(AuditAutoInput(unloadingPermission.copy(seals = Some(Seals(originalSeals)))))
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

      "when unloading permission has empty seals" in {
        forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
          (userAnswers, unloadingPermission) =>
            val auditUserInput = AuditUserInput(userAnswers)
            val auditAutoInput = Some(AuditAutoInput(unloadingPermission.copy(seals = Some(Seals(Nil)))))
            val audit          = AuditEventData(auditUserInput, auditAutoInput)

            val expectedResult = Json.parse(s"""
                 |{
                 |  "fullUserAnswers": ${userAnswers.data},
                 |  "prepopulatedUserAnswers": {
                 |    "seals": []
                 |  }
                 |}
                 |""".stripMargin)

            Json.toJson(audit) mustBe expectedResult
        }
      }

      "when unloading permission doesn't have seals" in {
        forAll(arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
          (userAnswers, unloadingPermission) =>
            val auditUserInput = AuditUserInput(userAnswers)
            val auditAutoInput = Some(AuditAutoInput(unloadingPermission.copy(seals = None)))
            val audit          = AuditEventData(auditUserInput, auditAutoInput)

            val expectedResult = Json.parse(s"""
                 |{
                 |  "fullUserAnswers": ${userAnswers.data},
                 |  "prepopulatedUserAnswers": {
                 |    "seals": []
                 |  }
                 |}
                 |""".stripMargin)

            Json.toJson(audit) mustBe expectedResult
        }
      }

      "when no unloading permission" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val auditUserInput = AuditUserInput(userAnswers)
            val auditAutoInput = None
            val audit          = AuditEventData(auditUserInput, auditAutoInput)

            val expectedResult = Json.parse(s"""
                 |{
                 |  "fullUserAnswers": ${userAnswers.data}
                 |}
                 |""".stripMargin)

            Json.toJson(audit) mustBe expectedResult
        }
      }
    }
  }
}
