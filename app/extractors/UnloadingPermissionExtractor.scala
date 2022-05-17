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

package extractors

import models.{Seal, UnloadingPermission, UserAnswers}
import pages._
import queries.{GoodsItemsQuery, SealsQuery}
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class UnloadingPermissionExtractor @Inject() (referenceDataService: ReferenceDataService) {

  def apply(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Try[UserAnswers]] =
    Future.fromTry {
      extractVehicleNameRegistrationReference(userAnswers, unloadingPermission)
        .flatMap(extractGrossMassAmount(_, unloadingPermission))
        .flatMap(extractTotalNumberOfItems(_, unloadingPermission))
        .flatMap(extractTotalNumberOfPackages(_, unloadingPermission))
        .flatMap(extractSeals(_, unloadingPermission))
        .flatMap(extractGoodsItems(_, unloadingPermission))
        .flatMap(extractDateOfPreparation(_, unloadingPermission))
    } flatMap {
      extractVehicleRegistrationCountryPage(_, unloadingPermission)
    }

  private def extractVehicleNameRegistrationReference(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  ): Try[UserAnswers] =
    unloadingPermission.transportIdentity match {
      case Some(transportIdentity) => userAnswers.set(VehicleNameRegistrationReferencePage, transportIdentity)
      case None                    => Success(userAnswers)
    }

  private def extractVehicleRegistrationCountryPage(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Try[UserAnswers]] =
    referenceDataService.getCountryByCode(unloadingPermission.transportCountry) map {
      case Some(country) => userAnswers.set(VehicleRegistrationCountryPage, country)
      case None          => Success(userAnswers)
    }

  private def extractGrossMassAmount(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  ): Try[UserAnswers] =
    userAnswers.set(GrossMassAmountPage, unloadingPermission.grossMass)

  private def extractTotalNumberOfItems(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  ): Try[UserAnswers] =
    userAnswers.set(TotalNumberOfItemsPage, unloadingPermission.numberOfItems)

  private def extractTotalNumberOfPackages(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  ): Try[UserAnswers] =
    unloadingPermission.numberOfPackages match {
      case Some(numberOfPackages) => userAnswers.set(TotalNumberOfPackagesPage, numberOfPackages)
      case None                   => Success(userAnswers)
    }

  private def extractSeals(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  ): Try[UserAnswers] =
    unloadingPermission.seals.map(_.sealIds) match {
      case Some(seals) => userAnswers.set(SealsQuery, seals.map(Seal(_, removable = false)))
      case None        => Success(userAnswers)
    }

  private def extractGoodsItems(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  ): Try[UserAnswers] =
    userAnswers.set(GoodsItemsQuery, unloadingPermission.goodsItems.toList.map(_.description))

  private def extractDateOfPreparation(
    userAnswers: UserAnswers,
    unloadingPermission: UnloadingPermission
  ): Try[UserAnswers] =
    userAnswers.set(DateOfPreparationPage, unloadingPermission.dateOfPreparation)
}
