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

package viewModels.houseConsignment.index.items.document

import config.FrontendAppConfig
import models.{HouseConsignmentLevelDocuments, Index, Mode}
import play.api.i18n.Messages
import viewModels.ModeViewModelProvider
import viewModels.houseConsignment.index.items.document.TypeViewModel.typePrefix

import javax.inject.Inject

case class TypeViewModel(heading: String, title: String, requiredError: String, documents: HouseConsignmentLevelDocuments) {
  def maxLimitLabelForType(implicit config: FrontendAppConfig, messages: Messages): Option[String] = Documents.maxLimitLabelForType(documents, typePrefix)
}

object TypeViewModel {

  val typePrefix = "houseConsignment.index.items.document.type"

  class TypeViewModelProvider @Inject() extends ModeViewModelProvider {

    override val prefix = typePrefix

    def apply(mode: Mode, documents: HouseConsignmentLevelDocuments, houseConsignmentIndex: Index, itemIndex: Index)(implicit
      messages: Messages
    ): TypeViewModel =
      new TypeViewModel(heading(mode, houseConsignmentIndex, itemIndex),
                        title(mode, houseConsignmentIndex, itemIndex),
                        requiredError(mode, houseConsignmentIndex, itemIndex),
                        documents
      )
  }
}
