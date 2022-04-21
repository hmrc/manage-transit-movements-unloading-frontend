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

import models.{CheckMode, UserAnswers}
import play.api.i18n.Messages
import utils.{CheckYourAnswersHelper, UnloadingSummaryHelper}
import viewModels.sections.Section

class CheckYourAnswersViewModel {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages): Seq[Section] =
    Seq(
      goodsUnloadedSection(userAnswers),
      sealsSection(userAnswers),
      itemsSection(userAnswers)
    ).flatten

  private def sealsSection(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] = {
    val helper = new CheckYourAnswersHelper(userAnswers)

    val rows = Seq(
      helper.seals,
      helper.canSealsBeRead,
      helper.areAnySealsBroken
    ).flatten

    rows match {
      case Nil => None
      case _   => Some(Section(messages("checkYourAnswers.seals.subHeading"), rows))
    }
  }

  private def goodsUnloadedSection(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] = {
    val helper = new CheckYourAnswersHelper(userAnswers)
    val rows   = helper.dateGoodsUnloaded.toSeq

    rows match {
      case Nil => None
      case _   => Some(Section(rows))
    }
  }

  private def itemsSection(userAnswers: UserAnswers)(implicit messages: Messages): Option[Section] = {
    val helper = new UnloadingSummaryHelper(userAnswers, CheckMode)

    val rows = helper.vehicleUsed.toSeq ++
      helper.registeredCountry.toSeq ++
      helper.grossMass.toSeq ++
      helper.totalNumberOfItems.toSeq ++
      helper.totalNumberOfPackages.toSeq ++
      helper.items ++
      helper.comments.toSeq

    rows match {
      case Nil => None
      case _   => Some(Section(messages("checkYourAnswers.subHeading"), rows))
    }
  }

}
