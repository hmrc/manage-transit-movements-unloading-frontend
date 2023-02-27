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

package services

import derivable.DeriveNumberOfSeals
import models.messages._
import models.{Seals, UnloadingPermission, UserAnswers}
import pages._
import queries.SealsQuery

import java.time.LocalDate
import scala.util._

class RemarksServiceImpl extends RemarksService {

  import RemarksService._

  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Try[Remarks] =
    userAnswers.get(DateGoodsUnloadedPage) match {
      case Some(date) =>
        Success(
          Seq(
            unloadingPermissionContainsSeals(userAnswers, date, unloadingPermission),
            unloadingPermissionDoesNotContainSeals(userAnswers, date, unloadingPermission)
          ).reduce(_ orElse _).apply(unloadingPermission.seals)
        )
      case None =>
        Failure(new NoSuchElementException("date goods unloaded not found"))
    }

  private def unloadingPermissionContainsSeals(
    userAnswers: UserAnswers,
    unloadingDate: LocalDate,
    originalValues: UnloadingPermission
  ): PartialFunction[Option[Seals], Remarks] = {
    case Some(Seals(unloadingPermissionSeals)) if unloadingPermissionSeals.nonEmpty =>
      if (
        haveSealsChanged(unloadingPermissionSeals, userAnswers) ||
        sealsUnreadable(userAnswers) ||
        sealsBroken(userAnswers)
      ) {
        RemarksNonConform(
          stateOfSeals = Some(0),
          unloadingRemark = userAnswers.get(UnloadingReportPage),
          unloadingDate = unloadingDate
        )
      } else {
        (hasGrossMassChanged(originalValues.grossMass, userAnswers),
         hasNumberOfItemsChanged(originalValues.numberOfItems, userAnswers),
         hasTotalNumberOfPackagesChanged(originalValues.numberOfPackages, userAnswers)
        ) match {
          case (false, false, false) =>
            RemarksConformWithSeals(
              unloadingRemark = userAnswers.get(UnloadingReportPage),
              unloadingDate = unloadingDate
            )
          case _ =>
            RemarksNonConform(
              stateOfSeals = Some(1),
              unloadingRemark = userAnswers.get(UnloadingReportPage),
              unloadingDate = unloadingDate
            )
        }
      }
  }

  private def unloadingPermissionDoesNotContainSeals(
    userAnswers: UserAnswers,
    unloadingDate: LocalDate,
    originalValues: UnloadingPermission
  ): PartialFunction[Option[Seals], Remarks] = {
    case None =>
      userAnswers.get(DeriveNumberOfSeals) match {
        case Some(_) =>
          RemarksNonConform(
            stateOfSeals = Some(0),
            unloadingRemark = userAnswers.get(UnloadingReportPage),
            unloadingDate = unloadingDate
          )
        case None =>
          (hasGrossMassChanged(originalValues.grossMass, userAnswers),
           hasNumberOfItemsChanged(originalValues.numberOfItems, userAnswers),
           hasTotalNumberOfPackagesChanged(originalValues.numberOfPackages, userAnswers)
          ) match {
            case (false, false, false) =>
              RemarksConform(
                unloadingRemark = userAnswers.get(UnloadingReportPage),
                unloadingDate = unloadingDate
              )
            case _ =>
              RemarksNonConform(
                stateOfSeals = None,
                unloadingRemark = userAnswers.get(UnloadingReportPage),
                unloadingDate = unloadingDate
              )
          }
      }
  }
}

object RemarksService {

  def sealsUnreadable(userAnswers: UserAnswers): Boolean =
    !userAnswers.get(CanSealsBeReadPage).getOrElse(true)

  def sealsBroken(userAnswers: UserAnswers): Boolean =
    userAnswers.get(AreAnySealsBrokenPage).getOrElse(false)

  def haveSealsChanged(originalSeals: Seq[String], userAnswers: UserAnswers): Boolean =
    userAnswers.get(SealsQuery).exists {
      _.map(_.sealId).sorted != originalSeals.sorted
    }

  def hasGrossMassChanged(originalValue: String, userAnswers: UserAnswers): Boolean =
    userAnswers.get(GrossMassAmountPage).exists {
      _ != originalValue
    }

  def hasNumberOfItemsChanged(originalValue: Int, userAnswers: UserAnswers): Boolean =
    userAnswers.get(TotalNumberOfItemsPage).exists {
      _ != originalValue
    }

  def hasTotalNumberOfPackagesChanged(originalValue: Option[Int], userAnswers: UserAnswers): Boolean =
    userAnswers.get(TotalNumberOfPackagesPage).exists {
      !originalValue.contains(_)
    }
}

trait RemarksService {
  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Try[Remarks]
}
