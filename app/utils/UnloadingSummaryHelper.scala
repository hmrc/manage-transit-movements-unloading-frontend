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

package utils

import cats.data.NonEmptyList
import controllers.routes
import models.reference.Country
import models.{CheckMode, Index, NormalMode, UnloadingPermission, UserAnswers}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class UnloadingSummaryHelper(
  userAnswers: UserAnswers,
  unloadingPermission: UnloadingPermission
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers) {

  def seals: Seq[SummaryListRow] = {


    (index, value) =>
      SummaryListRow(
        key = messages("changeSeal.sealList.label", index.display).toKey,
        value = Value(value.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.NewSealNumberController.onPageLoad(arrivalId, index, CheckMode).url,
                visuallyHiddenText = Some(messages("changeSeal.sealList.change.hidden", index.display, value)),
                attributes = Map("id" -> s"change-seal-${index.position}")
              )
            )
          )
        )
      )
  }

  val sealsWithRemove: (Index, String) => SummaryListRow = {
    (index, value) =>
      SummaryListRow(
        key = messages("changeSeal.sealList.label", index.display).toKey,
        value = Value(value.toText),
        actions = Some(
          Actions(items =
            List(
              ActionItem(
                content = messages("site.edit").toText,
                href = routes.NewSealNumberController.onPageLoad(arrivalId, index, CheckMode).url,
                visuallyHiddenText = Some(messages("changeSeal.sealList.change.hidden", index.display, value)),
                attributes = Map("id" -> s"change-seal-${index.position}")
              ),
              ActionItem(
                content = messages("site.delete").toText,
                href = routes.ConfirmRemoveSealController.onPageLoad(arrivalId, index, CheckMode).url,
                visuallyHiddenText = Some(messages("changeSeal.sealList.remove.hidden", index.display, value)),
                attributes = Map("id" -> s"remove-seal-${index.position}")
              )
            )
          )
        )
      )
  }

  def items: NonEmptyList[SummaryListRow] =
    unloadingPermission.goodsItems.zipWithIndex.map {
      case (goodsItem, index) =>
        buildRow(
          prefix = "changeItem.itemList",
          answer = goodsItem.description.toText,
          id = None,
          call = None,
          args = Index(index).display
        )
    }

  def vehicleUsed: Option[SummaryListRow] =
    getAnswerOrAlternativeAnswerAndBuildRow[String](
      page = VehicleNameRegistrationReferencePage,
      alternativeValue = unloadingPermission.transportIdentity,
      formatAnswer = _.toText,
      prefix = "changeVehicle.reference",
      id = Some("change-vehicle-reference"),
      call = Some(routes.VehicleNameRegistrationReferenceController.onPageLoad(arrivalId, CheckMode))
    )

  def registeredCountry(country: Option[Country]): Option[SummaryListRow] =
    getAnswerOrAlternativeAnswerAndBuildRow[Country](
      page = VehicleRegistrationCountryPage,
      alternativeValue = country orElse unloadingPermission.transportCountry.map(Country("", _)),
      formatAnswer = _.description.toText,
      prefix = "changeVehicle.registeredCountry",
      id = Some("change-vehicle-country"),
      call = Some(routes.VehicleRegistrationCountryController.onPageLoad(arrivalId, CheckMode))
    )

  def grossMass: Option[SummaryListRow] =
    getAnswerOrAlternativeAnswerAndBuildRow[String](
      page = GrossMassAmountPage,
      alternativeValue = Some(unloadingPermission.grossMass),
      formatAnswer = _.toText,
      prefix = "changeItems.grossMass",
      id = Some("change-gross-mass"),
      call = Some(routes.GrossMassAmountController.onPageLoad(arrivalId, CheckMode))
    )

  def totalNumberOfItems: Option[SummaryListRow] =
    getAnswerOrAlternativeAnswerAndBuildRow[Int](
      page = TotalNumberOfItemsPage,
      alternativeValue = Some(unloadingPermission.numberOfItems),
      formatAnswer = _.toString.toText,
      prefix = "changeItems.totalNumberOfItems",
      id = Some("change-total-number-of-items"),
      call = Some(routes.TotalNumberOfItemsController.onPageLoad(arrivalId, CheckMode))
    )

  def totalNumberOfPackages: Option[SummaryListRow] =
    getAnswerOrAlternativeAnswerAndBuildRow[Int](
      page = TotalNumberOfPackagesPage,
      alternativeValue = unloadingPermission.numberOfPackages,
      formatAnswer = _.toString.toText,
      prefix = "changeItems.totalNumberOfPackages",
      id = Some("change-total-number-of-packages"),
      call = Some(routes.TotalNumberOfPackagesController.onPageLoad(arrivalId, CheckMode))
    )

  def comments: Option[SummaryListRow] =
    getAnswerOrAlternativeAnswerAndBuildRemovableRow[String](
      page = ChangesToReportPage,
      alternativeValue = None,
      formatAnswer = _.toText,
      prefix = "changeItems.comments",
      id = "comments",
      changeCall = routes.ChangesToReportController.onPageLoad(arrivalId, NormalMode),
      removeCall = routes.ConfirmRemoveCommentsController.onPageLoad(arrivalId, NormalMode)
    )
}
