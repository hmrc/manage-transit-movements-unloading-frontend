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

package models.requests

import play.api.mvc.{Request, WrappedRequest}
import models.{EoriNumber, UserAnswers}

case class OptionalDataRequest[A](
  request: Request[A],
  eoriNumber: EoriNumber,
  userAnswers: Option[UserAnswers]
) extends WrappedRequest[A](request)

trait MandatoryDataRequest[A] {
  val request: Request[A]
  val eoriNumber: EoriNumber
  val userAnswers: UserAnswers
}

case class DataRequest[A](
  request: Request[A],
  eoriNumber: EoriNumber,
  userAnswers: UserAnswers
) extends WrappedRequest[A](request)
    with MandatoryDataRequest[A]

class SpecificDataRequestProvider1[T1] {

  case class SpecificDataRequest[A](
    request: Request[A],
    eoriNumber: EoriNumber,
    userAnswers: UserAnswers,
    arg: T1
  ) extends WrappedRequest[A](request)
      with MandatoryDataRequest[A]
}

class SpecificDataRequestProvider2[T1, T2] {

  case class SpecificDataRequest[A](
    request: Request[A],
    eoriNumber: EoriNumber,
    userAnswers: UserAnswers,
    arg: (T1, T2)
  ) extends WrappedRequest[A](request)
}
