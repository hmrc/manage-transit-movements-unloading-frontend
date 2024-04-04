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
import generated.AdditionalInformationType02
import models.reference.AdditionalInformationCode
import models.{Index, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) extends PageTransformer {

  private case class TempAdditionalInformation(
    underlying: AdditionalInformationType02,
    code: AdditionalInformationCode
  )

  def transform(
    additionalInformation: Seq[AdditionalInformationType02]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.additionalInformation._
    import pages.sections.additionalInformation.AdditionalInformationSection

    genericTransform(additionalInformation) {
      case (TempAdditionalInformation(underlying, code), index) =>
        setSequenceNumber(AdditionalInformationSection(index), underlying.sequenceNumber) andThen
          set(AdditionalInformationCodePage(index), code) andThen
          set(AdditionalInformationTextPage(index), underlying.text)
    }
  }

  def transform(
    additionalReferences: Seq[AdditionalInformationType02],
    hcIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.additionalinformation._
    import pages.sections.houseConsignment.index.additionalInformation.AdditionalInformationSection

    genericTransform(additionalReferences) {
      case (TempAdditionalInformation(underlying, code), index) =>
        setSequenceNumber(AdditionalInformationSection(hcIndex, index), underlying.sequenceNumber) andThen
          set(HouseConsignmentAdditionalInformationCodePage(hcIndex, index), code) andThen
          set(HouseConsignmentAdditionalInformationTextPage(hcIndex, index), underlying.text)
    }
  }

  def transform(
    additionalReferences: Seq[AdditionalInformationType02],
    hcIndex: Index,
    itemIndex: Index
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = {
    import pages.houseConsignment.index.items.additionalinformation._
    import pages.sections.houseConsignment.index.items.additionalInformation.AdditionalInformationSection

    genericTransform(additionalReferences) {
      case (TempAdditionalInformation(underlying, code), index) =>
        setSequenceNumber(AdditionalInformationSection(hcIndex, itemIndex, index), underlying.sequenceNumber) andThen
          set(HouseConsignmentItemAdditionalInformationCodePage(hcIndex, itemIndex, index), code) andThen
          set(HouseConsignmentItemAdditionalInformationTextPage(hcIndex, itemIndex, index), underlying.text)
    }
  }

  private def genericTransform(
    additionalInformation: Seq[AdditionalInformationType02]
  )(
    pipeline: (TempAdditionalInformation, Index) => UserAnswers => Future[UserAnswers]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {
    lazy val referenceDataLookups = additionalInformation.map {
      additionalInformation =>
        referenceDataConnector
          .getAdditionalInformationCode(additionalInformation.code)
          .map(TempAdditionalInformation(additionalInformation, _))
    }

    Future.sequence(referenceDataLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (additionalInformation, i)) =>
          acc.flatMap(pipeline(additionalInformation, Index(i)))
      })
    }
  }
}
