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
import models.reference.{AdditionalInformationCode, AdditionalReferenceType}
import models.{Index, UserAnswers}
import pages.additionalInformation.{AdditionalInformationCodePage, AdditionalInformationTextPage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalInformationTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) extends PageTransformer {

  private case class TempAdditionalInformation(
    code: AdditionalInformationCode,
    text: Option[String]
  )
  private case class TempAdditionalInformationHC(code: AdditionalInformationCode)

  def transform(additionalInformation: Seq[AdditionalInformationType02])(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {

    lazy val referenceDataLookups = additionalInformation.map {
      additionalInformation =>
        referenceDataConnector.getAdditionalInformationCode(additionalInformation.code).map {
          additionalInformationCode =>
            TempAdditionalInformation(
              code = additionalInformationCode,
              text = additionalInformation.text
            )
        }
    }

    Future.sequence(referenceDataLookups).flatMap {
      _.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (TempAdditionalInformation(code, text), i)) =>
          acc.flatMap {
            userAnswers =>
              val informationIndex = Index(i)
              val pipeline: UserAnswers => Future[UserAnswers] =
                set(AdditionalInformationCodePage(informationIndex), code) andThen
                  set(AdditionalInformationTextPage(informationIndex), text)

              pipeline(userAnswers)
          }
      })
    }

  }
}
