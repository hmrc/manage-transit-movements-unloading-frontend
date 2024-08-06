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

class GoodsTooLargeForContainerYesNoPageSpec extends PageBehaviours {

  "GoodsTooLargeForContainerYesNoPage" - {

    beRetrievable[Boolean](GoodsTooLargeForContainerYesNoPage)

    beSettable[Boolean](GoodsTooLargeForContainerYesNoPage)

    beRemovable[Boolean](GoodsTooLargeForContainerYesNoPage)

  }

  "cleanup" - {
    "must remove answer to LargeUnsealedGoodsRecordDiscrepanciesYesNoPage when answered 'no'" in {
      val userAnswers = emptyUserAnswers
        .setValue(GoodsTooLargeForContainerYesNoPage, true)
        .setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, true)

      val result = userAnswers.setValue(GoodsTooLargeForContainerYesNoPage, false)

      result.get(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage) must not be defined
    }

    "must remove answer to pages when answered 'yes'" in {
      val userAnswers = emptyUserAnswers
        .setValue(GoodsTooLargeForContainerYesNoPage, false)
        .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
        .setValue(OtherThingsToReportPage, "other")

      val result = userAnswers.setValue(GoodsTooLargeForContainerYesNoPage, true)

      result.get(SealsReplacedByCustomsAuthorityYesNoPage) must not be defined
      result.get(OtherThingsToReportPage) must not be defined
    }
  }
}
