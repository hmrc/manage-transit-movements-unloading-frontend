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
import models.{Index, UnloadingPermission, UserAnswers}
import pages._
import play.api.i18n.Messages
import queries.SealsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.UnloadingSummaryHelper
import viewModels.sections.Section

class UnloadingSummaryViewModel {

  def sealsSection(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  )(implicit messages: Messages): Option[Section] =
    SealsSection.apply(userAnswers, unloadingPermission)

  def transportAndItemSections(
    userAnswers: UserAnswers,
    country: Option[Country],
    unloadingPermission: UnloadingPermission
  )(implicit messages: Messages): Seq[Section] =
    TransportSection(userAnswers, country, unloadingPermission).toSeq :+
      ItemsSection(userAnswers, unloadingPermission)
}

object SealsSection {

  def apply(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  )(implicit messages: Messages): Option[Section] = {
    val helper: UnloadingSummaryHelper = new UnloadingSummaryHelper(userAnswers, unloadingPermission)

    userAnswers.get(SealsQuery) match {
      case Some(seals) =>
        val rows: Seq[SummaryListRow] = seals.zipWithIndex.map {
          case (sealNumber, index) =>
            unloadingPermission.seals match {
              case Some(existingSeals) if existingSeals.SealId.length >= index + 1 =>
                SummaryRow.rowWithIndex(Index(index))(None)(sealNumber)(helper.seals)
              case _ => SummaryRow.rowWithIndex(Index(index))(None)(sealNumber)(helper.sealsWithRemove)
            }
        }
        Some(Section(messages("changeSeal.title"), rows))

      case None =>
        unloadingPermission.seals match {
          case Some(seals) =>
            val rows: Seq[SummaryListRow] = seals.SealId.zipWithIndex.map {
              case (sealNumber, index) =>
                val sealAnswer = SummaryRow.userAnswerWithIndex(Index(index))(userAnswers)(NewSealNumberPage)
                SummaryRow.rowWithIndex(Index(index))(sealAnswer)(sealNumber)(helper.seals)
            }
            Some(Section(messages("changeSeal.title"), rows))
          case None =>
            None
        }
    }
  }
}

object TransportSection {

  def apply(
    userAnswers: UserAnswers,
    country: Option[Country],
    unloadingPermission: UnloadingPermission
  )(implicit messages: Messages): Option[Section] = {
    val helper: UnloadingSummaryHelper = new UnloadingSummaryHelper(userAnswers, unloadingPermission)

    Seq(
      helper.vehicleUsed,
      helper.registeredCountry(country)
    ).flatten match {
      case Nil  => None
      case rows => Some(Section(messages("vehicleUsed.title"), rows))
    }
  }
}

object ItemsSection {

  def apply(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  )(implicit messages: Messages): Section = {
    val helper: UnloadingSummaryHelper = new UnloadingSummaryHelper(userAnswers, unloadingPermission)

    val rows = helper.grossMass.toSeq ++
      helper.totalNumberOfItems.toSeq ++
      helper.totalNumberOfPackages.toSeq ++
      helper.items.toList ++
      helper.comments.toSeq

    Section(messages("changeItems.title"), rows)
  }
}
