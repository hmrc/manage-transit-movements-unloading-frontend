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

package generators

import generated._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.const
import org.scalacheck.{Arbitrary, Gen}
import scalaxb.XMLCalendar

import javax.xml.datatype.XMLGregorianCalendar

trait MessagesModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryCC043CType: Arbitrary[CC043CType] =
    Arbitrary {
      for {
        messageSequence1                 <- arbitrary[MESSAGESequence]
        transitOperation                 <- arbitrary[TransitOperationType14]
        customsOfficeOfDestinationActual <- arbitrary[CustomsOfficeOfDestinationActualType03]
        holderOfTheTransitProcedure      <- Gen.option(arbitrary[HolderOfTheTransitProcedureType06])
        traderAtDestination              <- arbitrary[TraderAtDestinationType03]
        ctlControl                       <- Gen.option(arbitrary[CTLControlType])
        consignment                      <- Gen.option(arbitrary[ConsignmentType05])
      } yield CC043CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        CustomsOfficeOfDestinationActual = customsOfficeOfDestinationActual,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        TraderAtDestination = traderAtDestination,
        CTLControl = ctlControl,
        Consignment = consignment,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryMESSAGESequence: Arbitrary[MESSAGESequence] =
    Arbitrary {
      for {
        messageSender                   <- Gen.alphaNumStr
        messagE_1Sequence2              <- arbitrary[MESSAGE_1Sequence]
        messagE_TYPESequence3           <- arbitrary[MESSAGE_TYPESequence]
        correlatioN_IDENTIFIERSequence4 <- arbitrary[CORRELATION_IDENTIFIERSequence]
      } yield MESSAGESequence(
        messageSender = messageSender,
        messagE_1Sequence2 = messagE_1Sequence2,
        messagE_TYPESequence3 = messagE_TYPESequence3,
        correlatioN_IDENTIFIERSequence4 = correlatioN_IDENTIFIERSequence4
      )
    }

  implicit lazy val arbitraryMESSAGE_1Sequence: Arbitrary[MESSAGE_1Sequence] =
    Arbitrary {
      for {
        messageRecipient       <- Gen.alphaNumStr
        preparationDateAndTime <- arbitrary[XMLGregorianCalendar]
        messageIdentification  <- Gen.alphaNumStr
      } yield MESSAGE_1Sequence(
        messageRecipient = messageRecipient,
        preparationDateAndTime = preparationDateAndTime,
        messageIdentification = messageIdentification
      )
    }

  implicit lazy val arbitraryMESSAGE_TYPESequence: Arbitrary[MESSAGE_TYPESequence] =
    Arbitrary {
      MESSAGE_TYPESequence(CC043C)
    }

  implicit lazy val arbitraryCORRELATION_IDENTIFIERSequence: Arbitrary[CORRELATION_IDENTIFIERSequence] =
    Arbitrary {
      for {
        correlationIdentifier <- Gen.option(Gen.alphaNumStr)
      } yield CORRELATION_IDENTIFIERSequence(
        correlationIdentifier = correlationIdentifier
      )
    }

  implicit lazy val arbitraryTransitOperationType14: Arbitrary[TransitOperationType14] =
    Arbitrary {
      for {
        mrn                       <- Gen.alphaNumStr
        declarationType           <- Gen.option(Gen.alphaNumStr)
        declarationAcceptanceDate <- Gen.option(arbitrary[XMLGregorianCalendar])
        security                  <- Gen.alphaNumStr
        reducedDatasetIndicator   <- arbitrary[Flag]
      } yield TransitOperationType14(
        MRN = mrn,
        declarationType = declarationType,
        declarationAcceptanceDate = declarationAcceptanceDate,
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfDestinationActualType03: Arbitrary[CustomsOfficeOfDestinationActualType03] =
    Arbitrary {
      for {
        referenceNumber <- Gen.alphaNumStr
      } yield CustomsOfficeOfDestinationActualType03(
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType06: Arbitrary[HolderOfTheTransitProcedureType06] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(Gen.alphaNumStr)
        tirHolderIdentificationNumber <- Gen.option(Gen.alphaNumStr)
        name                          <- Gen.alphaNumStr
        address                       <- arbitrary[AddressType10]
      } yield HolderOfTheTransitProcedureType06(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryTraderAtDestinationType03: Arbitrary[TraderAtDestinationType03] =
    Arbitrary {
      for {
        identificationNumber <- Gen.alphaNumStr
      } yield TraderAtDestinationType03(
        identificationNumber = identificationNumber
      )
    }

  implicit lazy val arbitraryCTLControlType: Arbitrary[CTLControlType] =
    Arbitrary {
      for {
        continueUnloading <- positiveBigInts
      } yield CTLControlType(
        continueUnloading = continueUnloading
      )
    }

  implicit lazy val arbitraryConsignmentType05: Arbitrary[ConsignmentType05] =
    Arbitrary {
      for {
        countryOfDestination  <- Gen.option(Gen.alphaNumStr)
        containerIndicator    <- arbitrary[Flag]
        inlandModeOfTransport <- Gen.option(Gen.alphaNumStr)
        grossMass             <- Gen.option(positiveBigDecimals)
        consignor             <- Gen.option(arbitrary[ConsignorType05])
        consignee             <- Gen.option(arbitrary[ConsigneeType04])
        transportEquipment    <- arbitrary[Seq[TransportEquipmentType05]]
      } yield ConsignmentType05(
        countryOfDestination = countryOfDestination,
        containerIndicator = containerIndicator,
        inlandModeOfTransport = inlandModeOfTransport,
        grossMass = grossMass,
        Consignor = consignor,
        Consignee = consignee,
        TransportEquipment = transportEquipment,
        DepartureTransportMeans = Nil,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil,
        Incident = Nil,
        HouseConsignment = Nil
      )
    }

  implicit lazy val arbitraryHouseConsignmentType04: Arbitrary[HouseConsignmentType04] =
    Arbitrary {
      for {
        sequenceNumber          <- Gen.alphaNumStr
        grossMass               <- positiveBigDecimals
        consignor               <- Gen.option(arbitrary[ConsignorType06])
        consignee               <- Gen.option(arbitrary[ConsigneeType04])
        departureTransportMeans <- arbitrary[Seq[DepartureTransportMeansType02]]
        consignmentItems        <- arbitrary[Seq[ConsignmentItemType04]]
      } yield HouseConsignmentType04(
        sequenceNumber = sequenceNumber,
        grossMass = grossMass,
        securityIndicatorFromExportDeclaration = None,
        Consignor = consignor,
        Consignee = consignee,
        DepartureTransportMeans = departureTransportMeans,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil,
        ConsignmentItem = consignmentItems
      )
    }

  implicit lazy val arbitraryConsignorType05: Arbitrary[ConsignorType05] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(Gen.alphaNumStr)
        name                 <- Gen.option(Gen.alphaNumStr)
        address              <- Gen.option(arbitrary[AddressType07])
      } yield ConsignorType05(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryConsigneeType04: Arbitrary[ConsigneeType04] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(Gen.alphaNumStr)
        name                 <- Gen.option(Gen.alphaNumStr)
        address              <- Gen.option(arbitrary[AddressType07])
      } yield ConsigneeType04(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryConsignorType06: Arbitrary[ConsignorType06] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(Gen.alphaNumStr)
        name                 <- Gen.option(Gen.alphaNumStr)
        address              <- Gen.option(arbitrary[AddressType07])
      } yield ConsignorType06(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryDepartureTransportMeansType02: Arbitrary[DepartureTransportMeansType02] =
    Arbitrary {
      for {
        sequenceNumber       <- Gen.alphaNumStr
        typeOfIdentification <- Gen.alphaNumStr
        identificationNumber <- Gen.alphaNumStr
        nationality          <- Gen.alphaNumStr
      } yield DepartureTransportMeansType02(
        sequenceNumber = sequenceNumber,
        typeOfIdentification = typeOfIdentification,
        identificationNumber = identificationNumber,
        nationality = nationality
      )
    }

  implicit lazy val arbitraryTransportEquipmentType05: Arbitrary[TransportEquipmentType05] =
    Arbitrary {
      for {
        sequenceNumber                <- Gen.alphaNumStr
        containerIdentificationNumber <- Gen.option(Gen.alphaNumStr)
        seals                         <- arbitrary[Seq[SealType04]]
        goodsReferences               <- arbitrary[Seq[GoodsReferenceType02]]
      } yield TransportEquipmentType05(
        sequenceNumber = sequenceNumber,
        containerIdentificationNumber = containerIdentificationNumber,
        numberOfSeals = seals.length,
        Seal = seals,
        GoodsReference = goodsReferences
      )
    }

  implicit lazy val arbitraryAdditionalReferenceType02: Arbitrary[AdditionalReferenceType02] =
    Arbitrary {
      for {
        sequenceNumber <- Gen.alphaNumStr
        typeVal        <- Gen.alphaNumStr
        refNum         <- Gen.option(Gen.alphaNumStr)
      } yield AdditionalReferenceType02(
        sequenceNumber = sequenceNumber,
        typeValue = typeVal,
        referenceNumber = refNum
      )
    }

  implicit lazy val arbitraryAdditionalReferenceType03: Arbitrary[AdditionalReferenceType03] =
    Arbitrary {
      for {
        sequenceNumber <- Gen.alphaNumStr
        typeVal        <- Gen.alphaNumStr
        refNum         <- Gen.option(Gen.alphaNumStr)
      } yield AdditionalReferenceType03(
        sequenceNumber = sequenceNumber,
        typeValue = typeVal,
        referenceNumber = refNum
      )
    }

  implicit lazy val arbitrarySealType04: Arbitrary[SealType04] =
    Arbitrary {
      for {
        sequenceNumber <- Gen.alphaNumStr
        identifier     <- Gen.alphaNumStr
      } yield SealType04(
        sequenceNumber = sequenceNumber,
        identifier = identifier
      )
    }

  implicit lazy val arbitraryGoodsReferenceType02: Arbitrary[GoodsReferenceType02] =
    Arbitrary {
      for {
        sequenceNumber             <- Gen.alphaNumStr
        declarationGoodsItemNumber <- positiveBigInts
      } yield GoodsReferenceType02(
        sequenceNumber = sequenceNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber
      )
    }

  implicit lazy val arbitraryCommodityType08: Arbitrary[CommodityType08] =
    Arbitrary {
      for {
        descriptionOfGoods <- Gen.alphaNumStr
        cusCode            <- Gen.option(Gen.alphaNumStr)
        commodityCode      <- Gen.option(arbitrary[CommodityCodeType05])
        dangerousGoods     <- arbitrary[Seq[DangerousGoodsType01]]
        goodsMeasure       <- Gen.option(arbitrary[GoodsMeasureType03])
      } yield CommodityType08(
        descriptionOfGoods = descriptionOfGoods,
        cusCode = cusCode,
        CommodityCode = commodityCode,
        DangerousGoods = dangerousGoods,
        GoodsMeasure = goodsMeasure
      )
    }

  implicit lazy val arbitraryCommodityCodeType05: Arbitrary[CommodityCodeType05] =
    Arbitrary {
      for {
        harmonizedSystemSubHeadingCode <- Gen.alphaNumStr
        combinedNomenclatureCode       <- Gen.option(Gen.alphaNumStr)
      } yield CommodityCodeType05(
        harmonizedSystemSubHeadingCode = harmonizedSystemSubHeadingCode,
        combinedNomenclatureCode = combinedNomenclatureCode
      )
    }

  implicit lazy val arbitraryDangerousGoodsType01: Arbitrary[DangerousGoodsType01] =
    Arbitrary {
      for {
        sequenceNumber <- Gen.alphaNumStr
        unNumber       <- Gen.alphaNumStr
      } yield DangerousGoodsType01(
        sequenceNumber = sequenceNumber,
        UNNumber = unNumber
      )
    }

  implicit lazy val arbitraryGoodsMeasureType03: Arbitrary[GoodsMeasureType03] =
    Arbitrary {
      for {
        grossMass <- positiveBigDecimals
        netMass   <- Gen.option(positiveBigDecimals)
      } yield GoodsMeasureType03(
        grossMass = grossMass,
        netMass = netMass
      )
    }

  implicit lazy val arbitraryConsignmentItemType04: Arbitrary[ConsignmentItemType04] =
    Arbitrary {
      for {
        goodsItemNumber            <- Gen.alphaNumStr
        declarationGoodsItemNumber <- positiveBigInts
        commodity                  <- arbitrary[CommodityType08]
      } yield ConsignmentItemType04(
        goodsItemNumber = goodsItemNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber,
        declarationType = None,
        countryOfDestination = None,
        Consignee = None,
        Commodity = commodity,
        Packaging = Nil,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil
      )
    }

  implicit lazy val arbitraryAddressType07: Arbitrary[AddressType07] =
    Arbitrary {
      for {
        streetAndNumber <- Gen.alphaNumStr
        postcode        <- Gen.option(Gen.alphaNumStr)
        city            <- Gen.alphaNumStr
        country         <- Gen.alphaNumStr
      } yield AddressType07(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType10: Arbitrary[AddressType10] =
    Arbitrary {
      for {
        streetAndNumber <- Gen.alphaNumStr
        postcode        <- Gen.option(Gen.alphaNumStr)
        city            <- Gen.alphaNumStr
        country         <- Gen.alphaNumStr
      } yield AddressType10(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitrarySupportingDocumentType02: Arbitrary[SupportingDocumentType02] =
    Arbitrary {
      for {
        sequenceNumber          <- Gen.alphaNumStr
        typeValue               <- Gen.alphaNumStr
        referenceNumber         <- Gen.alphaNumStr
        complementOfInformation <- Gen.option(Gen.alphaNumStr)
      } yield SupportingDocumentType02(
        sequenceNumber = sequenceNumber,
        typeValue = typeValue,
        referenceNumber = referenceNumber,
        complementOfInformation = complementOfInformation
      )
    }

  implicit lazy val arbitraryTransportDocumentType02: Arbitrary[TransportDocumentType02] =
    Arbitrary {
      for {
        sequenceNumber  <- Gen.alphaNumStr
        typeValue       <- Gen.alphaNumStr
        referenceNumber <- Gen.alphaNumStr
      } yield TransportDocumentType02(
        sequenceNumber = sequenceNumber,
        typeValue = typeValue,
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryXMLGregorianCalendar: Arbitrary[XMLGregorianCalendar] =
    Arbitrary {
      Gen.const(XMLCalendar("2022-07-15"))
    }

  implicit lazy val arbitraryFlag: Arbitrary[Flag] =
    Arbitrary {
      Gen.oneOf(Number0, Number1)
    }
}
