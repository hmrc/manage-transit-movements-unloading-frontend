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

import cats.data.NonEmptySet
import connectors.ReferenceDataConnector.*
import models.SelectableList

package object services {

  implicit class RichResponses[T](value: Responses[T]) {

    def resolve(): NonEmptySet[T] =
      value match {
        case Right(value) => value
        case Left(ex)     => throw ex
      }
  }

  implicit class RichResponse[T](value: Response[T]) {

    def resolve(): T =
      value match {
        case Right(value) => value
        case Left(ex)     => throw ex
      }

    def isDefined: Boolean =
      value match {
        case Right(value)                           => true
        case Left(_: NoReferenceDataFoundException) => false
        case Left(ex)                               => throw ex
      }
  }

  implicit class RichSelectables[T <: models.reference.Selectable](value: NonEmptySet[T]) {

    def toSelectableList: SelectableList[T] =
      SelectableList(value.toSeq)
  }

  implicit class RichNonEmptySet[T](value: NonEmptySet[T]) {

    def toSeq: Seq[T] =
      value.toNonEmptyList.toList
  }
}
