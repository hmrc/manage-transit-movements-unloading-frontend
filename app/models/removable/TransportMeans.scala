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

package models.removable

import models.reference.TransportMeansIdentification
import models.{Index, UserAnswers}
import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads

case class TransportMeans(index: Index, identificationType: Option[TransportMeansIdentification], identificationNumber: Option[String]) {

  def forRemoveDisplay: Option[String] = (identificationType, identificationNumber) match {
    case (Some(a), Some(b)) => Some(s"$a - $b")
    case (Some(a), None)    => Some(a.toString)
    case (None, Some(b))    => Some(b)
    case (None, None)       => None
  }

  def forAddAnotherDisplay(implicit messages: Messages): String = {
    val prefix = messages("departureMeansOfTransport.prefix", index.display)
    (identificationType, identificationNumber) match {
      case (Some(a), Some(b)) => s"$prefix - $a - $b"
      case (Some(a), None)    => s"$prefix - $a"
      case (None, Some(b))    => s"$prefix - $b"
      case (None, None)       => prefix
    }
  }
}

object TransportMeans {

  def apply(userAnswers: UserAnswers, transportMeansIndex: Index): Option[TransportMeans] = {
    import pages.departureMeansOfTransport._
    implicit val reads: Reads[TransportMeans] = (
      TransportMeansIdentificationPage(transportMeansIndex).path.readNullable[TransportMeansIdentification] and
        VehicleIdentificationNumberPage(transportMeansIndex).path.readNullable[String]
    ).apply {
      (identifier, identificationNumber) => TransportMeans(transportMeansIndex, identifier, identificationNumber)
    }
    userAnswers.data.asOpt[TransportMeans]
  }
}
