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
import generated.{SupportingDocumentType02, TransportDocumentType02}
import models.{Document, Index, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DocumentTransformer @Inject() (
  referenceDataConnector: ReferenceDataConnector
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(
    supportingDocuments: Seq[SupportingDocumentType02],
    transportDocuments: Seq[TransportDocumentType02]
  )(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] = userAnswers => {
    lazy val supportingDocumentsWithDescription = supportingDocuments.map {
      supportingDocument =>
        referenceDataConnector.getSupportingDocument(supportingDocument.typeValue).map {
          Document.apply(supportingDocument, _)
        }
    }

    lazy val transportDocumentsWithDescription = transportDocuments.map {
      transportDocument =>
        referenceDataConnector.getTransportDocument(transportDocument.typeValue).map {
          Document.apply(transportDocument, _)
        }
    }

    for {
      sd <- Future.sequence(supportingDocumentsWithDescription)
      td <- Future.sequence(transportDocumentsWithDescription)
      documents = sd ++ td
      userAnswers <- documents.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (acc, (document, i)) =>
          acc.flatMap {
            userAnswers =>
              val documentIndex: Index = Index(i)
              val pipeline: UserAnswers => Future[UserAnswers] = document match {
                case Document.SupportingDocument(documentType, referenceNumber, complementOfInformation) =>
                  ???
                case Document.TransportDocument(documentType, referenceNumber) =>
                  ???
              }

              pipeline(userAnswers)
          }
      })
    } yield userAnswers
  }
}