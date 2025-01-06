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

import pages.*

case class OtherThingsToReport(prefix: String)

object OtherThingsToReport {

  def apply(userAnswers: UserAnswers): OtherThingsToReport = {
    val procedure = Procedure(userAnswers)
    OtherThingsToReport(userAnswers, procedure)
  }

  def apply(userAnswers: UserAnswers, procedure: Procedure): OtherThingsToReport = {
    val prefix = procedure match {
      case _: Procedure.Revised =>
        userAnswers.get(SealsReplacedByCustomsAuthorityYesNoPage) match {
          case Some(true) =>
            "otherThingsToReport.newAuthAndSealsReplaced"
          case Some(false) =>
            "otherThingsToReport.newAuth"
          case None =>
            throw new Exception(s"[${userAnswers.id}] - Couldn't determine value because SealsReplacedByCustomsAuthorityYesNoPage is unpopulated")
        }
      case Procedure.Unrevised | Procedure.CannotUseRevised =>
        "otherThingsToReport.oldAuth"
    }
    OtherThingsToReport(prefix)
  }
}
