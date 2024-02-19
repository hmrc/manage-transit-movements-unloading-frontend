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

package utils

import base.SpecBase
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.grossMass.GrossMassPage
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

class ConsignmentAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private def grossMassAction = Some(
    Actions(
      "",
      List(
        ActionItem(
          "#",
          Text("Change"),
          Some("unloadingFindings.gross.mass.heading.change.hidden"),
          "",
          Map("id" -> "change-gross-mass")
        )
      )
    )
  )

  "grossWeight" - {

    "must return None" - {
      s"when no transport equipments defined" in {
        val helper = new ConsignmentAnswersHelper(emptyUserAnswers)
        val result = helper.grossMass
        result.isEmpty mustBe true
      }
    }

    "must return Some(Row)" - {
      s"when $GrossMassPage is defined" in {
        val answers = emptyUserAnswers
          .setValue(GrossMassPage, "999.99")

        val helper = new ConsignmentAnswersHelper(answers)
        val result = helper.grossMass.head

        result mustBe
          SummaryListRow(
            key = Key("Gross weight".toText),
            value = Value("999.99".toText),
            actions = grossMassAction
          )
      }
    }
  }
}
