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

sealed trait Procedure {
  val revised: Boolean
}

object Procedure {

  sealed trait Unrevised extends Procedure {
    override val revised: Boolean = false
  }

  case object UsingUnrevised extends Unrevised

  sealed trait Revised extends Procedure {
    override val revised: Boolean = true
  }

  case object RevisedAndGoodsTooLarge extends Revised

  case object RevisedAndGoodsNotTooLarge extends Revised

  case object CannotUseRevised extends Unrevised

  case object UsingRevised extends Revised

  def apply(userAnswers: UserAnswers): Procedure =
    (
      userAnswers.get(NewAuthYesNoPage),
      userAnswers.get(RevisedUnloadingProcedureConditionsYesNoPage),
      userAnswers.get(GoodsTooLargeForContainerYesNoPage)
    ) match {
      case (Some(true), Some(false), _) => CannotUseRevised
      case (Some(true), Some(true), Some(true)) =>
        userAnswers.get(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage) match {
          case Some(true) => CannotUseRevised
          case _          => RevisedAndGoodsTooLarge
        }
      case (Some(true), Some(true), Some(false)) => RevisedAndGoodsNotTooLarge
      case (Some(false), _, _)                   => UsingUnrevised
      case _                                     => throw new Exception(s"[${userAnswers.id}] - Couldn't determine procedure")
    }
}
