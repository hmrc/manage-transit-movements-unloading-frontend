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
import generated.{AdditionalReferenceType02, AdditionalReferenceType03}
import models.reference.AdditionalReferenceType
import models.{Index, UserAnswers}
import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
import pages.houseConsignment.index.items.additionalReference.{AdditionalReferenceNumberPage => AdditionalReferenceNumberItemPage, AdditionalReferencePage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferenceTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) extends PageTransformer {

  private case class TempAdditionalReference(
    typeValue: AdditionalReferenceType,
    referenceNumber: Option[String]
  )
  private case class TempAdditionalReferenceHC(typeValue: AdditionalReferenceType, referenceNumber: Option[String])

  def transform(additionalReferences: Seq[AdditionalReferenceType03])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {

    lazy val referenceDataLookups = additionalReferences.map {
      additionalReference =>
        referenceDataConnector.getAdditionalReferenceType(additionalReference.typeValue).map {
          additionalReferenceType =>
            TempAdditionalReference(
              typeValue = additionalReferenceType,
              referenceNumber = additionalReference.referenceNumber
            )
        }
    }

    Future.sequence(referenceDataLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (TempAdditionalReference(typeValue, referenceNumber), i)) =>
          acc.flatMap {
            userAnswers =>
              val dtmIndex = Index(i)
              val pipeline: UserAnswers => Future[UserAnswers] =
                set(AdditionalReferenceTypePage(dtmIndex), typeValue) andThen
                  set(AdditionalReferenceNumberPage(dtmIndex), referenceNumber)

              pipeline(userAnswers)
          }
      })
    }

  }

  def transform(additionalReferences: Seq[AdditionalReferenceType02], hcIndex: Index, itemIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers => {

    lazy val referenceDataLookups = additionalReferences.map {
      additionalReference =>
        referenceDataConnector.getAdditionalReferenceType(additionalReference.typeValue).map {
          additionalReferenceType =>
            TempAdditionalReferenceHC(
              typeValue = additionalReferenceType,
              additionalReference.referenceNumber
            )
        }
    }

    Future.sequence(referenceDataLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (TempAdditionalReferenceHC(additionalReference, referenceNumber), i)) =>
          acc.flatMap {
            userAnswers =>
              val sequence = Index(i)
              val pipeline: UserAnswers => Future[UserAnswers] =
                set(AdditionalReferencePage(hcIndex, itemIndex, sequence), additionalReference) andThen
                  set(AdditionalReferenceNumberItemPage(hcIndex, itemIndex, sequence), referenceNumber)

              pipeline(userAnswers)
          }
      })
    }
  }
}
