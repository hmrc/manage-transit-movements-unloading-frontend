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

package pages.houseConsignment.index.items.additionalinformation

import models.reference.AdditionalInformationType
import pages.behaviours.PageBehaviours

class HouseConsignmentAdditionalInformationCodePageSpec extends PageBehaviours {

  "HouseConsignmentAdditionalInformationCodePage" - {

    beRetrievable[AdditionalInformationType](HouseConsignmentAdditionalInformationCodePage(index, index, index))

    beSettable[AdditionalInformationType](HouseConsignmentAdditionalInformationCodePage(index, index, index))

    beRemovable[AdditionalInformationType](HouseConsignmentAdditionalInformationCodePage(index, index, index))
  }
}
