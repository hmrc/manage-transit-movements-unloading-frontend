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

import models.reference.CustomsOffice
import play.api.i18n.Messages

case class UnloadingRemarksSentViewModel(customsOffice: Option[CustomsOffice], customsOfficeId: String) {

  def fetch(implicit messages: Messages): String = customsOffice match {
    case Some(CustomsOffice(_, name, _, Some(phone))) => messages("unloadingRemarksSent.telephoneAvailable", name, phone)
    case Some(CustomsOffice(_, name, _, None))        => messages("unloadingRemarksSent.telephoneNotAvailable", name)
    case Some(CustomsOffice(id, "", _, Some(phone)))  => messages("unloadingRemarksSent.teleAvailAndOfficeNameNotAvail", id, phone)
    case _                                            => messages("unloadingRemarksSent.teleNotAvailAndOfficeNameNotAvail", customsOfficeId)
  }
}
