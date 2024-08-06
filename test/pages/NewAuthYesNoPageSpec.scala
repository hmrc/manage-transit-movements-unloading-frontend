/*
 * Copyright 2023 HM Revenue & Customs
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

package pages

import pages.behaviours.PageBehaviours

class NewAuthYesNoPageSpec extends PageBehaviours {

  "NewAuthYesNoPage" - {

    beRetrievable[Boolean](NewAuthYesNoPage)

    beSettable[Boolean](NewAuthYesNoPage)

    beRemovable[Boolean](NewAuthYesNoPage)

    "cleanup" - {
      "must remove answers to pages when newAuth is answered no" in {
        val userAnswers = emptyUserAnswers
          .setValue(NewAuthYesNoPage, true)
          .setValue(GoodsTooLargeForContainerYesNoPage, true)
          .setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, true)
          .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
          .setValue(OtherThingsToReportPage, "otherThings")

        val result = userAnswers.setValue(NewAuthYesNoPage, false)

        result.get(GoodsTooLargeForContainerYesNoPage) must not be defined
        result.get(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage) must not be defined
        result.get(SealsReplacedByCustomsAuthorityYesNoPage) must not be defined
        result.get(OtherThingsToReportPage) must not be defined
      }
    }
  }
}
