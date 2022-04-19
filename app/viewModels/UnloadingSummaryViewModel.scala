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

import cats.data.NonEmptyList
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
    val unloadingSummaryHelper: UnloadingSummaryHelper = new UnloadingSummaryHelper(userAnswers)

    userAnswers.get(SealsQuery) match {
      case Some(seals) =>
        val rows: Seq[SummaryListRow] = seals.zipWithIndex.map {
          case (sealNumber, index) =>
            unloadingPermission.seals match {
              case Some(existingSeals) if existingSeals.SealId.length >= index + 1 =>
                SummaryRow.rowWithIndex(Index(index))(None)(sealNumber)(unloadingSummaryHelper.seals)
              case _ => SummaryRow.rowWithIndex(Index(index))(None)(sealNumber)(unloadingSummaryHelper.sealsWithRemove)
            }
        }
        Some(Section(messages("changeSeal.title"), rows))

      case None =>
        unloadingPermission.seals match {
          case Some(seals) =>
            val rows: Seq[SummaryListRow] = seals.SealId.zipWithIndex.map {
              case (sealNumber, index) =>
                val sealAnswer = SummaryRow.userAnswerWithIndex(Index(index))(userAnswers)(NewSealNumberPage)
                SummaryRow.rowWithIndex(Index(index))(sealAnswer)(sealNumber)(unloadingSummaryHelper.seals)
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
    val unloadingSummaryHelper: UnloadingSummaryHelper = new UnloadingSummaryHelper(userAnswers)

    val vehicleAnswer: Option[String]          = SummaryRow.userAnswerString(userAnswers)(VehicleNameRegistrationReferencePage)
    val transportIdentity: Seq[SummaryListRow] = SummaryRow.row(vehicleAnswer)(unloadingPermission.transportIdentity)(unloadingSummaryHelper.vehicleUsed)

    val transportCountryDescription: Option[String] = country match {
      case Some(value) => Some(value.description)
      case None        => unloadingPermission.transportCountry
    }

    val countryAnswer: Option[String]         = SummaryRow.userAnswerCountry(userAnswers)(VehicleRegistrationCountryPage)
    val transportCountry: Seq[SummaryListRow] = SummaryRow.row(countryAnswer)(transportCountryDescription)(unloadingSummaryHelper.registeredCountry)

    transportIdentity ++ transportCountry match {
      case transport if transport.nonEmpty =>
        Some(Section(messages("vehicleUsed.title"), transport))
      case _ => None
    }
  }
}

object ItemsSection {

  def apply(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  )(implicit messages: Messages): Section = {
    val unloadingSummaryHelper: UnloadingSummaryHelper = new UnloadingSummaryHelper(userAnswers)

    val grossMassAnswer: Option[String]   = SummaryRow.userAnswerString(userAnswers)(GrossMassAmountPage)
    val grossMassRow: Seq[SummaryListRow] = SummaryRow.row(grossMassAnswer)(Some(unloadingPermission.grossMass))(unloadingSummaryHelper.grossMass)

    val totalNumberOfItemsAnswer: Option[Int] = SummaryRow.userAnswerInt(userAnswers)(TotalNumberOfItemsPage)
    val totalNumberOfItemsRow: Seq[SummaryListRow] =
      SummaryRow.rowInt(totalNumberOfItemsAnswer)(Some(unloadingPermission.numberOfItems))(unloadingSummaryHelper.totalNumberOfItems)

    val totalNumberOfPackagesAnswer: Option[Int] = SummaryRow.userAnswerInt(userAnswers)(TotalNumberOfPackagesPage)
    val totalNumberOfPackagesRow: Seq[SummaryListRow] =
      SummaryRow.rowInt(totalNumberOfPackagesAnswer)(unloadingPermission.numberOfPackages)(unloadingSummaryHelper.totalNumberOfPackages)

    val itemsRow: NonEmptyList[SummaryListRow] = SummaryRow.rowGoodsItems(unloadingPermission.goodsItems)(userAnswers)(unloadingSummaryHelper.items)
    val commentsAnswer: Option[String]         = SummaryRow.userAnswerString(userAnswers)(ChangesToReportPage)
    val commentsRow: Seq[SummaryListRow]       = SummaryRow.row(commentsAnswer)(None)(unloadingSummaryHelper.comments)

    Section(messages("changeItems.title"), grossMassRow ++ totalNumberOfItemsRow ++ totalNumberOfPackagesRow ++ itemsRow.toList ++ commentsRow)
  }
}
