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

package services

import javax.inject.Inject
import models.{Seals, UnloadingPermission, UserAnswers}
import models.messages._
import queries.SealsQuery

class UnloadingRemarksRequestServiceImpl @Inject() (resultOfControlService: ResultOfControlService) extends UnloadingRemarksRequestService {

  def build(meta: Meta, unloadingRemarks: Remarks, unloadingPermission: UnloadingPermission, userAnswers: UserAnswers): UnloadingRemarksRequest = {

    val header = Header(
      movementReferenceNumber = unloadingPermission.movementReferenceNumber,
      transportIdentity = unloadingPermission.transportIdentity,
      transportCountry = unloadingPermission.transportCountry,
      numberOfItems = unloadingPermission.numberOfItems,
      numberOfPackages = unloadingPermission.numberOfPackages,
      grossMass = unloadingPermission.grossMass
    )

    val seals: Option[Seals] = unloadingRemarks match {
      case _: RemarksConform                => None
      case _: RemarksConformWithSeals       => unloadingPermission.seals
      case RemarksNonConform(None, _, _)    => unloadingPermission.seals
      case RemarksNonConform(Some(1), _, _) => unloadingPermission.seals
      case _ =>
        userAnswers
          .get(SealsQuery)
          .map {
            seals =>
              Some(Seals(seals.length, seals))
          }
          .getOrElse(unloadingPermission.seals)
    }
    val resultsOfControl: Seq[ResultsOfControl] = resultOfControlService.build(userAnswers, unloadingPermission)

    UnloadingRemarksRequest(
      meta,
      header,
      unloadingPermission.traderAtDestination,
      unloadingPermission.presentationOffice,
      unloadingRemarks,
      resultsOfControl,
      seals
    )
  }

}

trait UnloadingRemarksRequestService {
  def build(meta: Meta, unloadingRemarks: Remarks, unloadingPermission: UnloadingPermission, userAnswers: UserAnswers): UnloadingRemarksRequest
}
