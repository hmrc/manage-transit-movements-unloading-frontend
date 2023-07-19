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

package models

import play.api.libs.json.{__, Json, OWrites, Reads}

case class IE043Data(data: MessageData)

object IE043Data {
  implicit val reads: Reads[IE043Data]    = (__ \ "body" \ "n1:CC043C").read[MessageData].map(IE043Data.apply)
  implicit val writes: OWrites[IE043Data] = Json.writes[IE043Data]
}
