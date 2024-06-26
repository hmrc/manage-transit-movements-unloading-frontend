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

package viewModels.documents

import config.FrontendAppConfig
import models.{ConsignmentLevelDocuments, HouseConsignmentLevelDocuments, Index}
import models.DocType.{Support, Transport}
import play.api.i18n.Messages

object Documents {

  def maxLimitLabelForType(
    documents: ConsignmentLevelDocuments,
    prefix: String
  )(implicit config: FrontendAppConfig, messages: Messages): Option[String] =
    if (documents.supporting >= config.maxSupportingDocumentsConsignment) {
      Some(messages(s"$prefix.maxLimitForType.label", Support.display.toLowerCase, Transport.display.toLowerCase))
    } else if (documents.transport >= config.maxTransportDocumentsConsignment) {
      Some(messages(s"$prefix.maxLimitForType.label", Transport.display.toLowerCase, Support.display.toLowerCase))
    } else {
      None
    }

  def maxLimitLabelForType(
    documents: HouseConsignmentLevelDocuments,
    prefix: String
  )(implicit config: FrontendAppConfig, messages: Messages): Option[String] =
    if (documents.supporting >= config.maxSupportingDocumentsHouseConsignment) {
      Some(messages(s"$prefix.maxLimitForType.label", Support.display.toLowerCase, Transport.display.toLowerCase))
    } else if (documents.transport >= config.maxTransportDocumentsHouseConsignment) {
      Some(messages(s"$prefix.maxLimitForType.label", Transport.display.toLowerCase, Support.display.toLowerCase))
    } else {
      None
    }

  def maxLimitLabelForType(
    documents: HouseConsignmentLevelDocuments,
    itemIndex: Index,
    houseConsignmentIndex: Index,
    prefix: String
  )(implicit config: FrontendAppConfig, messages: Messages): Option[String] =
    if (documents.supporting >= config.maxSupportingDocumentsHouseConsignment) {
      Some(messages(s"$prefix.maxLimitForType.label", Support.display.toLowerCase, Transport.display.toLowerCase, houseConsignmentIndex, itemIndex))
    } else if (documents.transport >= config.maxTransportDocumentsHouseConsignment) {
      Some(messages(s"$prefix.maxLimitForType.label", Transport.display.toLowerCase, Support.display.toLowerCase, houseConsignmentIndex, itemIndex))
    } else {
      None
    }

  def maxLimitLabelForType(
    documents: HouseConsignmentLevelDocuments,
    houseConsignmentIndex: Index,
    prefix: String
  )(implicit config: FrontendAppConfig, messages: Messages): Option[String] =
    if (documents.supporting >= config.maxSupportingDocumentsHouseConsignment) {
      Some(messages(s"$prefix.maxLimitForType.label", Support.display.toLowerCase, Transport.display.toLowerCase, houseConsignmentIndex))
    } else if (documents.transport >= config.maxTransportDocumentsHouseConsignment) {
      Some(messages(s"$prefix.maxLimitForType.label", Transport.display.toLowerCase, Support.display.toLowerCase, houseConsignmentIndex))
    } else {
      None
    }

}
