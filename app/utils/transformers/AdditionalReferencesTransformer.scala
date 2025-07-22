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

import generated.{AdditionalReferenceType01, AdditionalReferenceType02}
import models.{Index, UserAnswers}
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalReferencesTransformer @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(
    additionalReferences: Seq[AdditionalReferenceType02]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.additionalReference.{AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
    import pages.sections.additionalReference.AdditionalReferenceSection

    additionalReferences.mapWithSets {
      (value, index) =>
        setSequenceNumber(AdditionalReferenceSection(index), value.sequenceNumber) andThen
          set(AdditionalReferenceTypePage(index), value.typeValue, referenceDataService.getAdditionalReference) andThen
          set(AdditionalReferenceNumberPage(index), value.referenceNumber)
    }
  }

  def transform(
    additionalReferences: Seq[AdditionalReferenceType02],
    hcIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.additionalReference.*
    import pages.sections.houseConsignment.index.additionalReference.AdditionalReferenceSection

    additionalReferences.mapWithSets {
      (value, index) =>
        setSequenceNumber(AdditionalReferenceSection(hcIndex, index), value.sequenceNumber) andThen
          set(HouseConsignmentAdditionalReferenceTypePage(hcIndex, index), value.typeValue, referenceDataService.getAdditionalReference) andThen
          set(HouseConsignmentAdditionalReferenceNumberPage(hcIndex, index), value.referenceNumber)
    }
  }

  def transform(
    additionalReferences: Seq[AdditionalReferenceType01],
    hcIndex: Index,
    itemIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.items.additionalReference.{AdditionalReferenceInCL234Page, AdditionalReferenceNumberPage, AdditionalReferenceTypePage}
    import pages.sections.houseConsignment.index.items.additionalReference.AdditionalReferenceSection

    additionalReferences.mapWithSets {
      (value, index) =>
        setSequenceNumber(AdditionalReferenceSection(hcIndex, itemIndex, index), value.sequenceNumber) andThen
          set(AdditionalReferenceTypePage(hcIndex, itemIndex, index), value.typeValue, referenceDataService.getAdditionalReference) andThen
          set(AdditionalReferenceInCL234Page(hcIndex, itemIndex, index), value.typeValue, referenceDataService.isDocumentTypeExcise) andThen
          set(AdditionalReferenceNumberPage(hcIndex, itemIndex, index), value.referenceNumber)
    }
  }
}
