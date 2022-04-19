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
import models.{UnloadingPermission, UserAnswers}
import pages._
import play.api.i18n.Messages
import queries.SealsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.{CheckYourAnswersHelper, UnloadingSummaryHelper}
import viewModels.sections.Section

class CheckYourAnswersViewModel {

  def apply(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission,
    summaryTransportCountry: Option[Country]
  )(implicit messages: Messages): Seq[Section] =
    Seq(
      goodsUnloadedSection(userAnswers),
      sealsSection(userAnswers, unloadingPermission),
      itemsSection(userAnswers, unloadingPermission, summaryTransportCountry)
    )

  private def sealsSection(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  )(implicit messages: Messages): Section = {
    val checkYourAnswersRow = new CheckYourAnswersHelper(userAnswers)

    val rowCanSealsBeRead: Option[SummaryListRow]    = checkYourAnswersRow.canSealsBeRead
    val rowAreAnySealsBroken: Option[SummaryListRow] = checkYourAnswersRow.areAnySealsBroken

    val seals: Option[SummaryListRow] = (userAnswers.get(SealsQuery), unloadingPermission.seals) match {
      case (Some(userAnswersSeals), _)            => checkYourAnswersRow.seals(userAnswersSeals)
      case (None, Some(unloadingPermissionSeals)) => checkYourAnswersRow.seals(unloadingPermissionSeals.SealId)
      case (_, _)                                 => None
    }

    Section(messages("checkYourAnswers.seals.subHeading"), seals.toSeq ++ rowCanSealsBeRead ++ rowAreAnySealsBroken)
  }

  private def goodsUnloadedSection(userAnswers: UserAnswers)(implicit messages: Messages): Section = {
    val checkYourAnswersRow                      = new CheckYourAnswersHelper(userAnswers)
    val rowGoodsUnloaded: Option[SummaryListRow] = checkYourAnswersRow.dateGoodsUnloaded
    Section(rowGoodsUnloaded.toSeq)
  }

  private def itemsSection(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission,
    summaryTransportCountry: Option[Country]
  )(implicit messages: Messages): Section = {

    val unloadingSummaryRow = new UnloadingSummaryHelper(userAnswers)

    val transportIdentityAnswer: Option[String] = userAnswers.get(VehicleNameRegistrationReferencePage)
    val transportIdentityRow: Seq[SummaryListRow] =
      SummaryRow.row(transportIdentityAnswer)(unloadingPermission.transportIdentity)(unloadingSummaryRow.vehicleUsed)

    val transportCountryDescription: Option[String] = summaryTransportCountry match {
      case Some(country) => Some(country.description)
      case None          => unloadingPermission.transportCountry
    }

    val countryAnswer: Option[String]            = SummaryRow.userAnswerCountry(userAnswers)(VehicleRegistrationCountryPage)
    val transportCountryRow: Seq[SummaryListRow] = SummaryRow.row(countryAnswer)(transportCountryDescription)(unloadingSummaryRow.registeredCountry)

    val grossMassAnswer: Option[String]   = SummaryRow.userAnswerString(userAnswers)(GrossMassAmountPage)
    val grossMassRow: Seq[SummaryListRow] = SummaryRow.row(grossMassAnswer)(Some(unloadingPermission.grossMass))(unloadingSummaryRow.grossMass)

    val itemsRow: NonEmptyList[SummaryListRow] = SummaryRow.rowGoodsItems(unloadingPermission.goodsItems)(userAnswers)(unloadingSummaryRow.items)

    val totalNumberOfItemsAnswer: Option[Int] = SummaryRow.userAnswerInt(userAnswers)(TotalNumberOfItemsPage)
    val totalNumberOfItemsRow: Seq[SummaryListRow] =
      SummaryRow.rowInt(totalNumberOfItemsAnswer)(Some(unloadingPermission.numberOfItems))(unloadingSummaryRow.totalNumberOfItems)

    val totalNumberOfPackagesAnswer: Option[Int] = SummaryRow.userAnswerInt(userAnswers)(TotalNumberOfPackagesPage)
    val totalNumberOfPackagesRow: Seq[SummaryListRow] =
      SummaryRow.rowInt(totalNumberOfPackagesAnswer)(unloadingPermission.numberOfPackages)(unloadingSummaryRow.totalNumberOfPackages)

    val commentsAnswer: Option[String]   = SummaryRow.userAnswerString(userAnswers)(ChangesToReportPage)
    val commentsRow: Seq[SummaryListRow] = SummaryRow.row(commentsAnswer)(None)(unloadingSummaryRow.comments)

    Section(
      messages("checkYourAnswers.subHeading"),
      transportIdentityRow ++ transportCountryRow ++ grossMassRow ++ totalNumberOfItemsRow ++ totalNumberOfPackagesRow ++ itemsRow.toList ++ commentsRow
    )
  }

}
