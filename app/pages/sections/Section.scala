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

package pages.sections

import models.Index
import pages.QuestionPage
import play.api.libs.json._
import services.submission.RichJsPath
import utils.transformers.SequenceNumber

import scala.annotation.tailrec

trait Section[T <: JsValue] extends QuestionPage[T] {

  def readArray[A](implicit reads: (Index, BigInt) => Reads[Option[A]]): Reads[Seq[A]] =
    path.last
      .readWithDefault(JsArray())
      .map {
        jsArray =>
          @tailrec
          def rec(values: List[(JsValue, Int)], acc: Seq[A] = Nil, sequenceNumber: BigInt = 1): Seq[A] =
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
                  .validate[Option[A]](reads(Index(index), updatedSequenceNumber))
                  .asOpt
                  .flatten
                  .fold(acc)(acc :+ _)

                rec(tail, updatedAcc, updatedSequenceNumber + 1)
            }

          rec(jsArray.value.zipWithIndex.toList)
      }

  def count(f: JsValue => Boolean): Reads[BigInt] =
    path.last
      .readWithDefault(JsArray())
      .map(_.value.count(f))
}
