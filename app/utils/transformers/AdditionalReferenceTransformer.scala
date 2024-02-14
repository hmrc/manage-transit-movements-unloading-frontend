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

import connectors.ReferenceDataConnector
import generated.AdditionalReferenceType03
import models.reference.AdditionalReference
import models.{Index, UserAnswers}
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) extends PageTransformer {

  def transform(additionalReferences: Seq[AdditionalReferenceType03])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {
    additionalReferences.zipWithIndex.foldLeft(Future.successful(userAnswers))(
      {
        case (accumulator, (reference, i)) =>
          accumulator.flatMap {
            userAnswers =>
              referenceDataConnector.getAdditionalReferenceType(reference.typeValue).flatMap {
                refDataValue =>
                  val pipeline: UserAnswers => Future[UserAnswers] =
                    set(AdditionalReferenceTypePage(Index(i)), AdditionalReference(refDataValue.documentType, refDataValue.description)) andThen
                      set(AdditionalReferenceNumberPage(Index(i)), reference.referenceNumber)

                  pipeline(userAnswers)

              }

          }

      }
    )

  }
}
