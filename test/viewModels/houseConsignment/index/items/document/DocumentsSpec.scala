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

import base.SpecBase
import models.HouseConsignmentLevelDocuments

class DocumentsSpec extends SpecBase {

  "Documents" - {

    "must return message if max limit reached" in {
      Documents.maxLimitLabelForType(HouseConsignmentLevelDocuments(0, frontendAppConfig.maxTransportDocumentsHouseConsignment), "prefix").isDefined mustBe true
      Documents
        .maxLimitLabelForType(HouseConsignmentLevelDocuments(frontendAppConfig.maxSupportingDocumentsHouseConsignment, 0), "prefix")
        .isDefined mustBe true
    }

    "must return None if max limit not reached" in {
      Documents.maxLimitLabelForType(HouseConsignmentLevelDocuments(0, 0), "prefix") mustBe None
    }
  }
}
