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

package utils

import models.Index
import play.api.libs.json.{JsArray, JsObject, JsValue, Reads}

package object answersHelpers {

  implicit class RichJsArray(arr: JsArray) {

    def mapWithIndex[T](f: (JsValue, Index, Index) => T): Seq[T] =
      arr.value.zipWithIndex
        .flatMap {
          case (value, i) =>
            value match {
              case JsObject(underlying) =>
                underlying.keys.toSeq match {
                  case "sequenceNumber" :: Nil => None
                  case _                       => Some((value, i))
                }
              case _ => Some((value, i))
            }
        }
        .zipWithIndex
        .map {
          case ((value, index), displayIndex) => f(value, Index(index), Index(displayIndex))
        }
        .toSeq

    def zipWithIndex: List[(JsValue, Index)] = arr.value.toList.zipWithIndex.map(
      x => (x._1, Index(x._2))
    )

    def isEmpty: Boolean = arr.value.isEmpty
  }

  implicit class RichOptionalJsArray(arr: Option[JsArray]) {

    def mapWithIndex[T](f: (JsValue, Index, Index) => T): Seq[T] =
      arr.map(_.mapWithIndex(f)).getOrElse(Nil)

    def validate[T](implicit rds: Reads[T]): Option[T] =
      arr.flatMap(_.validate[T].asOpt)

    def length: Int = arr.getOrElse(JsArray()).value.length

  }
}
