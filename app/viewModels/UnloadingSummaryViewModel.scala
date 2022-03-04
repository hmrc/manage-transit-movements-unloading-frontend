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
import queries.SealsQuery
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels._
import utils.UnloadingSummaryHelper
import viewModels.sections.Section

case class UnloadingSummaryViewModel(sections: Seq[Section])

object UnloadingSummaryViewModel {

  def apply(userAnswers: UserAnswers, transportCountry: Option[Country])(implicit unloadingPermission: UnloadingPermission): UnloadingSummaryViewModel = {

    implicit val unloadingSummaryRow: UnloadingSummaryHelper = new UnloadingSummaryHelper(userAnswers)

    UnloadingSummaryViewModel(TransportSection(userAnswers, transportCountry) ++ ItemsSection(userAnswers))
  }

}

object SealsSection {

  def apply(userAnswers: UserAnswers)(implicit unloadingPermission: UnloadingPermission, unloadingSummaryRow: UnloadingSummaryHelper): Option[Seq[Section]] =
    userAnswers.get(SealsQuery) match {
      case Some(seals) =>
        val rows: Seq[Row] = seals.zipWithIndex.map {
          case (sealNumber, index) =>
            unloadingPermission.seals match {
              case Some(existingSeals) if existingSeals.SealId.length >= index + 1 =>
                SummaryRow.rowWithIndex(Index(index))(None)(sealNumber)(unloadingSummaryRow.seals)

              case _ => SummaryRow.rowWithIndex(Index(index))(None)(sealNumber)(unloadingSummaryRow.sealsWithRemove)
            }
        }

        Some(Seq(Section(msg"changeSeal.title", rows)))

      case None =>
        unloadingPermission.seals match {
          case Some(seals) =>
            val rows: Seq[Row] = seals.SealId.zipWithIndex.map {
              case (sealNumber, index) =>
                val sealAnswer = SummaryRow.userAnswerWithIndex(Index(index))(userAnswers)(NewSealNumberPage)
                SummaryRow.rowWithIndex(Index(index))(sealAnswer)(sealNumber)(unloadingSummaryRow.seals)
            }

            Some(Seq(Section(msg"changeSeal.title", rows)))
          case None =>
            None
        }
    }
}

object TransportSection {

  def apply(userAnswers: UserAnswers, summaryTransportCountry: Option[Country])(implicit
    unloadingPermission: UnloadingPermission,
    unloadingSummaryRow: UnloadingSummaryHelper
  ): Seq[Section] = {

    val vehicleAnswer: Option[String] = SummaryRow.userAnswerString(userAnswers)(VehicleNameRegistrationReferencePage)
    val transportIdentity: Seq[Row]   = SummaryRow.row(vehicleAnswer)(unloadingPermission.transportIdentity)(unloadingSummaryRow.vehicleUsed)

    val transportCountryDescription: Option[String] = summaryTransportCountry match {
      case Some(country) => Some(country.description)
      case None          => unloadingPermission.transportCountry
    }

    val countryAnswer: Option[String] = SummaryRow.userAnswerCountry(userAnswers)(VehicleRegistrationCountryPage)
    val transportCountry: Seq[Row]    = SummaryRow.row(countryAnswer)(transportCountryDescription)(unloadingSummaryRow.registeredCountry)

    transportIdentity ++ transportCountry match {
      case transport if transport.nonEmpty =>
        Seq(Section(msg"vehicleUsed.title", transport))
      case _ => Nil
    }
  }
}

object ItemsSection {

  def apply(userAnswers: UserAnswers)(implicit unloadingPermission: UnloadingPermission, unloadingSummaryRow: UnloadingSummaryHelper): Seq[Section] = {
    val grossMassAnswer: Option[String] = SummaryRow.userAnswerString(userAnswers)(GrossMassAmountPage)
    val grossMassRow: Seq[Row]          = SummaryRow.row(grossMassAnswer)(Some(unloadingPermission.grossMass))(unloadingSummaryRow.grossMass)

    val totalNumberOfItemsAnswer: Option[Int] = SummaryRow.userAnswerInt(userAnswers)(TotalNumberOfItemsPage)
    val totalNumberOfItemsRow: Seq[Row] =
      SummaryRow.rowInt(totalNumberOfItemsAnswer)(Some(unloadingPermission.numberOfItems))(unloadingSummaryRow.totalNumberOfItems)

    val totalNumberOfPackagesAnswer: Option[Int] = SummaryRow.userAnswerInt(userAnswers)(TotalNumberOfPackagesPage)
    val totalNumberOfPackagesRow: Seq[Row] =
      SummaryRow.rowInt(totalNumberOfPackagesAnswer)(unloadingPermission.numberOfPackages)(unloadingSummaryRow.totalNumberOfPackages)

    val itemsRow: NonEmptyList[Row]    = SummaryRow.rowGoodsItems(unloadingPermission.goodsItems)(userAnswers)(unloadingSummaryRow.items)
    val commentsAnswer: Option[String] = SummaryRow.userAnswerString(userAnswers)(ChangesToReportPage)
    val commentsRow: Seq[Row]          = SummaryRow.row(commentsAnswer)(None)(unloadingSummaryRow.comments)

    Seq(Section(msg"changeItems.title", grossMassRow ++ totalNumberOfItemsRow ++ totalNumberOfPackagesRow ++ itemsRow.toList ++ commentsRow))
  }
}
