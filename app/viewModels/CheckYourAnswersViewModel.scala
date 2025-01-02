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
import pages.{
  AddTransitUnloadingPermissionDiscrepanciesYesNoPage,
  GoodsTooLargeForContainerYesNoPage,
  LargeUnsealedGoodsRecordDiscrepanciesYesNoPage,
  NewAuthYesNoPage,
  RevisedUnloadingProcedureConditionsYesNoPage
}
import play.api.i18n.Messages
import utils.answersHelpers.CheckYourAnswersHelper
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection

import javax.inject.Inject

case class CheckYourAnswersViewModel(
  procedureSection: Section,
  warning: Option[String],
  sections: Seq[Section],
  showDiscrepanciesLink: Boolean,
  goodsTooLarge: Option[Boolean]
)

object CheckYourAnswersViewModel {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages): CheckYourAnswersViewModel =
    new CheckYourAnswersViewModelProvider()(userAnswers)

  class CheckYourAnswersViewModelProvider @Inject() () {

    def apply(userAnswers: UserAnswers)(implicit messages: Messages): CheckYourAnswersViewModel = {
      val helper = new CheckYourAnswersHelper(userAnswers)

      val procedureSection = StaticSection(
        rows = Seq(
          helper.newProcedure,
          helper.revisedUnloadingProcedureConditionsYesNo,
          helper.goodsTooLarge,
          helper.largeUnsealedGoodsRecordDiscrepanciesYesNo
        ).flatten
      )

      // warning

      val unloadingSection = StaticSection(
        rows = Seq(
          helper.unloadingType,
          helper.goodsUnloadedDate,
          helper.canSealsBeRead,
          helper.anySealsBroken
        ).flatten
      )

      val discrepanciesSection = StaticSection(
        sectionTitle = messages("checkYourAnswers.subsections.additionalComments"),
        rows = Seq(
          helper.addDiscrepanciesYesNo,
          helper.addCommentsYesNo,
          helper.additionalComment,
          helper.sealsReplaced,
          helper.addReportYesNo,
          helper.report
        ).flatten
      )

      val discrepanciesPresent =
        (userAnswers.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage), userAnswers.get(NewAuthYesNoPage)) match {
          case (Some(false), _) => false
          case (_, Some(true))  => false
          case _                => true
        }

      val warning = (
        userAnswers.get(RevisedUnloadingProcedureConditionsYesNoPage),
        userAnswers.get(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage)
      ) match {
        case (Some(false), _) | (_, Some(true)) => Some(messages("site.warning.procedure"))
        case _                                  => None
      }

      new CheckYourAnswersViewModel(
        procedureSection,
        warning,
        Seq(unloadingSection, discrepanciesSection),
        discrepanciesPresent,
        userAnswers.get(GoodsTooLargeForContainerYesNoPage)
      )
    }
  }
}
