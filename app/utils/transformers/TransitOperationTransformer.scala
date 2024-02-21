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
import generated.TransitOperationType14
import models.{DeclarationType, UserAnswers}
import pages.{DeclarationTypePage, SecurityTypePage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransitOperationTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) extends PageTransformer {

  def transform(transitOperation: TransitOperationType14)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers =>
    transitOperation match {
      case to =>
        referenceDataConnector.getSecurityType(to.security).flatMap {
          secType =>
            to.declarationType
              .map(
                dec => referenceDataConnector.getDeclarationType(dec).map(Some(_))
              )
              .getOrElse(Future.successful(None))
              .flatMap {
                optDecType =>
                  lazy val pipeline: UserAnswers => Future[UserAnswers] =
                    set(SecurityTypePage, secType) andThen
                      set(DeclarationTypePage, optDecType)
                  pipeline(userAnswers)
              }
        }

      case _ =>
        Future.successful(userAnswers)
    }
}
