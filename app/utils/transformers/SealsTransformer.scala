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

import generated.SealType04
import models.{Index, UserAnswers}
import pages.sections.SealSection
import pages.transportEquipment.index.seals.SealIdentificationNumberPage

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SealsTransformer @Inject() (implicit ec: ExecutionContext) extends PageTransformer {

  def transform(seals: Seq[SealType04], equipmentIndex: Index): UserAnswers => Future[UserAnswers] = userAnswers =>
    seals.zipWithIndex.foldLeft(Future.successful(userAnswers))({
      case (acc, (SealType04(sequenceNumber, identifier), i)) =>
        acc.flatMap {
          userAnswers =>
            val sealIndex: Index = Index(i)
            val pipeline: UserAnswers => Future[UserAnswers] =
              setSequenceNumber(SealSection(equipmentIndex, sealIndex), sequenceNumber) andThen
                set(SealIdentificationNumberPage(equipmentIndex, sealIndex), identifier)

            pipeline(userAnswers)
        }
    })
}
