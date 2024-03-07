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
import models.reference.{AdditionalInformationCode}
import models.{Index, UserAnswers}
import pages.additionalInformation.{AdditionalInformationCodePage, AdditionalInformationTextPage}
import pages.houseConsignment.index.items.additionalinformation.{HouseConsignmentAdditionalInformationCodePage, HouseConsignmentAdditionalInformationTextPage}
import pages.sections.additionalInformation.AdditionalInformationSection
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) extends PageTransformer {

  def transform(additionalInformation: Seq[AdditionalInformationType02])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {

    lazy val referenceDataLookups = additionalInformation.map {
      additionalInformation =>
        referenceDataConnector
          .getAdditionalInformationCode(additionalInformation.code)
          .map(TempAdditionalInformation(additionalInformation, _))
    }

    Future.sequence(referenceDataLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (TempAdditionalInformation(underlying, code), i)) =>
          acc.flatMap {
            userAnswers =>
              val informationIndex = Index(i)
              val pipeline: UserAnswers => Future[UserAnswers] = {
                setSequenceNumber(AdditionalInformationSection(informationIndex), underlying.sequenceNumber) andThen
                  set(AdditionalInformationCodePage(informationIndex), code) andThen
                  set(AdditionalInformationTextPage(informationIndex), underlying.text)
              }
              pipeline(userAnswers)
          }
      })
    }
  }

  def transform(additionalReferences: Seq[AdditionalInformationType02], hcIndex: Index, itemIndex: Index)(implicit
    hc: HeaderCarrier
  ): UserAnswers => Future[UserAnswers] = userAnswers => {

    lazy val referenceDataLookups = additionalReferences.map {
      additionalInformation =>
        referenceDataConnector
          .getAdditionalInformationCode(additionalInformation.code)
          .map(TempAdditionalInformation(additionalInformation, _))
    }

    Future.sequence(referenceDataLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (TempAdditionalInformation(underlying, additionalInformation), i)) =>
          acc.flatMap {
            userAnswers =>
              val index = Index(i)
              val pipeline: UserAnswers => Future[UserAnswers] = {
                setSequenceNumber(AdditionalInformationSection(index), underlying.sequenceNumber) andThen
                  set(HouseConsignmentAdditionalInformationCodePage(hcIndex, itemIndex, index), additionalInformation) andThen
                  set(HouseConsignmentAdditionalInformationTextPage(hcIndex, itemIndex, index), underlying.text)
              }
              pipeline(userAnswers)
          }
      })
    }
  }

  private case class TempAdditionalInformation[T](
    underlying: T,
    code: AdditionalInformationCode
  )

}
