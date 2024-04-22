/*
 * Copyright 2024 HM Revenue & Customs
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

package services.submission

import connectors.ApiConnector
import generated._
import models.{ArrivalId, EoriNumber, Index, UnloadingType, UserAnswers}
import pages.sections.ConsignmentSection
import pages.sections.additionalReference.AdditionalReferencesSection
import play.api.libs.json.{__, Reads}
import scalaxb.DataRecord
import scalaxb.`package`.toXML
import services.DateTimeService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.transformers.Removed

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future
import scala.xml.{NamespaceBinding, NodeSeq}

class SubmissionService @Inject() (
  dateTimeService: DateTimeService,
  messageIdentificationService: MessageIdentificationService,
  connector: ApiConnector
) {

  private val scope: NamespaceBinding = scalaxb.toScope(Some("ncts") -> "http://ncts.dgtaxud.ec")

  def submit(userAnswers: UserAnswers, arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    connector.submit(buildXml(userAnswers), arrivalId)

  private def buildXml(userAnswers: UserAnswers): NodeSeq =
    toXML(transform(userAnswers), s"ncts:${CC044C.toString}", scope)

  private def transform(userAnswers: UserAnswers): CC044CType = {
    val officeOfDestination = userAnswers.ie043Data.CustomsOfficeOfDestinationActual.referenceNumber
    implicit val reads: Reads[CC044CType] =
      for {
        transitOperation <- __.read[TransitOperationType15](transitOperationReads(userAnswers))
        unloadingRemark  <- __.read[UnloadingRemarkType]
        consignment      <- ConsignmentSection.path.readNullableSafe[ConsignmentType06](consignmentReads(userAnswers.ie043Data.Consignment))
      } yield CC044CType(
        messageSequence1 = messageSequence(userAnswers.eoriNumber, officeOfDestination),
        TransitOperation = transitOperation,
        CustomsOfficeOfDestinationActual = CustomsOfficeOfDestinationActualType03(
          referenceNumber = officeOfDestination
        ),
        TraderAtDestination = TraderAtDestinationType02(
          identificationNumber = userAnswers.ie043Data.TraderAtDestination.identificationNumber
        ),
        UnloadingRemark = unloadingRemark,
        Consignment = consignment,
        attributes = Map("@PhaseID" -> DataRecord(PhaseIDtype.fromString("NCTS5.0", scope)))
      )

    userAnswers.data.as[CC044CType]
  }

  def messageSequence(eoriNumber: EoriNumber, officeOfDestination: String): MESSAGESequence =
    MESSAGESequence(
      messageSender = eoriNumber.value,
      messageRecipient = s"NTA.${officeOfDestination.take(2)}",
      preparationDateAndTime = dateTimeService.currentDateTime,
      messageIdentification = messageIdentificationService.randomIdentifier,
      messageType = CC044C,
      correlationIdentifier = None
    )

  def transitOperationReads(userAnswers: UserAnswers): Reads[TransitOperationType15] = {
    import pages.OtherThingsToReportPage

    OtherThingsToReportPage.path.readNullable[String].map {
      otherThingsToReport =>
        TransitOperationType15(
          MRN = userAnswers.mrn.value,
          otherThingsToReport = otherThingsToReport
        )
    }
  }

  implicit val unloadingRemarkReads: Reads[UnloadingRemarkType] = {
    import pages._

    for {
      unloadingCompletion <- UnloadingTypePage.path.read[UnloadingType].map(unloadingTypeToFlag)
      unloadingDate       <- DateGoodsUnloadedPage.path.read[LocalDate].map(localDateToXMLGregorianCalendar)
      canSealsBeRead      <- CanSealsBeReadPage.path.readNullable[Boolean]
      areAnySealsBroken   <- AreAnySealsBrokenPage.path.readNullable[Boolean]
      unloadingRemark     <- UnloadingCommentsPage.path.readNullable[String]
      stateOfSeals = (canSealsBeRead, areAnySealsBroken) match {
        case (Some(true), Some(false)) => Some(Number1)
        case (Some(_), Some(_))        => Some(Number0)
        case _                         => None
      }
      conform <- stateOfSeals match {
        case Some(Number0) => Number0: Reads[Flag]
        case _             => AddTransitUnloadingPermissionDiscrepanciesYesNoPage.path.read[Boolean].map(!_).map(boolToFlag)
      }
    } yield UnloadingRemarkType(
      conform = conform,
      unloadingCompletion = unloadingCompletion,
      unloadingDate = unloadingDate,
      stateOfSeals = stateOfSeals,
      unloadingRemark = unloadingRemark
    )
  }

  def consignmentReads(ie043: Option[ConsignmentType05]): Reads[ConsignmentType06] = {
    import pages.grossMass.GrossMassPage
    import pages.sections._

    lazy val transportEquipment      = ie043.getList(_.TransportEquipment)
    lazy val departureTransportMeans = ie043.getList(_.DepartureTransportMeans)
    lazy val additionalReferences    = ie043.getList(_.AdditionalReference)

    for {
      grossMass               <- GrossMassPage.readNullable(identity).apply(ie043)
      transportEquipment      <- TransportEquipmentListSection.readArray(consignmentTransportEquipmentReads(transportEquipment))
      departureTransportMeans <- TransportMeansListSection.readArray(consignmentDepartureTransportMeansReads(departureTransportMeans))
      additionalReferences    <- AdditionalReferencesSection.readArray(consignmentAdditionalReferenceReads(additionalReferences))
    } yield ConsignmentType06(
      grossMass = grossMass,
      TransportEquipment = transportEquipment,
      DepartureTransportMeans = departureTransportMeans,
      SupportingDocument = Nil,
      TransportDocument = Nil,
      AdditionalReference = additionalReferences,
      HouseConsignment = Nil
    )
  }

  // scalastyle:off method.length
  private def consignmentTransportEquipmentReads(
    ie043: Seq[TransportEquipmentType05]
  )(index: Index, sequenceNumber: BigInt): Reads[Option[TransportEquipmentType03]] = {
    import pages.ContainerIdentificationNumberPage
    import pages.sections.SealsSection
    import pages.sections.transport.equipment.ItemsSection
    import pages.transportEquipment.index._
    import pages.transportEquipment.index.seals._

    def sealReads(
      ie043: Seq[SealType04]
    )(sealIndex: Index, sequenceNumber: BigInt): Reads[Option[SealType02]] =
      SealIdentificationNumberPage(index, sealIndex).readNullable(identity).apply(ie043).map {
        _.map {
          identifier =>
            SealType02(
              sequenceNumber = sequenceNumber,
              identifier = identifier
            )
        }
      }

    def goodsReferenceReads(
      ie043: Seq[GoodsReferenceType02]
    )(goodsReferenceIndex: Index, sequenceNumber: BigInt): Reads[Option[GoodsReferenceType01]] =
      ItemPage(index, goodsReferenceIndex).readNullable(identity).apply(ie043).map {
        _.map {
          declarationGoodsItemNumber =>
            GoodsReferenceType01(
              sequenceNumber = sequenceNumber,
              declarationGoodsItemNumber = declarationGoodsItemNumber
            )
        }
      }

    val transportEquipment = ie043.find(_.sequenceNumber == sequenceNumber)
    val seals              = transportEquipment.getList(_.Seal)
    val goodsReferences    = transportEquipment.getList(_.GoodsReference)

    for {
      removed                       <- (__ \ Removed).readNullable[Boolean]
      containerIdentificationNumber <- ContainerIdentificationNumberPage(index).readNullable(identity).apply(transportEquipment)
      seals                         <- SealsSection(index).readArray[SealType02](sealReads(seals))
      goodsReferences               <- ItemsSection(index).readArray[GoodsReferenceType01](goodsReferenceReads(goodsReferences))
    } yield removed match {
      case Some(true) =>
        Some(
          TransportEquipmentType03(
            sequenceNumber = sequenceNumber
          )
        )
      case _ =>
        (containerIdentificationNumber, seals, goodsReferences) match {
          case (None, Nil, Nil) =>
            None
          case _ =>
            Some(
              TransportEquipmentType03(
                sequenceNumber = sequenceNumber,
                containerIdentificationNumber = containerIdentificationNumber,
                numberOfSeals = Some(seals.length),
                Seal = seals,
                GoodsReference = goodsReferences
              )
            )
        }
    }
  }
  // scalastyle:on method.length

  private def consignmentDepartureTransportMeansReads(
    ie043: Seq[DepartureTransportMeansType02]
  )(index: Index, sequenceNumber: BigInt): Reads[Option[DepartureTransportMeansType04]] = {
    import pages.departureMeansOfTransport._

    for {
      removed              <- (__ \ Removed).readNullable[Boolean]
      typeOfIdentification <- TransportMeansIdentificationPage(index).readNullable(_.`type`).apply(ie043)
      identificationNumber <- VehicleIdentificationNumberPage(index).readNullable(identity).apply(ie043)
      nationality          <- CountryPage(index).readNullable(_.code).apply(ie043)
    } yield removed match {
      case Some(true) =>
        Some(
          DepartureTransportMeansType04(
            sequenceNumber = sequenceNumber
          )
        )
      case _ =>
        (typeOfIdentification, identificationNumber, nationality) match {
          case (None, None, None) =>
            None
          case _ =>
            Some(
              DepartureTransportMeansType04(
                sequenceNumber = sequenceNumber,
                typeOfIdentification = typeOfIdentification,
                identificationNumber = identificationNumber,
                nationality = nationality
              )
            )
        }
    }
  }

  private def consignmentAdditionalReferenceReads(
    ie043: Seq[AdditionalReferenceType03]
  )(index: Index, sequenceNumber: BigInt): Reads[Option[AdditionalReferenceType06]] = {
    import pages.additionalReference._

    for {
      removed         <- (__ \ Removed).readNullable[Boolean]
      typeValue       <- AdditionalReferenceTypePage(index).readNullable(_.documentType).apply(ie043)
      referenceNumber <- AdditionalReferenceNumberPage(index).readNullable(identity).apply(ie043)
    } yield removed match {
      case Some(true) =>
        Some(
          AdditionalReferenceType06(
            sequenceNumber = sequenceNumber
          )
        )
      case _ =>
        (typeValue, referenceNumber) match {
          case (None, None) =>
            None
          case _ =>
            Some(
              AdditionalReferenceType06(
                sequenceNumber = sequenceNumber,
                typeValue = typeValue,
                referenceNumber = referenceNumber
              )
            )
        }
    }
  }
}
