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

package utils.transformers

import generated.ConsignorType06
import models.{Index, UserAnswers}
import pages.{ConsignorIdentifierPage, ConsignorNamePage}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConsignorTransformer @Inject() (implicit ec: ExecutionContext) extends PageTransformer {

  def transform(consignor: Option[ConsignorType06], hcIndex: Index): UserAnswers => Future[UserAnswers] = userAnswers =>
    consignor match {
      case Some(ConsignorType06(identificationNumber, name, _)) =>
        val pipeline: UserAnswers => Future[UserAnswers] =
          set(ConsignorIdentifierPage(hcIndex), identificationNumber) andThen
            set(ConsignorNamePage(hcIndex), name)

        pipeline(userAnswers)
      case None =>
        Future.successful(userAnswers)
    }
}
