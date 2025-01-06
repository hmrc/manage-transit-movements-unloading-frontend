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

import models.{Procedure, UserAnswers}
import pages.AddTransitUnloadingPermissionDiscrepanciesYesNoPage
import play.api.i18n.Messages
import utils.answersHelpers.CheckYourAnswersHelper
import viewModels.sections.Section
import viewModels.sections.Section.StaticSection

import javax.inject.Inject

case class CheckYourAnswersViewModel(
  procedureSection: Section,
  sections: Seq[Section],
  showDiscrepanciesLink: Boolean,
  procedure: Procedure
)

object CheckYourAnswersViewModel {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages): CheckYourAnswersViewModel =
    new CheckYourAnswersViewModelProvider()(userAnswers)

  class CheckYourAnswersViewModelProvider @Inject() {

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

      val procedure = Procedure(userAnswers)

      val discrepanciesPresent = procedure match {
        case Procedure.Unrevised | Procedure.CannotUseRevised =>
          userAnswers.get(AddTransitUnloadingPermissionDiscrepanciesYesNoPage) match {
            case Some(false) => false
            case _           => true
          }
        case _ => false
      }

      new CheckYourAnswersViewModel(
        procedureSection,
        Seq(unloadingSection, discrepanciesSection),
        discrepanciesPresent,
        procedure
      )
    }
  }
}
