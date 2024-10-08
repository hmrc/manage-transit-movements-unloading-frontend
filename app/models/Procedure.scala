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

import pages.{NewAuthYesNoPage, SealsReplacedByCustomsAuthorityYesNoPage}

sealed trait Procedure {
  val prefix: String
}

object Procedure {

  case object Unrevised extends Procedure {
    override val prefix: String = "otherThingsToReport.oldAuth"
  }

  case object RevisedAndSealReplaced extends Procedure {
    override val prefix: String = "otherThingsToReport.newAuthAndSealsReplaced"
  }

  case object RevisedAndSealNotReplaced extends Procedure {
    override val prefix: String = "otherThingsToReport.newAuth"
  }

  def apply(userAnswers: UserAnswers): Procedure =
    userAnswers.get(NewAuthYesNoPage) match
      case Some(value) =>
        apply(userAnswers, value)
      case None =>
        throw new Exception(s"[${userAnswers.id}] - Couldn't determine procedure because NewAuthYesNoPage is unpopulated")

  def apply(userAnswers: UserAnswers, revised: Boolean): Procedure =
    if (revised) {
      userAnswers.get(SealsReplacedByCustomsAuthorityYesNoPage) match
        case Some(true) =>
          RevisedAndSealReplaced
        case Some(false) =>
          RevisedAndSealNotReplaced
        case None =>
          throw new Exception(s"[${userAnswers.id}] - Couldn't determine procedure because SealsReplacedByCustomsAuthorityYesNoPage is unpopulated")
    } else {
      Unrevised
    }
}
