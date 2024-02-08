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

import generated.TransportDocumentType02
import models.{Index, UserAnswers}
import pages.documents.DocumentReferenceNumberPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TransportDocumentTransformer @Inject() ()(implicit ec: ExecutionContext) extends PageTransformer {

  def transform(transportDocuments: Seq[TransportDocumentType02]): UserAnswers => Future[UserAnswers] = userAnswers =>
    transportDocuments.zipWithIndex
      .foldLeft(Future.successful(userAnswers))({
        case (acc, (transportDocument, i)) =>
          acc.flatMap {
            userAnswers =>
              val docIndex = Index(i)
              val pipeline =
                set(DocumentReferenceNumberPage(docIndex), transportDocument.referenceNumber)

              pipeline(userAnswers)
          }
      })
}
