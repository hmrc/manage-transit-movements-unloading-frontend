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

package generators

import models.ErrorType.GenericError
import models.messages._
import models.{
  ErrorPointer,
  ErrorType,
  FunctionalError,
  GrossMassPointer,
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
import org.scalacheck.Gen.choose
import org.scalacheck.{Arbitrary, Gen}
import utils.Format
import utils.Format.dateFormatted

import java.time.{LocalDate, LocalTime}

trait MessagesModelGenerators extends Generators {

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
        pointer       <- Gen.oneOf(Seq(GrossMassPointer, NumberOfItemsPointer, UnloadingDatePointer, VehicleRegistrationPointer, NumberOfPackagesPointer))
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
        grossMass               <- Gen.choose(0.0, 99999999.999).map(BigDecimal(_).bigDecimal.setScale(3, BigDecimal.RoundingMode.DOWN))
      } yield Header(movementReferenceNumber.toString, transportIdentity, transportCountry.map(_.mkString), numberOfItems, numberOfPackages, grossMass.toString)
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
}
