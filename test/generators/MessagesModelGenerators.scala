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
import models.ErrorType.GenericError
import models.messages._
import models.{
  DefaultPointer,
  ErrorPointer,
  ErrorType,
  FunctionalError,
  GrossWeightPointer,
  MovementReferenceNumber,
  NumberOfItemsPointer,
  NumberOfPackagesPointer,
  Seals,
  TraderAtDestination,
  UnloadingDatePointer,
  UnloadingPermission,
  UnloadingRemarksRejectionMessage,
  VehicleRegistrationPointer
}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.{choose, const}
import org.scalacheck.{Arbitrary, Gen}
import scalaxb.XMLCalendar
import utils.Format
import utils.Format.dateFormatted

import java.time.{LocalDate, LocalTime}
import javax.xml.datatype.XMLGregorianCalendar

trait MessagesModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryInterchangeControlReference: Arbitrary[InterchangeControlReference] =
    Arbitrary {
      for {
        date  <- localDateGen
        index <- Gen.posNum[Int]
      } yield InterchangeControlReference(dateFormatted(date), index)
    }

  implicit lazy val genericErrorType: Arbitrary[GenericError] =
    Arbitrary {
      Gen.oneOf(ErrorType.genericValues)
    }

  implicit lazy val arbitraryErrorType: Arbitrary[ErrorType] =
    Arbitrary {
      for {
        genericError <- arbitrary[GenericError]
        errorType    <- Gen.oneOf(Seq(genericError))
      } yield errorType
    }

  implicit lazy val arbitraryRejectionError: Arbitrary[FunctionalError] =
    Arbitrary {

      for {
        errorType     <- arbitrary[ErrorType]
        pointer       <- Gen.oneOf(ErrorPointer.values)
        reason        <- arbitrary[Option[String]]
        originalValue <- Gen.option(stringsWithMaxLength(6))
        date          <- arbitrary[LocalDate]
        int           <- arbitrary[Int]
      } yield {
        val value: Option[String] = pointer match {
          case UnloadingDatePointer                           => Some(date.format(Format.dateFormatter))
          case NumberOfItemsPointer | NumberOfPackagesPointer => Some(int.toString)
          case _                                              => originalValue
        }
        FunctionalError(errorType, pointer, reason, value)
      }
    }

  implicit lazy val arbitraryRejectionErrorNonDefaultPointer: Arbitrary[FunctionalError] =
    Arbitrary {

      for {
        errorType     <- arbitrary[ErrorType]
        pointer       <- arbitraryNonDefaultErrorPointer.arbitrary
        reason        <- arbitrary[Option[String]]
        originalValue <- stringsWithMaxLength(6)
        date          <- arbitrary[LocalDate]
        int           <- arbitrary[Int]
      } yield {
        val value: Option[String] = pointer match {
          case UnloadingDatePointer                           => Some(date.format(Format.dateFormatter))
          case NumberOfItemsPointer | NumberOfPackagesPointer => Some(int.toString)
          case _                                              => Some(originalValue)
        }
        FunctionalError(errorType, pointer, reason, value)
      }
    }

  implicit lazy val arbitraryNonDefaultErrorPointer: Arbitrary[ErrorPointer] =
    Arbitrary {
      Gen.oneOf(Seq(GrossWeightPointer, NumberOfItemsPointer, UnloadingDatePointer, VehicleRegistrationPointer, NumberOfPackagesPointer))
    }

  implicit lazy val arbitraryDefaultPointer: Arbitrary[ErrorPointer] =
    Arbitrary {
      for {
        str <- arbitrary[String]
      } yield DefaultPointer(str)
    }

  implicit lazy val arbitraryMeta: Arbitrary[Meta] =
    Arbitrary {
      for {
        interchangeControlReference <- arbitrary[InterchangeControlReference]
        date                        <- arbitrary[LocalDate]
        time                        <- arbitrary[LocalTime]
      } yield Meta(
        interchangeControlReference,
        date,
        LocalTime.of(time.getHour, time.getMinute),
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None
      )
    }

  implicit lazy val arbitraryHeader: Arbitrary[Header] =
    Arbitrary {
      for {
        movementReferenceNumber <- arbitrary[MovementReferenceNumber]
        transportIdentity       <- Gen.option(stringsWithMaxLength(UnloadingPermission.transportIdentityLength))
        transportCountry        <- Gen.option(Gen.pick(UnloadingPermission.transportCountryLength, 'A' to 'Z'))
        numberOfItems           <- choose(min = 1: Int, 2: Int)
        numberOfPackages        <- Gen.option(choose(min = 1: Int, 2: Int))
        GrossWeight             <- Gen.choose(0.0, 99999999.999).map(BigDecimal(_).bigDecimal.setScale(3, BigDecimal.RoundingMode.DOWN))
      } yield Header(movementReferenceNumber.toString,
                     transportIdentity,
                     transportCountry.map(_.mkString),
                     numberOfItems,
                     numberOfPackages,
                     GrossWeight.toString
      )
    }

  implicit lazy val arbitraryUnloadingRemarksRequest: Arbitrary[UnloadingRemarksRequest] =
    Arbitrary {
      for {
        meta               <- arbitrary[Meta]
        header             <- arbitrary[Header]
        traderDestination  <- arbitrary[TraderAtDestination]
        presentationOffice <- Gen.pick(UnloadingRemarksRequest.presentationOfficeLength, 'A' to 'Z')
        remarks            <- Gen.oneOf(arbitrary[RemarksConform], arbitrary[RemarksConformWithSeals], arbitrary[RemarksNonConform])
        resultOfControl    <- listWithMaxLength[ResultsOfControl](RemarksNonConform.resultsOfControlLength)
        seals              <- Gen.option(arbitrary[Seals])
      } yield UnloadingRemarksRequest(meta, header, traderDestination, presentationOffice.mkString, remarks, resultOfControl, seals)
    }

  implicit lazy val arbitraryUnloadingRemarksRejectionMessage: Arbitrary[UnloadingRemarksRejectionMessage] =
    Arbitrary {

      for {
        mrn    <- arbitrary[MovementReferenceNumber]
        date   <- arbitrary[LocalDate]
        action <- arbitrary[Option[String]]
        errors <- listWithMaxLength[FunctionalError](5)(arbitraryRejectionError)
      } yield UnloadingRemarksRejectionMessage(mrn, date, action, errors)
    }

  // --------------------

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
        continueUnloading <- arbitrary[BigInt]
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
        grossMass             <- Gen.option(arbitrary[BigDecimal])
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
        declarationGoodsItemNumber <- arbitrary[BigInt]
      } yield GoodsReferenceType02(
        sequenceNumber = sequenceNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber
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

  implicit lazy val arbitraryXMLGregorianCalendar: Arbitrary[XMLGregorianCalendar] =
    Arbitrary {
      Gen.const(XMLCalendar("2022-07-15"))
    }

  implicit lazy val arbitraryFlag: Arbitrary[Flag] =
    Arbitrary {
      Gen.oneOf(Number0, Number1)
    }
}
