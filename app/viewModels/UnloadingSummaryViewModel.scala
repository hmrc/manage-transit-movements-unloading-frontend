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

package viewModels

import models.UserAnswers
import play.api.i18n.Messages
import utils.UnloadingSummaryHelper
import viewModels.sections.Section

class UnloadingSummaryViewModel {

  def sealsSection(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] =
    SealsSection.apply(userAnswers)

  def transportAndItemSections(userAnswers: UserAnswers)(implicit messages: Messages): Seq[Section] =
    TransportSection(userAnswers).toSeq :+ ItemsSection(userAnswers)
}

object SealsSection {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] = {
    val helper: UnloadingSummaryHelper = new UnloadingSummaryHelper(userAnswers)

    helper.seals ++ helper.sealsWithRemove match {
      case Nil  => None
      case rows => Some(Section(messages("changeSeal.title"), rows))
    }
  }
}

object TransportSection {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] = {
    val helper: UnloadingSummaryHelper = new UnloadingSummaryHelper(userAnswers)

    Seq(
      helper.vehicleUsed,
      helper.registeredCountry
    ).flatten match {
      case Nil  => None
      case rows => Some(Section(messages("vehicleUsed.title"), rows))
    }
  }
}

object ItemsSection {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages): Section = {
    val helper: UnloadingSummaryHelper = new UnloadingSummaryHelper(userAnswers)

    val rows = helper.grossMass.toSeq ++
      helper.totalNumberOfItems.toSeq ++
      helper.totalNumberOfPackages.toSeq ++
      helper.items ++
      helper.comments.toSeq

    Section(messages("changeItems.title"), rows)
  }
}
