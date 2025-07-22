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

import generated.AdditionalInformationType02
import models.{Index, UserAnswers}
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationTransformer @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(
    additionalInformation: Seq[AdditionalInformationType02]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.additionalInformation.*
    import pages.sections.additionalInformation.AdditionalInformationSection

    additionalInformation.forEachDoSets {
      (value, index) =>
        setSequenceNumber(AdditionalInformationSection(index), value.sequenceNumber) andThen
          set(AdditionalInformationCodePage(index), value.code, referenceDataService.getAdditionalInformationCode) andThen
          set(AdditionalInformationTextPage(index), value.text)
    }
  }

  def transform(
    additionalInformation: Seq[AdditionalInformationType02],
    hcIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.additionalinformation.*
    import pages.sections.houseConsignment.index.additionalInformation.AdditionalInformationSection

    additionalInformation.forEachDoSets {
      (value, index) =>
        setSequenceNumber(AdditionalInformationSection(hcIndex, index), value.sequenceNumber) andThen
          set(HouseConsignmentAdditionalInformationCodePage(hcIndex, index), value.code, referenceDataService.getAdditionalInformationCode) andThen
          set(HouseConsignmentAdditionalInformationTextPage(hcIndex, index), value.text)
    }
  }

  def transform(
    additionalInformation: Seq[AdditionalInformationType02],
    hcIndex: Index,
    itemIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.items.additionalinformation.*
    import pages.sections.houseConsignment.index.items.additionalInformation.AdditionalInformationSection

    additionalInformation.forEachDoSets {
      (value, index) =>
        setSequenceNumber(AdditionalInformationSection(hcIndex, itemIndex, index), value.sequenceNumber) andThen
          set(
            HouseConsignmentItemAdditionalInformationCodePage(hcIndex, itemIndex, index),
            value.code,
            referenceDataService.getAdditionalInformationCode
          ) andThen
          set(HouseConsignmentItemAdditionalInformationTextPage(hcIndex, itemIndex, index), value.text)
    }
  }
}
