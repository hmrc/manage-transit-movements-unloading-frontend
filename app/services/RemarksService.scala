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

import com.google.inject.Inject
import derivable.DeriveNumberOfSeals
import models.messages._
import models.{Seals, UnloadingPermission, UserAnswers}
import pages._
import queries.SealsQuery

import java.time.LocalDate
import scala.concurrent.Future

class RemarksServiceImpl @Inject() (resultOfControlService: ResultOfControlService) extends RemarksService {

  import RemarksService._

  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Response =
    userAnswers.get(DateGoodsUnloadedPage) match {

      case Some(date) =>
        implicit val unloadingDate: LocalDate = date

        implicit val originalValues: UnloadingPermission = unloadingPermission

        Seq(unloadingPermissionContainsSeals(userAnswers), unloadingPermissionDoesNotContainSeals(userAnswers))
          .reduce(_ orElse _)
          .apply(unloadingPermission.seals)

      case None =>
        Future.failed(new NoSuchElementException("date goods unloaded not found"))
    }

  private def unloadingPermissionContainsSeals(userAnswers: UserAnswers)(implicit
    unloadingDate: LocalDate,
    originalValues: UnloadingPermission
  ): PartialFunction[Option[Seals], Response] = {
    case Some(Seals(_, unloadingPermissionSeals)) if unloadingPermissionSeals.nonEmpty =>
      if (
        haveSealsChanged(unloadingPermissionSeals, userAnswers) ||
        sealsUnreadable(userAnswers.get(CanSealsBeReadPage)) ||
        sealsBroken(userAnswers.get(AreAnySealsBrokenPage))
      ) {
        Future.successful(
          RemarksNonConform(
            stateOfSeals = Some(0),
            unloadingRemark = userAnswers.get(ChangesToReportPage),
            unloadingDate = unloadingDate
          )
        )
      } else {
        (hasGrossMassChanged(originalValues.grossMass, userAnswers),
         hasNumberOfItemsChanged(originalValues.numberOfItems, userAnswers),
         hasTotalNumberOfPackagesChanged(originalValues.numberOfPackages, userAnswers)
        ) match {
          case (false, false, false) =>
            Future.successful(
              RemarksConformWithSeals(
                unloadingRemark = userAnswers.get(ChangesToReportPage),
                unloadingDate = unloadingDate
              )
            )
          case (_, _, _) =>
            Future.successful(
              RemarksNonConform(
                stateOfSeals = Some(1),
                unloadingRemark = userAnswers.get(ChangesToReportPage),
                unloadingDate = unloadingDate
              )
            )
        }

      }

  }

  private def unloadingPermissionDoesNotContainSeals(userAnswers: UserAnswers)(implicit
    unloadingDate: LocalDate,
    originalValues: UnloadingPermission
  ): PartialFunction[Option[Seals], Response] = {
    case None =>
      userAnswers.get(DeriveNumberOfSeals) match {
        case Some(_) =>
          Future.successful(
            RemarksNonConform(
              stateOfSeals = Some(0),
              unloadingRemark = userAnswers.get(ChangesToReportPage),
              unloadingDate = unloadingDate
            )
          )
        case None =>
          (hasGrossMassChanged(originalValues.grossMass, userAnswers),
           hasNumberOfItemsChanged(originalValues.numberOfItems, userAnswers),
           hasTotalNumberOfPackagesChanged(originalValues.numberOfPackages, userAnswers)
          ) match {
            case (false, false, false) =>
              Future.successful(
                RemarksConform(
                  unloadingRemark = userAnswers.get(ChangesToReportPage),
                  unloadingDate = unloadingDate
                )
              )
            case (_, _, _) =>
              Future.successful(
                RemarksNonConform(
                  stateOfSeals = None,
                  unloadingRemark = userAnswers.get(ChangesToReportPage),
                  unloadingDate = unloadingDate
                )
              )
          }
      }
  }
}

object RemarksService {

  type Response = Future[Remarks]

  def sealsUnreadable(canSealsBeReadPage: Option[Boolean]): Boolean =
    !canSealsBeReadPage.getOrElse(true)

  def sealsBroken(areAnySealsBrokenPage: Option[Boolean]): Boolean =
    areAnySealsBrokenPage.getOrElse(false)

  def haveSealsChanged(originalSeals: Seq[String], userAnswers: UserAnswers): Boolean =
    userAnswers.get(SealsQuery).exists {
      userSeals =>
        userSeals.sorted != originalSeals.sorted
    }

  def hasGrossMassChanged(originalValue: String, userAnswers: UserAnswers): Boolean =
    userAnswers.get(GrossMassAmountPage).exists {
      userGrossMass =>
        userGrossMass != originalValue
    }

  def hasNumberOfItemsChanged(originalValue: Int, userAnswers: UserAnswers): Boolean =
    userAnswers.get(TotalNumberOfItemsPage).exists {
      userNumberItems =>
        userNumberItems != originalValue
    }

  def hasTotalNumberOfPackagesChanged(originalValue: Option[Int], userAnswers: UserAnswers): Boolean =
    userAnswers.get(TotalNumberOfPackagesPage).exists(!originalValue.contains(_))
}

trait RemarksService {
  def build(userAnswers: UserAnswers, unloadingPermission: UnloadingPermission): Future[Remarks]
}
