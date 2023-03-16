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

case class UnloadingRemarksSentViewModel(customsOffice: Option[CustomsOffice]) {

  def foo(customsOffice: Option[CustomsOffice])(implicit messages: Messages) = customsOffice match {
    case Some(CustomsOffice(_, name, _, Some(phone), _)) => messages("unloadingRemarksSent.teleavailable", name, phone)
    case _                                               => messages("unloadingRemarksSent.teleavailable", "test", "1213242")
  }
}
