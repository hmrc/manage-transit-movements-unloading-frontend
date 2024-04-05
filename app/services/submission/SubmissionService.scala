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
import play.api.libs.json.{__, Reads}
import scalaxb.DataRecord
import scalaxb.`package`.toXML
import services.DateTimeService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.transformers.SequenceNumber

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
        consignment      <- __.readNullableSafe[ConsignmentType06]
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
      messagE_1Sequence2 = MESSAGE_1Sequence(
        messageRecipient = s"NTA.${officeOfDestination.take(2)}",
        preparationDateAndTime = dateTimeService.currentDateTime,
        messageIdentification = messageIdentificationService.randomIdentifier
      ),
      messagE_TYPESequence3 = MESSAGE_TYPESequence(
        messageType = CC044C
      ),
      correlatioN_IDENTIFIERSequence4 = CORRELATION_IDENTIFIERSequence(
        correlationIdentifier = None
      )
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
      conform             <- AddUnloadingCommentsYesNoPage.path.read[Boolean].map(!_).map(boolToFlag)
      unloadingCompletion <- UnloadingTypePage.path.read[UnloadingType].map(unloadingTypeToFlag)
      unloadingDate       <- DateGoodsUnloadedPage.path.read[LocalDate].map(localDateToXMLGregorianCalendar)
      canSealsBeRead      <- CanSealsBeReadPage.path.readNullable[Boolean]
      areAnySealsBroken   <- AreAnySealsBrokenPage.path.readNullable[Boolean]
      unloadingRemark     <- UnloadingCommentsPage.path.readNullable[String]
    } yield UnloadingRemarkType(
      conform = conform,
      unloadingCompletion = unloadingCompletion,
      unloadingDate = unloadingDate,
      stateOfSeals = (canSealsBeRead, areAnySealsBroken) match {
        case (Some(true), Some(false)) => Some(Number1)
        case (Some(_), Some(_))        => Some(Number0)
        case _                         => None
      },
      unloadingRemark = unloadingRemark
    )
  }

  implicit val consignmentReads: Reads[ConsignmentType06] = {
    import pages.grossMass.GrossMassPage
    import pages.sections._

    for {
      grossMass          <- GrossMassPage.path.readNullable[BigDecimal]
      transportEquipment <- TransportEquipmentListSection.path.readArray(consignmentTransportEquipmentReads)
    } yield ConsignmentType06(
      grossMass = grossMass,
      TransportEquipment = transportEquipment,
      DepartureTransportMeans = Nil,
      SupportingDocument = Nil,
      TransportDocument = Nil,
      AdditionalReference = Nil,
      HouseConsignment = Nil
    )
  }

  private def consignmentTransportEquipmentReads(index: Index): Reads[TransportEquipmentType03] = {
    import pages.ContainerIdentificationNumberPage
    import pages.sections.SealsSection
    import pages.sections.transport.equipment.ItemsSection
    import pages.transportEquipment.index._
    import pages.transportEquipment.index.seals._

    def sealReads(sealIndex: Index): Reads[SealType02] =
      for {
        sequenceNumber <- (__ \ SequenceNumber).read[String]
        identifier     <- (__ \ SealIdentificationNumberPage(index, sealIndex).toString).read[String]
      } yield SealType02(
        sequenceNumber = sequenceNumber,
        identifier = identifier
      )

    def goodsReferenceReads(goodsReferenceIndex: Index): Reads[GoodsReferenceType01] =
      for {
        sequenceNumber             <- (__ \ SequenceNumber).read[String]
        declarationGoodsItemNumber <- (__ \ ItemPage(index, goodsReferenceIndex).toString).read[BigInt]
      } yield GoodsReferenceType01(
        sequenceNumber = sequenceNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber
      )

    for {
      sequenceNumber                <- (__ \ SequenceNumber).read[String]
      containerIdentificationNumber <- (__ \ ContainerIdentificationNumberPage(index).toString).readNullable[String]
      seals                         <- (__ \ SealsSection(index).toString).readArray[SealType02](sealReads)
      goodsReferences               <- (__ \ ItemsSection(index).toString).readArray[GoodsReferenceType01](goodsReferenceReads)
    } yield TransportEquipmentType03(
      sequenceNumber = sequenceNumber,
      containerIdentificationNumber = containerIdentificationNumber,
      numberOfSeals = Some(seals.length),
      Seal = seals,
      GoodsReference = goodsReferences
    )
  }
}
