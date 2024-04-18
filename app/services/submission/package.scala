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

package services

import generated.{Flag, Number0, Number1}
import models.{Index, UnloadingType}
import play.api.libs.json._
import scalaxb.XMLCalendar
import utils.transformers.SequenceNumber

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import javax.xml.datatype.XMLGregorianCalendar
import scala.annotation.tailrec
import scala.language.implicitConversions

package object submission {

  implicit class RichJsPath(value: JsPath) {

    def readArray[T](implicit reads: (Index, BigInt) => Reads[Option[T]]): Reads[Seq[T]] =
      value
        .readWithDefault(JsArray())
        .map {
          jsArray =>
            @tailrec
            def rec(values: List[(JsValue, Int)], acc: Seq[T] = Nil, sequenceNumber: BigInt = 1): Seq[T] =
              values match {
                case Nil =>
                  acc
                case (jsValue, index) :: tail =>
                  val updatedSequenceNumber = jsValue
                    .validate[Option[BigInt]]((__ \ SequenceNumber).readNullable[BigInt])
                    .asOpt
                    .flatten
                    .getOrElse(sequenceNumber)

                  val updatedAcc = jsValue
                    .validate[Option[T]](reads(Index(index), updatedSequenceNumber))
                    .asOpt
                    .flatten
                    .fold(acc)(acc :+ _)

                  rec(tail, updatedAcc, updatedSequenceNumber + 1)
              }
            rec(jsArray.value.zipWithIndex.toList)
        }

    def readNullableSafe[T](implicit reads: Reads[T]): Reads[Option[T]] =
      value.readNullable[T] orElse None
  }

  implicit def boolToFlag(x: Boolean): Flag =
    if (x) Number1 else Number0

  implicit def unloadingTypeToFlag(x: UnloadingType): Flag =
    x match {
      case UnloadingType.Fully     => Number1
      case UnloadingType.Partially => Number0
    }

  implicit def localDateToXMLGregorianCalendar(date: Option[LocalDate]): Option[XMLGregorianCalendar] =
    date.map(localDateToXMLGregorianCalendar)

  implicit def localDateToXMLGregorianCalendar(date: LocalDate): XMLGregorianCalendar =
    stringToXMLGregorianCalendar(date.toString)

  implicit def stringToXMLGregorianCalendar(date: Option[String]): Option[XMLGregorianCalendar] =
    date.map(stringToXMLGregorianCalendar)

  implicit def stringToXMLGregorianCalendar(date: String): XMLGregorianCalendar =
    XMLCalendar(date.replace("Z", ""))

  implicit def localDateTimeToXMLGregorianCalendar(localDateTime: LocalDateTime): XMLGregorianCalendar = {
    val formatterNoMillis: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    localDateTime.format(formatterNoMillis)
  }

  implicit def successfulReads[T](value: T): Reads[T] = Reads {
    _ => JsSuccess(value)
  }
}
