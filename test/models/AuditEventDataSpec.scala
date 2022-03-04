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
import play.api.libs.json.Json

class AuditEventDataSpec extends SpecBase {

  "AuditEventData" - {

    "must serialise" in {

      val ua = emptyUserAnswers.copy(data = Json.obj(("key", "value")), prepopulateData = Json.obj(("key", "value")))

      val auditUserInput = AuditUserInput(ua.data)
      val auditAutoInput = AuditAutoInput(ua.prepopulateData)
      val audit          = AuditEventData(auditUserInput, auditAutoInput)

      val expectedResult = Json.toJsObject(auditUserInput) ++ Json.toJsObject(auditAutoInput)

      Json.toJson(audit) mustBe expectedResult
    }
  }

}
