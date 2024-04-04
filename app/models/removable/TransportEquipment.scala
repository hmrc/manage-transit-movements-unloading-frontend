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

import models.{Index, UserAnswers}
import play.api.i18n.Messages
import play.api.libs.json.Reads

case class TransportEquipment(index: Index, containerId: Option[String]) {

  def forRemoveDisplay(implicit messages: Messages): Option[String] = containerId.map {
    id => messages("transportEquipment.containerId", id)
  }

  def forAddAnotherDisplay(implicit messages: Messages): String = {
    val prefix = messages("transportEquipment.prefix", index.display)
    containerId match {
      case Some(value) => s"$prefix - ${messages("transportEquipment.containerId", value)}"
      case None        => prefix
    }
  }
}

object TransportEquipment {

  def apply(userAnswers: UserAnswers, transportEquipmentIndex: Index): Option[TransportEquipment] = {
    import pages._
    implicit val reads: Reads[TransportEquipment] =
      ContainerIdentificationNumberPage(transportEquipmentIndex).path.readNullable[String].map {
        containerId =>
          TransportEquipment(transportEquipmentIndex, containerId)
      }
    userAnswers.data.asOpt[TransportEquipment]
  }
}
