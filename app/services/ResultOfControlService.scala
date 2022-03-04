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

import models.{Seals, UnloadingPermission, UserAnswers}
import models.messages._
import models.reference.Country
import pages._
import queries.SealsQuery

class ResultOfControlServiceImpl extends ResultOfControlService {

  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Seq[ResultsOfControl] = {

    implicit val ua: UserAnswers = userAnswers

    val vehicleRegistrationReference: Seq[ResultsOfControl] = resultsOfControlString(VehicleNameRegistrationReferencePage, TransportIdentity)

    val vehicleRegistrationCountry: Seq[ResultsOfControlDifferentValues] = resultsOfControlCountry(VehicleRegistrationCountryPage, TransportCountry)

    val totalNumberOfItemsPage: Seq[ResultsOfControl] = resultsOfControlInt(TotalNumberOfItemsPage, NumberOfItems)

    val totalNumberOfPackagesPage: Seq[ResultsOfControl] = resultsOfControlInt(TotalNumberOfPackagesPage, NumberOfPackages)

    val grossMassAmount: Seq[ResultsOfControl] = resultsOfControlString(GrossMassAmountPage, GrossMass)

    val sealsBroken: Seq[ResultsOfControl] = resultsOfControlOther

    val sealsUpdated: Seq[ResultsOfControl] = resultsOfControlSealsUpdated(unloadingPermission.seals)

    Seq(vehicleRegistrationReference,
        vehicleRegistrationCountry,
        totalNumberOfItemsPage,
        totalNumberOfPackagesPage,
        grossMassAmount,
        sealsBroken,
        sealsUpdated
    ).flatten
  }

  private def resultsOfControlString(questionPage: QuestionPage[String], pointerIdentity: PointerIdentity)(implicit
    ua: UserAnswers
  ): Seq[ResultsOfControlDifferentValues] =
    ua.get(questionPage)
      .map {
        value =>
          ResultsOfControlDifferentValues(
            PointerToAttribute(pointerIdentity),
            value
          )
      }
      .toSeq

  private def resultsOfControlInt(questionPage: QuestionPage[Int], pointerIdentity: PointerIdentity)(implicit
    ua: UserAnswers
  ): Seq[ResultsOfControlDifferentValues] =
    ua.get(questionPage)
      .map {
        value =>
          ResultsOfControlDifferentValues(
            PointerToAttribute(pointerIdentity),
            value.toString
          )
      }
      .toSeq

  private def resultsOfControlCountry(questionPage: QuestionPage[Country], pointerIdentity: PointerIdentity)(implicit
    ua: UserAnswers
  ): Seq[ResultsOfControlDifferentValues] =
    ua.get(questionPage) match {
      case Some(Country(code, _)) =>
        Seq(
          ResultsOfControlDifferentValues(
            PointerToAttribute(pointerIdentity),
            code
          )
        )
      case _ => Seq.empty
    }

  private def resultsOfControlOther()(implicit ua: UserAnswers): Seq[ResultsOfControlOther] =
    (ua.get(AreAnySealsBrokenPage), ua.get(CanSealsBeReadPage)) match {
      case (Some(true), Some(false)) => Seq(ResultsOfControlSealsBroken, ResultsOfControlSealsNotReadable)
      case (Some(true), _)           => Seq(ResultsOfControlSealsBroken)
      case (_, Some(false))          => Seq(ResultsOfControlSealsNotReadable)
      case _                         => Nil
    }

  private def resultsOfControlSealsUpdated(unloadingPermissionSeals: Option[Seals])(implicit ua: UserAnswers): Seq[ResultsOfControlOther] =
    unloadingPermissionSeals match {
      case Some(Seals(_, originalSeals)) if originalSeals.nonEmpty =>
        if (RemarksService.haveSealsChanged(originalSeals, ua)) {
          Seq(ResultsOfControlSealsUpdated)
        } else {
          Nil
        }
      case _ =>
        if (ua.get(SealsQuery).exists(_.nonEmpty)) {
          Seq(ResultsOfControlSealsUpdated)
        } else {
          Nil
        }
    }
}

trait ResultOfControlService {
  def build(userAnswers: UserAnswers, unloadingPermissionSeals: UnloadingPermission): Seq[ResultsOfControl]
}
