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

import models.reference.Country
import models.{UnloadingPermission, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.{CheckYourAnswersHelper, UnloadingSummaryHelper}
import viewModels.sections.Section

class CheckYourAnswersViewModel {

  def apply(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission,
    country: Option[Country]
  )(implicit messages: Messages): Seq[Section] =
    Seq(
      goodsUnloadedSection(userAnswers),
      sealsSection(userAnswers, unloadingPermission),
      itemsSection(userAnswers, unloadingPermission, country)
    )

  private def sealsSection(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  )(implicit messages: Messages): Section = {
    val helper = new CheckYourAnswersHelper(userAnswers)

    val rows = Seq(
      helper.seals(unloadingPermission.seals.map(_.SealId)),
      helper.canSealsBeRead,
      helper.areAnySealsBroken
    ).flatten

    Section(messages("checkYourAnswers.seals.subHeading"), rows)
  }

  private def goodsUnloadedSection(userAnswers: UserAnswers)(implicit messages: Messages): Section = {
    val checkYourAnswersRow                      = new CheckYourAnswersHelper(userAnswers)
    val rowGoodsUnloaded: Option[SummaryListRow] = checkYourAnswersRow.dateGoodsUnloaded
    Section(rowGoodsUnloaded.toSeq)
  }

  private def itemsSection(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission,
    country: Option[Country]
  )(implicit messages: Messages): Section = {
    val helper = new UnloadingSummaryHelper(userAnswers, unloadingPermission)

    val rows = Seq(
      helper.vehicleUsed,
      helper.registeredCountry(country),
      helper.grossMass,
      helper.totalNumberOfItems,
      helper.totalNumberOfPackages,
      helper.comments
    ).flatten

    Section(messages("checkYourAnswers.subHeading"), rows)
  }

}
