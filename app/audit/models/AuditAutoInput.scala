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

import models.UnloadingPermission
import play.api.libs.json.{JsArray, JsString, Json, Writes}

case class AuditAutoInput(unloadingPermission: UnloadingPermission)

object AuditAutoInput {

  implicit val writes: Writes[AuditAutoInput] = (input: AuditAutoInput) => {
    Json.obj(
      "prepopulatedUserAnswers" -> Json.obj(
        "seals" -> JsArray(input.unloadingPermission.seals.map(_.sealIds.map(JsString)).getOrElse(Nil))
      )
    )
  }
}
