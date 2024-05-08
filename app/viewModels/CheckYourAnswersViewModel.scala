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

package viewModels

import models.UserAnswers
import pages.AddUnloadingCommentsYesNoPage
import play.api.i18n.Messages
import utils.answersHelpers.CheckYourAnswersHelper
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection

import javax.inject.Inject

case class CheckYourAnswersViewModel(sections: Seq[Section], showDiscrepanciesLink: Boolean)

object CheckYourAnswersViewModel {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages): CheckYourAnswersViewModel =
    new CheckYourAnswersViewModelProvider()(userAnswers)

  class CheckYourAnswersViewModelProvider @Inject() () {

    def apply(userAnswers: UserAnswers)(implicit messages: Messages): CheckYourAnswersViewModel = {
      val helper = new CheckYourAnswersHelper(userAnswers)

      val headerSection = StaticSection(
        rows = Seq(
          helper.unloadingType,
          helper.goodsUnloadedDate,
          helper.canSealsBeRead,
          helper.anySealsBroken
        ).flatten
      )

      val commentsSection = StaticSection(
        sectionTitle = messages("checkYourAnswers.subsections.additionalComments"),
        rows = Seq(
          helper.unloadingCommentsYesNo,
          helper.addCommentsYesNo,
          helper.additionalComment,
          helper.addReportYesNo,
          helper.report
        ).flatten
      )

      val discrepanciesPresent = userAnswers.get(AddUnloadingCommentsYesNoPage) match {
        case Some(false) => false
        case _           => true
      }

      new CheckYourAnswersViewModel(
        Seq(headerSection, commentsSection),
        discrepanciesPresent
      )
    }
  }
}
