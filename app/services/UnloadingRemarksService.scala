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

import cats.data.OptionT
import cats.implicits._
import com.google.inject.Inject
import connectors.UnloadingConnector
import logging.Logging
import models.messages._
import models.{ArrivalId, UnloadingPermission, UserAnswers}
import pages._
import repositories.InterchangeControlReferenceIdRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksService @Inject() (
  metaService: MetaService,
  remarksService: RemarksService,
  unloadingRemarksRequestService: UnloadingRemarksRequestService,
  interchangeControlReferenceIdRepository: InterchangeControlReferenceIdRepository,
  unloadingRemarksMessageService: UnloadingRemarksMessageService,
  unloadingConnector: UnloadingConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  def submit(arrivalId: ArrivalId, userAnswers: UserAnswers, unloadingPermission: UnloadingPermission)(implicit hc: HeaderCarrier): Future[Option[Int]] =
    (for {
      interchangeControlReference <- interchangeControlReferenceIdRepository.nextInterchangeControlReferenceId()
      unloadingRemarks            <- Future.fromTry(remarksService.build(userAnswers, unloadingPermission))
      meta                    = metaService.build(interchangeControlReference)
      unloadingRemarksRequest = unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswers)
      response <- unloadingConnector.post(arrivalId, unloadingRemarksRequest)
    } yield Some(response.status)).recover {
      case ex =>
        logger.error(s"[UnloadingRemarksService][submit] Submission failed: ${ex.getMessage}")
        None
    }

  def resubmit(arrivalId: ArrivalId, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[Int]] =
    (for {
      unloadingRemarksRequest <- OptionT(unloadingRemarksMessageService.unloadingRemarksMessage(arrivalId))
      updatedUnloadingRemarks <- OptionT(getUpdatedUnloadingRemarkRequest(unloadingRemarksRequest, userAnswers))
      response                <- OptionT.liftF(unloadingConnector.post(arrivalId, updatedUnloadingRemarks))
    } yield response.status).value

  private[services] def getUpdatedUnloadingRemarkRequest(
    unloadingRemarksRequest: UnloadingRemarksRequest,
    userAnswers: UserAnswers
  ): Future[Option[UnloadingRemarksRequest]] =
    interchangeControlReferenceIdRepository
      .nextInterchangeControlReferenceId()
      .map {
        interchangeControlReference =>
          val meta: Meta                      = metaService.build(interchangeControlReference)
          val unloadingRemarksRequestWithMeta = unloadingRemarksRequest.copy(meta = meta)
          userAnswers.get(DateGoodsUnloadedPage) match {
            case Some(localDate) =>
              val unloadingRemarks: Remarks = getUpdatedGoodsUnloadedDate(unloadingRemarksRequestWithMeta, localDate)
              Some(unloadingRemarksRequestWithMeta.copy(unloadingRemark = unloadingRemarks))
            case _ =>
              updatedResultsOfControl(unloadingRemarksRequestWithMeta, userAnswers) map {
                resultOfControls =>
                  unloadingRemarksRequestWithMeta.copy(resultOfControl = resultOfControls)
              }
          }
      }

  private def getUpdatedGoodsUnloadedDate(unloadingRemarksRequestWithMeta: UnloadingRemarksRequest, localDate: LocalDate): Remarks =
    unloadingRemarksRequestWithMeta.unloadingRemark match {
      case y: RemarksNonConform => y.copy(unloadingDate = localDate)
      case x                    => x
    }

  private def updatedResultsOfControl(unloadingRemarksRequest: UnloadingRemarksRequest, userAnswers: UserAnswers): Option[Seq[ResultsOfControl]] =
    getResultOfControlCorrectedValue(userAnswers) map {
      case (newValue, pointerIdentity) =>
        unloadingRemarksRequest.resultOfControl map {
          case differentValues: ResultsOfControlDifferentValues if differentValues.pointerToAttribute.pointer == pointerIdentity =>
            differentValues.copy(correctedValue = newValue)
          case roc: ResultsOfControl => roc
        }
    }

  private def getResultOfControlCorrectedValue(userAnswers: UserAnswers): Option[(String, PointerIdentity)] =
    userAnswers.get(VehicleNameRegistrationReferencePage).map((_, TransportIdentity)) orElse
      userAnswers.get(TotalNumberOfPackagesPage).map(_.toString).map((_, NumberOfPackages)) orElse
      userAnswers.get(TotalNumberOfItemsPage).map(_.toString).map((_, NumberOfItems)) orElse
      userAnswers.get(GrossWeightPage).map((_, GrossWeight))
}
