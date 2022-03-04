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

import java.time.LocalDate

import com.google.inject.Inject
import connectors.UnloadingConnector
import logging.Logging
import models.messages._
import models.{ArrivalId, UnloadingPermission, UserAnswers}
import pages._
import play.api.http.Status._
import repositories.InterchangeControlReferenceIdRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UnloadingRemarksService @Inject() (metaService: MetaService,
                                         remarksService: RemarksService,
                                         unloadingRemarksRequestService: UnloadingRemarksRequestService,
                                         interchangeControlReferenceIdRepository: InterchangeControlReferenceIdRepository,
                                         unloadingRemarksMessageService: UnloadingRemarksMessageService,
                                         unloadingConnector: UnloadingConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  def submit(arrivalId: ArrivalId, userAnswers: UserAnswers, unloadingPermission: UnloadingPermission)(implicit hc: HeaderCarrier): Future[Option[Int]] =
    interchangeControlReferenceIdRepository
      .nextInterchangeControlReferenceId()
      .flatMap {
        interchangeControlReference =>
          remarksService
            .build(userAnswers, unloadingPermission)
            .flatMap {
              unloadingRemarks =>
                val meta: Meta = metaService.build(interchangeControlReference)

                val unloadingRemarksRequest: UnloadingRemarksRequest =
                  unloadingRemarksRequestService.build(meta, unloadingRemarks, unloadingPermission, userAnswers)

                unloadingConnector
                  .post(arrivalId, unloadingRemarksRequest)
                  .flatMap(
                    response => Future.successful(Some(response.status))
                  )
                  .recover {
                    case ex =>
                      logger.error(s"$ex")
                      Some(SERVICE_UNAVAILABLE)
                  }
            }
      }
      .recover {
        case ex =>
          logger.error(s"$ex")
          None
      }

  def resubmit(arrivalId: ArrivalId, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[Int]] =
    unloadingRemarksMessageService.unloadingRemarksMessage(arrivalId) flatMap {
      case Some(unloadingRemarksRequest) =>
        getUpdatedUnloadingRemarkRequest(unloadingRemarksRequest, userAnswers) flatMap {
          case Some(updatedUnloadingRemarks) =>
            unloadingConnector
              .post(arrivalId, updatedUnloadingRemarks)
              .map(
                response => Some(response.status)
              )
          case _ => logger.debug("Failed to get updated unloading remarks request"); Future.successful(None)
        }
      case _ => logger.debug("Failed to get unloading remarks request: Service.unloadingRemarksMessage(arrivalId)"); Future.successful(None)
    }

  private[services] def getUpdatedUnloadingRemarkRequest(unloadingRemarksRequest: UnloadingRemarksRequest,
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
    getResultOfControlCorrectedValue(userAnswers: UserAnswers) match {
      case Some((newValue, pointerIdentity: PointerIdentity)) =>
        val resultsOfControl = unloadingRemarksRequest.resultOfControl.map {
          case differentValues: ResultsOfControlDifferentValues if differentValues.pointerToAttribute.pointer == pointerIdentity =>
            differentValues.copy(correctedValue = newValue)
          case roc: ResultsOfControl => roc
        }
        Some(resultsOfControl)
      case _ => None
    }

  private def getResultOfControlCorrectedValue(userAnswers: UserAnswers): Option[(String, PointerIdentity)] =
    userAnswers.get(VehicleNameRegistrationReferencePage) match {
      case Some(answer) => Some((answer, TransportIdentity))
      case _ =>
        userAnswers.get(TotalNumberOfPackagesPage) match {
          case Some(answer) => Some((answer.toString, NumberOfPackages))
          case _ =>
            userAnswers.get(TotalNumberOfItemsPage) match {
              case Some(answer) => Some((answer.toString, NumberOfItems))
              case _ =>
                userAnswers.get(GrossMassAmountPage) match {
                  case Some(answer) => Some((answer, GrossMass))
                  case _            => None
                }
            }
        }
    }
}
