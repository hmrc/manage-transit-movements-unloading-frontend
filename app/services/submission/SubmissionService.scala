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
import models.UnloadingSubmissionValues._
import models.{ArrivalId, DocType, EoriNumber, Index, RichCC043CType, StateOfSeals, UnloadingType, UserAnswers}
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
        unloadingRemark  <- __.read[UnloadingRemarkType](unloadingRemarkReads(userAnswers))
        consignment      <- ConsignmentSection.path.readSafe(consignmentReads(userAnswers.ie043Data.Consignment))
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
          attributes = attributes
        )
      }

    userAnswers.data.as[CC044CType]
  }

  def attributes: Map[String, DataRecord[?]] =
    Map("@PhaseID" -> DataRecord(PhaseIDtype.fromString(NCTS5u461Value.toString, scope)))

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

  def unloadingRemarkReads(userAnswers: UserAnswers): Reads[UnloadingRemarkType] = {
    import pages._

    lazy val revisedProcedureReads: Reads[UnloadingRemarkType] =
      UnloadingRemarkType(
        conform = Conform,
        unloadingCompletion = FullyUnloaded,
        unloadingDate = dateTimeService.currentDateTime.toLocalDate,
        stateOfSeals = Option.when(userAnswers.ie043Data.sealsExist)(PresentAndNotDamaged),
        unloadingRemark = None
      )

    lazy val unrevisedProcedureReads: Reads[UnloadingRemarkType] =
      for {
        unloadingCompletion <- UnloadingTypePage.path.read[UnloadingType].map(unloadingTypeToFlag)
        unloadingDate       <- DateGoodsUnloadedPage.path.read[LocalDate].map(localDateToXMLGregorianCalendar)
        unloadingRemark     <- UnloadingCommentsPage.path.readNullable[String]
        stateOfSeals        <- __.read[StateOfSeals].map(_.value)
        conform <- stateOfSeals match {
          case Some(false) => Reads.pure(false)
          case _           => AddTransitUnloadingPermissionDiscrepanciesYesNoPage.path.read[Boolean].map(!_)
        }
      } yield UnloadingRemarkType(
        conform = conform,
        unloadingCompletion = unloadingCompletion,
        unloadingDate = unloadingDate,
        stateOfSeals = stateOfSeals,
        unloadingRemark = unloadingRemark
      )

    lazy val cannotUseRevisedUnloadingProcedureReads: Reads[UnloadingRemarkType] =
      UnloadingRemarkType(
        conform = NotConform,
        unloadingCompletion = FullyUnloaded,
        unloadingDate = dateTimeService.currentDateTime.toLocalDate,
        stateOfSeals = None,
        unloadingRemark = None
      )

    NewAuthYesNoPage.path.read[Boolean].flatMap {
      case true => revisedProcedureReads
      case false =>
        LargeUnsealedGoodsRecordDiscrepanciesYesNoPage.path.readNullable[Boolean].flatMap {
          case Some(true) => cannotUseRevisedUnloadingProcedureReads
          case _          => unrevisedProcedureReads
        }
    }
  }

  def consignmentReads(ie043: Option[CUSTOM_ConsignmentType05]): Reads[Option[ConsignmentType06]] = {
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
      grossMass               <- pages.GrossWeightPage.readNullable(identity).apply(ie043)
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
      for {
        removed <- (__ \ Removed).readNullable[Boolean]
        identifier <- removed match {
          case Some(true) =>
            successfulReads(SealIdentificationNumberPage(index, sealIndex).valueInIE043(ie043, Some(sequenceNumber)))
          case _ =>
            SealIdentificationNumberPage(index, sealIndex).readNullable(identity).apply(ie043)
        }
      } yield identifier.map {
        value =>
          SealType02(
            sequenceNumber = sequenceNumber,
            identifier = value
          )
      }

    def goodsReferenceReads(
      ie043: Seq[GoodsReferenceType02]
    )(goodsReferenceIndex: Index, sequenceNumber: BigInt): Reads[Option[GoodsReferenceType01]] =
      for {
        removed <- (__ \ Removed).readNullable[Boolean]
        declarationGoodsItemNumber <- removed match {
          case Some(true) =>
            successfulReads(ItemPage(index, goodsReferenceIndex).valueInIE043(ie043, Some(sequenceNumber)))
          case _ =>
            ItemPage(index, goodsReferenceIndex).readNullable(identity).apply(ie043)
        }
      } yield declarationGoodsItemNumber.map {
        value =>
          GoodsReferenceType01(
            sequenceNumber = sequenceNumber,
            declarationGoodsItemNumber = value
          )
      }

    lazy val transportEquipment = ie043.find(_.sequenceNumber == sequenceNumber)
    lazy val seals              = transportEquipment.getList(_.Seal)
    lazy val goodsReferences    = transportEquipment.getList(_.GoodsReference)

    for {
      removed                       <- (__ \ Removed).readNullable[Boolean]
      containerIdentificationNumber <- ContainerIdentificationNumberPage(index).readNullable(identity).apply(transportEquipment)
      numberOfSeals <- SealsSection(index).count {
        !_.validate((__ \ Removed).readNullable[Boolean]).asOpt.flatten.contains(true)
      }
      seals           <- SealsSection(index).readArray(sealReads(seals))
      goodsReferences <- ItemsSection(index).readArray(goodsReferenceReads(goodsReferences))
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
                numberOfSeals = Some(numberOfSeals),
                Seal = seals,
                GoodsReference = goodsReferences
              )
            )
        }
    }
  }
  // scalastyle:on method.length

  private def consignmentDepartureTransportMeansReads(
    ie043: Seq[CUSTOM_DepartureTransportMeansType02]
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
    ie043: Seq[CUSTOM_HouseConsignmentType04]
  )(index: Index, sequenceNumber: BigInt): Reads[Option[HouseConsignmentType05]] = {
    import pages.houseConsignment.index._
    import pages.sections.ItemsSection
    import pages.sections.houseConsignment.index.additionalReference._
    import pages.sections.houseConsignment.index.departureTransportMeans._
    import pages.sections.houseConsignment.index.documents._

    lazy val houseConsignment        = ie043.find(_.sequenceNumber == sequenceNumber)
    lazy val departureTransportMeans = houseConsignment.getList(_.DepartureTransportMeans)
    lazy val supportingDocuments     = houseConsignment.getList(_.SupportingDocument)
    lazy val transportDocuments      = houseConsignment.getList(_.TransportDocument)
    lazy val additionalReferences    = houseConsignment.getList(_.AdditionalReference)
    lazy val consignmentItems        = houseConsignment.getList(_.ConsignmentItem)

    for {
      removed                 <- (__ \ Removed).readNullable[Boolean]
      grossMass               <- GrossWeightPage(index).readNullable(identity).apply(ie043)
      departureTransportMeans <- TransportMeansListSection(index).readArray(houseConsignmentDepartureTransportMeansReads(departureTransportMeans)(index))
      supportingDocuments     <- DocumentsSection(index).readArray(houseConsignmentSupportingDocumentReads(supportingDocuments)(index))
      transportDocuments      <- DocumentsSection(index).readArray(houseConsignmentTransportDocumentReads(transportDocuments)(index))
      additionalReferences    <- AdditionalReferenceListSection(index).readArray(houseConsignmentAdditionalReferenceReads(additionalReferences)(index))
      consignmentItems        <- ItemsSection(index).readArray(consignmentItemReads(consignmentItems)(index))
    } yield removed match {
      case Some(true) =>
        Some(
          HouseConsignmentType05(
            sequenceNumber = sequenceNumber
          )
        )
      case _ =>
        (grossMass, departureTransportMeans, supportingDocuments, transportDocuments, additionalReferences, consignmentItems) match {
          case (None, Nil, Nil, Nil, Nil, Nil) =>
            None
          case _ =>
            Some(
              HouseConsignmentType05(
                sequenceNumber = sequenceNumber,
                grossMass = grossMass,
                DepartureTransportMeans = departureTransportMeans,
                SupportingDocument = supportingDocuments,
                TransportDocument = transportDocuments,
                AdditionalReference = additionalReferences,
                ConsignmentItem = consignmentItems
              )
            )
        }
    }
  }

  private def houseConsignmentDepartureTransportMeansReads(
    ie043: Seq[DepartureTransportMeansType02]
  )(
    houseConsignmentIndex: Index
  )(index: Index, sequenceNumber: BigInt): Reads[Option[DepartureTransportMeansType04]] = {
    import pages.houseConsignment.index.departureMeansOfTransport._

    for {
      removed              <- (__ \ Removed).readNullable[Boolean]
      typeOfIdentification <- TransportMeansIdentificationPage(houseConsignmentIndex, index).readNullable(_.`type`).apply(ie043)
      identificationNumber <- VehicleIdentificationNumberPage(houseConsignmentIndex, index).readNullable(identity).apply(ie043)
      nationality          <- CountryPage(houseConsignmentIndex, index).readNullable(_.code).apply(ie043)
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

  private def houseConsignmentSupportingDocumentReads(
    ie043: Seq[SupportingDocumentType02]
  )(
    houseConsignmentIndex: Index
  )(index: Index, sequenceNumber: BigInt): Reads[Option[SupportingDocumentType03]] = {
    import pages.houseConsignment.index.documents._

    (TypePage(houseConsignmentIndex, index).path.last \ "type").read[DocType].flatMap {
      case DocType.Support =>
        for {
          removed                 <- (__ \ Removed).readNullable[Boolean]
          typeValue               <- SupportingTypePage(houseConsignmentIndex, index).readNullable(_.code).apply(ie043)
          referenceNumber         <- SupportingDocumentReferenceNumberPage(houseConsignmentIndex, index).readNullable(identity).apply(ie043)
          complementOfInformation <- AdditionalInformationPage(houseConsignmentIndex, index).readNullable(identity).apply(ie043)
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

  private def houseConsignmentTransportDocumentReads(
    ie043: Seq[TransportDocumentType02]
  )(
    houseConsignmentIndex: Index
  )(index: Index, sequenceNumber: BigInt): Reads[Option[TransportDocumentType03]] = {
    import pages.houseConsignment.index.documents._

    (TypePage(houseConsignmentIndex, index).path.last \ "type").read[DocType].flatMap {
      case DocType.Transport =>
        for {
          removed         <- (__ \ Removed).readNullable[Boolean]
          typeValue       <- TransportTypePage(houseConsignmentIndex, index).readNullable(_.code).apply(ie043)
          referenceNumber <- TransportDocumentReferenceNumberPage(houseConsignmentIndex, index).readNullable(identity).apply(ie043)
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

  private def houseConsignmentAdditionalReferenceReads(
    ie043: Seq[AdditionalReferenceType03]
  )(
    houseConsignmentIndex: Index
  )(index: Index, sequenceNumber: BigInt): Reads[Option[AdditionalReferenceType06]] = {
    import pages.houseConsignment.index.additionalReference._

    for {
      removed         <- (__ \ Removed).readNullable[Boolean]
      typeValue       <- HouseConsignmentAdditionalReferenceTypePage(houseConsignmentIndex, index).readNullable(_.documentType).apply(ie043)
      referenceNumber <- HouseConsignmentAdditionalReferenceNumberPage(houseConsignmentIndex, index).readNullable(identity).apply(ie043)
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

  // scalastyle:off method.length
  def consignmentItemReads(
    ie043: Seq[CUSTOM_ConsignmentItemType04]
  )(
    houseConsignmentIndex: Index
  )(itemIndex: Index, sequenceNumber: BigInt): Reads[Option[ConsignmentItemType05]] = {
    import pages.houseConsignment.index.items._
    import pages.sections.PackagingListSection
    import pages.sections.houseConsignment.index.items.additionalReference._
    import pages.sections.houseConsignment.index.items.documents.DocumentsSection

    lazy val consignmentItem      = ie043.find(_.goodsItemNumber == sequenceNumber)
    lazy val commodity            = consignmentItem.map(_.Commodity)
    lazy val packaging            = consignmentItem.getList(_.Packaging)
    lazy val supportingDocuments  = consignmentItem.getList(_.SupportingDocument)
    lazy val transportDocuments   = consignmentItem.getList(_.TransportDocument)
    lazy val additionalReferences = consignmentItem.getList(_.AdditionalReference)

    for {
      removed                    <- (__ \ Removed).readNullable[Boolean]
      declarationGoodsItemNumber <- DeclarationGoodsItemNumberPage(houseConsignmentIndex, itemIndex).path.last.read[BigInt]
      commodity                  <- (__ \ "Commodity").readSafe(consignmentItemCommodityReads(commodity)(houseConsignmentIndex, itemIndex))
      packaging <- PackagingListSection(houseConsignmentIndex, itemIndex).readArray(
        consignmentItemPackagingReads(packaging)(houseConsignmentIndex, itemIndex)
      )
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
        (commodity, packaging, supportingDocuments, transportDocuments, additionalReferences) match {
          case (None, Nil, Nil, Nil, Nil) =>
            None
          case _ =>
            Some(
              ConsignmentItemType05(
                goodsItemNumber = sequenceNumber,
                declarationGoodsItemNumber = declarationGoodsItemNumber,
                Commodity = commodity,
                Packaging = packaging,
                SupportingDocument = supportingDocuments,
                TransportDocument = transportDocuments,
                AdditionalReference = additionalReferences
              )
            )
        }
    }
  }
  // scalastyle:on method.length

  // scalastyle:off method.length
  private def consignmentItemCommodityReads(
    ie043: Option[CUSTOM_CommodityType08]
  )(
    houseConsignmentIndex: Index,
    itemIndex: Index
  ): Reads[Option[CommodityType03]] = {
    import pages.houseConsignment.index.items._

    lazy val commodityCode = ie043.flatMap(_.CommodityCode)
    lazy val goodsMeasure  = ie043.flatMap(_.GoodsMeasure)

    def commodityCodeReads(ie043: Option[CommodityCodeType05]): Reads[Option[CommodityCodeType03]] =
      for {
        harmonizedSystemSubHeadingCode <- CommodityCodePage(houseConsignmentIndex, itemIndex).readNullable(identity).apply(ie043)
        combinedNomenclatureCode       <- CombinedNomenclatureCodePage(houseConsignmentIndex, itemIndex).readNullable(identity).apply(ie043)
      } yield harmonizedSystemSubHeadingCode.map {
        value =>
          CommodityCodeType03(
            harmonizedSystemSubHeadingCode = value,
            combinedNomenclatureCode = combinedNomenclatureCode
          )
      }

    def goodsMeasureReads(ie043: Option[CUSTOM_GoodsMeasureType03]): Reads[Option[GoodsMeasureType04]] =
      for {
        grossMass <- GrossWeightPage(houseConsignmentIndex, itemIndex).readNullable(identity).apply(ie043)
        netMass   <- NetWeightPage(houseConsignmentIndex, itemIndex).readNullable(identity).apply(ie043)
      } yield (grossMass, netMass) match {
        case (None, None) =>
          None
        case _ =>
          Some(
            GoodsMeasureType04(
              grossMass = grossMass,
              netMass = netMass
            )
          )
      }

    for {
      descriptionOfGoods <- ItemDescriptionPage(houseConsignmentIndex, itemIndex).readNullable(identity).apply(ie043)
      cusCode            <- CustomsUnionAndStatisticsCodePage(houseConsignmentIndex, itemIndex).readNullable(identity).apply(ie043)
      commodityCode      <- (__ \ "CommodityCode").readSafe(commodityCodeReads(commodityCode))
      goodsMeasure       <- (__ \ "GoodsMeasure").readSafe(goodsMeasureReads(goodsMeasure))
    } yield (descriptionOfGoods, cusCode, commodityCode, goodsMeasure) match {
      case (None, None, None, None) =>
        None
      case _ =>
        Some(
          CommodityType03(
            descriptionOfGoods = descriptionOfGoods,
            cusCode = cusCode,
            CommodityCode = commodityCode,
            GoodsMeasure = goodsMeasure
          )
        )
    }
  }
  // scalastyle:on method.length

  private def consignmentItemPackagingReads(
    ie043: Seq[PackagingType02]
  )(
    houseConsignmentIndex: Index,
    itemIndex: Index
  )(index: Index, sequenceNumber: BigInt): Reads[Option[PackagingType04]] = {
    import pages.houseConsignment.index.items.packages._

    for {
      removed          <- (__ \ Removed).readNullable[Boolean]
      typeOfPackages   <- PackageTypePage(houseConsignmentIndex, itemIndex, index).readNullable(_.code).apply(ie043)
      numberOfPackages <- NumberOfPackagesPage(houseConsignmentIndex, itemIndex, index).readNullable(identity).apply(ie043)
      shippingMarks    <- PackageShippingMarkPage(houseConsignmentIndex, itemIndex, index).readNullable(identity).apply(ie043)
    } yield removed match {
      case Some(true) =>
        Some(
          PackagingType04(
            sequenceNumber = sequenceNumber
          )
        )
      case _ =>
        (typeOfPackages, numberOfPackages, shippingMarks) match {
          case (None, None, None) =>
            None
          case _ =>
            Some(
              PackagingType04(
                sequenceNumber = sequenceNumber,
                typeOfPackages = typeOfPackages,
                numberOfPackages = numberOfPackages,
                shippingMarks = shippingMarks
              )
            )
        }
    }
  }

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
