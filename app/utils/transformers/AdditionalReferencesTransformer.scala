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
import pages.houseConsignment.index.items.additionalReference.{
  AdditionalReferenceTypePage => AdditionalReferenceTypeItemPage,
  AdditionalReferenceNumberPage => AdditionalReferenceNumberItemPage
}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferencesTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) extends PageTransformer {

  private case class TempAdditionalReference[T](
    underlying: T,
    typeValue: AdditionalReferenceType
  )

  def transform(
    additionalReferences: Seq[AdditionalReferenceType03]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.sections.additionalReference.AdditionalReferenceSection

    genericTransform(additionalReferences)(_.typeValue) {
      case (TempAdditionalReference(underlying, typeValue), index) =>
        setSequenceNumber(AdditionalReferenceSection(index), underlying.sequenceNumber) andThen
          set(AdditionalReferenceTypePage(index), typeValue) andThen
          set(AdditionalReferenceNumberPage(index), underlying.referenceNumber)
    }
  }

  def transform(
    additionalReferences: Seq[AdditionalReferenceType02],
    hcIndex: Index,
    itemIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferenceSection

    genericTransform(additionalReferences)(_.typeValue) {
      case (TempAdditionalReference(underlying, typeValue), index) =>
        setSequenceNumber(AdditionalReferenceSection(hcIndex, itemIndex, index), underlying.sequenceNumber) andThen
          set(AdditionalReferenceTypeItemPage(hcIndex, itemIndex, index), typeValue) andThen
          set(AdditionalReferenceNumberItemPage(hcIndex, itemIndex, index), underlying.referenceNumber)
    }
  }

  private def genericTransform[T](
    additionalReferences: Seq[T]
  )(
    lookup: T => String
  )(
    pipeline: (TempAdditionalReference[T], Index) => UserAnswers => Future[UserAnswers]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {
    lazy val referenceDataLookups = additionalReferences.map {
      additionalReference =>
        referenceDataConnector
          .getAdditionalReferenceType(lookup(additionalReference))
          .map(TempAdditionalReference(additionalReference, _))
    }

    Future.sequence(referenceDataLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (additionalReference, i)) =>
          acc.flatMap(pipeline(additionalReference, Index(i)))
      })
    }
  }
}
