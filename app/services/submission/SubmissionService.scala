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
import models.{ArrivalId, DocType, EoriNumber, Index, UnloadingType, UserAnswers}
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
    import pages.sections.ConsignmentSection

    implicit val reads: Reads[CC044CType] =
      for {
        transitOperation <- __.read[TransitOperationType15](transitOperationReads(userAnswers))
        unloadingRemark  <- __.read[UnloadingRemarkType]
        consignment      <- ConsignmentSection.path.read[Option[ConsignmentType06]](consignmentReads(userAnswers.ie043Data.Consignment))
      } yield {
        val officeOfDestination = userAnswers.ie043Data.CustomsOfficeOfDestinationActual.referenceNumber
        CC044CType(
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
      }

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

  def consignmentReads(ie043: Option[ConsignmentType05]): Reads[Option[ConsignmentType06]] = {
    import pages.grossMass.GrossMassPage
    import pages.sections._
    import pages.sections.additionalReference.AdditionalReferencesSection
    import pages.sections.documents.DocumentsSection

    lazy val transportEquipment      = ie043.getList(_.TransportEquipment)
    lazy val departureTransportMeans = ie043.getList(_.DepartureTransportMeans)
    lazy val supportingDocuments     = ie043.getList(_.SupportingDocument)
    lazy val transportDocuments      = ie043.getList(_.TransportDocument)
    lazy val additionalReferences    = ie043.getList(_.AdditionalReference)
    lazy val houseConsignments       = ie043.getList(_.HouseConsignment)

    for {
      grossMass               <- GrossMassPage.readNullable(identity).apply(ie043)
      transportEquipment      <- TransportEquipmentListSection.readArray(consignmentTransportEquipmentReads(transportEquipment))
      departureTransportMeans <- TransportMeansListSection.readArray(consignmentDepartureTransportMeansReads(departureTransportMeans))
      supportingDocuments     <- DocumentsSection.readArray(consignmentSupportingDocumentReads(supportingDocuments))
      transportDocuments      <- DocumentsSection.readArray(consignmentTransportDocumentReads(transportDocuments))
      additionalReferences    <- AdditionalReferencesSection.readArray(consignmentAdditionalReferenceReads(additionalReferences))
      houseConsignments       <- HouseConsignmentsSection.readArray(houseConsignmentReads(houseConsignments))
    } yield (grossMass, transportEquipment, departureTransportMeans, supportingDocuments, transportDocuments, additionalReferences, houseConsignments) match {
      case (None, Nil, Nil, Nil, Nil, Nil, Nil) =>
        None
      case _ =>
        Some(
          ConsignmentType06(
            grossMass = grossMass,
            TransportEquipment = transportEquipment,
            DepartureTransportMeans = departureTransportMeans,
            SupportingDocument = supportingDocuments,
            TransportDocument = transportDocuments,
            AdditionalReference = additionalReferences,
            HouseConsignment = houseConsignments
          )
        )
    }
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

    lazy val transportEquipment = ie043.find(_.sequenceNumber == sequenceNumber)
    lazy val seals              = transportEquipment.getList(_.Seal)
    lazy val goodsReferences    = transportEquipment.getList(_.GoodsReference)

    for {
      removed                       <- (__ \ Removed).readNullable[Boolean]
      containerIdentificationNumber <- ContainerIdentificationNumberPage(index).readNullable(identity).apply(transportEquipment)
      seals                         <- SealsSection(index).readArray(sealReads(seals))
      goodsReferences               <- ItemsSection(index).readArray(goodsReferenceReads(goodsReferences))
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

  private def consignmentSupportingDocumentReads(
    ie043: Seq[SupportingDocumentType02]
  )(index: Index, sequenceNumber: BigInt): Reads[Option[SupportingDocumentType03]] = {
    import pages.documents._

    (TypePage(index).path.last \ "type").read[DocType].flatMap {
      case DocType.Support =>
        for {
          removed                 <- (__ \ Removed).readNullable[Boolean]
          typeValue               <- SupportingTypePage(index).readNullable(_.code).apply(ie043)
          referenceNumber         <- SupportingDocumentReferenceNumberPage(index).readNullable(identity, 2).apply(ie043)
          complementOfInformation <- AdditionalInformationPage(index).readNullable(identity, 2).apply(ie043)
        } yield removed match {
          case Some(true) =>
            Some(
              SupportingDocumentType03(
                sequenceNumber = sequenceNumber
              )
            )
          case _ =>
            (typeValue, referenceNumber, complementOfInformation) match {
              case (None, None, None) =>
                None
              case _ =>
                Some(
                  SupportingDocumentType03(
                    sequenceNumber = sequenceNumber,
                    typeValue = typeValue,
                    referenceNumber = referenceNumber,
                    complementOfInformation = complementOfInformation
                  )
                )
            }
        }
      case _ =>
        None
    }
  }

  private def consignmentTransportDocumentReads(
    ie043: Seq[TransportDocumentType02]
  )(index: Index, sequenceNumber: BigInt): Reads[Option[TransportDocumentType03]] = {
    import pages.documents._

    (TypePage(index).path.last \ "type").read[DocType].flatMap {
      case DocType.Transport =>
        for {
          removed         <- (__ \ Removed).readNullable[Boolean]
          typeValue       <- TransportTypePage(index).readNullable(_.code).apply(ie043)
          referenceNumber <- TransportDocumentReferenceNumberPage(index).readNullable(identity, 2).apply(ie043)
        } yield removed match {
          case Some(true) =>
            Some(
              TransportDocumentType03(
                sequenceNumber = sequenceNumber
              )
            )
          case _ =>
            (typeValue, referenceNumber) match {
              case (None, None) =>
                None
              case _ =>
                Some(
                  TransportDocumentType03(
                    sequenceNumber = sequenceNumber,
                    typeValue = typeValue,
                    referenceNumber = referenceNumber
                  )
                )
            }
        }
      case _ =>
        None
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

  def houseConsignmentReads(
    ie043: Seq[HouseConsignmentType04]
  )(index: Index, sequenceNumber: BigInt): Reads[Option[HouseConsignmentType05]] = {
    import pages.sections.ItemsSection

    lazy val houseConsignment = ie043.find(_.sequenceNumber == sequenceNumber)
    lazy val consignmentItems = houseConsignment.getList(_.ConsignmentItem)

    for {
      removed          <- (__ \ Removed).readNullable[Boolean]
      consignmentItems <- ItemsSection(index).readArray(consignmentItemReads(consignmentItems)(index))
    } yield removed match {
      case Some(true) =>
        Some(
          HouseConsignmentType05(
            sequenceNumber = sequenceNumber
          )
        )
      case _ =>
        consignmentItems match {
          case Nil =>
            None
          case _ =>
            Some(
              HouseConsignmentType05(
                sequenceNumber = sequenceNumber,
                grossMass = None,
                DepartureTransportMeans = Nil,
                SupportingDocument = Nil,
                TransportDocument = Nil,
                AdditionalReference = Nil,
                ConsignmentItem = consignmentItems
              )
            )
        }
    }
  }

  // scalastyle:off method.length
  def consignmentItemReads(
    ie043: Seq[ConsignmentItemType04]
  )(
    houseConsignmentIndex: Index
  )(itemIndex: Index, sequenceNumber: BigInt): Reads[Option[ConsignmentItemType05]] = {
    import pages.houseConsignment.index.items._
    import pages.sections.houseConsignment.index.items.additionalReference._
    import pages.sections.houseConsignment.index.items.documents.DocumentsSection

    lazy val consignmentItem      = ie043.find(_.goodsItemNumber == sequenceNumber)
    lazy val supportingDocuments  = consignmentItem.getList(_.SupportingDocument)
    lazy val transportDocuments   = consignmentItem.getList(_.TransportDocument)
    lazy val additionalReferences = consignmentItem.getList(_.AdditionalReference)

    for {
      removed                    <- (__ \ Removed).readNullable[Boolean]
      declarationGoodsItemNumber <- DeclarationGoodsItemNumberPage(houseConsignmentIndex, itemIndex).path.last.read[BigInt]
      supportingDocuments <- DocumentsSection(houseConsignmentIndex, itemIndex).readArray(
        consignmentItemSupportingDocumentReads(supportingDocuments)(houseConsignmentIndex, itemIndex)
      )
      transportDocuments <- DocumentsSection(houseConsignmentIndex, itemIndex).readArray(
        consignmentItemTransportDocumentReads(transportDocuments)(houseConsignmentIndex, itemIndex)
      )
      additionalReferences <- AdditionalReferencesSection(houseConsignmentIndex, itemIndex).readArray(
        consignmentItemAdditionalReferenceReads(additionalReferences)(houseConsignmentIndex, itemIndex)
      )
    } yield removed match {
      case Some(true) =>
        Some(
          ConsignmentItemType05(
            goodsItemNumber = sequenceNumber,
            declarationGoodsItemNumber = declarationGoodsItemNumber
          )
        )
      case _ =>
        (supportingDocuments, transportDocuments, additionalReferences) match {
          case (Nil, Nil, Nil) =>
            None
          case _ =>
            Some(
              ConsignmentItemType05(
                goodsItemNumber = sequenceNumber,
                declarationGoodsItemNumber = declarationGoodsItemNumber,
                Commodity = None,
                Packaging = Nil,
                SupportingDocument = supportingDocuments,
                TransportDocument = transportDocuments,
                AdditionalReference = additionalReferences
              )
            )
        }
    }
  }
  // scalastyle:on method.length

  private def consignmentItemSupportingDocumentReads(
    ie043: Seq[SupportingDocumentType02]
  )(
    houseConsignmentIndex: Index,
    itemIndex: Index
  )(index: Index, sequenceNumber: BigInt): Reads[Option[SupportingDocumentType03]] = {
    import pages.houseConsignment.index.items.document._

    (TypePage(houseConsignmentIndex, itemIndex, index).path.last \ "type").read[DocType].flatMap {
      case DocType.Support =>
        for {
          removed                 <- (__ \ Removed).readNullable[Boolean]
          typeValue               <- SupportingTypePage(houseConsignmentIndex, itemIndex, index).readNullable(_.code).apply(ie043)
          referenceNumber         <- SupportingDocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, index).readNullable(identity).apply(ie043)
          complementOfInformation <- AdditionalInformationPage(houseConsignmentIndex, itemIndex, index).readNullable(identity).apply(ie043)
        } yield removed match {
          case Some(true) =>
            Some(
              SupportingDocumentType03(
                sequenceNumber = sequenceNumber
              )
            )
          case _ =>
            (typeValue, referenceNumber, complementOfInformation) match {
              case (None, None, None) =>
                None
              case _ =>
                Some(
                  SupportingDocumentType03(
                    sequenceNumber = sequenceNumber,
                    typeValue = typeValue,
                    referenceNumber = referenceNumber,
                    complementOfInformation = complementOfInformation
                  )
                )
            }
        }
      case _ =>
        None
    }
  }

  private def consignmentItemTransportDocumentReads(
    ie043: Seq[TransportDocumentType02]
  )(
    houseConsignmentIndex: Index,
    itemIndex: Index
  )(index: Index, sequenceNumber: BigInt): Reads[Option[TransportDocumentType03]] = {
    import pages.houseConsignment.index.items.document._

    (TypePage(houseConsignmentIndex, itemIndex, index).path.last \ "type").read[DocType].flatMap {
      case DocType.Transport =>
        for {
          removed         <- (__ \ Removed).readNullable[Boolean]
          typeValue       <- TransportTypePage(houseConsignmentIndex, itemIndex, index).readNullable(_.code).apply(ie043)
          referenceNumber <- TransportDocumentReferenceNumberPage(houseConsignmentIndex, itemIndex, index).readNullable(identity).apply(ie043)
        } yield removed match {
          case Some(true) =>
            Some(
              TransportDocumentType03(
                sequenceNumber = sequenceNumber
              )
            )
          case _ =>
            (typeValue, referenceNumber) match {
              case (None, None) =>
                None
              case _ =>
                Some(
                  TransportDocumentType03(
                    sequenceNumber = sequenceNumber,
                    typeValue = typeValue,
                    referenceNumber = referenceNumber
                  )
                )
            }
        }
      case _ =>
        None
    }
  }

  private def consignmentItemAdditionalReferenceReads(
    ie043: Seq[AdditionalReferenceType02]
  )(
    houseConsignmentIndex: Index,
    itemIndex: Index
  )(index: Index, sequenceNumber: BigInt): Reads[Option[AdditionalReferenceType06]] = {
    import pages.houseConsignment.index.items.additionalReference._

    for {
      removed         <- (__ \ Removed).readNullable[Boolean]
      typeValue       <- AdditionalReferenceTypePage(houseConsignmentIndex, itemIndex, index).readNullable(_.documentType).apply(ie043)
      referenceNumber <- AdditionalReferenceNumberPage(houseConsignmentIndex, itemIndex, index).readNullable(identity).apply(ie043)
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
