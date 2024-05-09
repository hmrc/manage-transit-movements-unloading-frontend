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

package models

import pages.{AreAnySealsBrokenPage, CanSealsBeReadPage}
import play.api.libs.json.Reads

case class StateOfSeals(value: Option[Boolean])

object StateOfSeals {

  def apply(userAnswers: UserAnswers): StateOfSeals =
    userAnswers.data
      .validate[StateOfSeals]
      .getOrElse(StateOfSeals(None))

  implicit val reads: Reads[StateOfSeals] =
    for {
      canSealsBeRead    <- CanSealsBeReadPage.path.readNullable[Boolean]
      areAnySealsBroken <- AreAnySealsBrokenPage.path.readNullable[Boolean]
    } yield (canSealsBeRead, areAnySealsBroken) match {
      case (Some(true), Some(false)) => StateOfSeals(Some(true))
      case (Some(_), Some(_))        => StateOfSeals(Some(false))
      case _                         => StateOfSeals(None)
    }
}
