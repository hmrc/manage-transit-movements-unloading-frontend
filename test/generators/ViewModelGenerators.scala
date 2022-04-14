/*
 * Copyright 2022 HM Revenue & Customs
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

package generators

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewModels.sections.SummarySection

trait ViewModelGenerators {
  self: Generators =>

  private val maxSeqLength = 10

  implicit lazy val arbitraryText: Arbitrary[Text] = Arbitrary {
    for {
      content <- Gen.alphaNumStr
    } yield content.toText
  }

  implicit lazy val arbitraryContent: Arbitrary[Content] = Arbitrary {
    arbitrary[Text]
  }

  implicit lazy val arbitraryKey: Arbitrary[Key] = Arbitrary {
    for {
      content <- arbitrary[Content]
      classes <- Gen.alphaNumStr
    } yield Key(content, classes)
  }

  implicit lazy val arbitraryValue: Arbitrary[Value] = Arbitrary {
    for {
      content <- arbitrary[Content]
      classes <- Gen.alphaNumStr
    } yield Value(content, classes)
  }

  implicit lazy val arbitraryActionItem: Arbitrary[ActionItem] = Arbitrary {
    for {
      content            <- arbitrary[Content]
      href               <- Gen.alphaNumStr
      visuallyHiddenText <- Gen.option(Gen.alphaNumStr)
      classes            <- Gen.alphaNumStr
      attributes         <- Gen.const(Map.empty[String, String]) // TODO: Do we need to have valid attributes generated here? Use case?
    } yield ActionItem(href, content, visuallyHiddenText, classes, attributes)
  }

  implicit lazy val arbitraryActions: Arbitrary[Actions] = Arbitrary {
    for {
      length <- Gen.choose(1, maxSeqLength)
      items  <- Gen.containerOfN[Seq, ActionItem](length, arbitrary[ActionItem])
    } yield Actions(items = items)
  }

  implicit lazy val arbitrarySummaryListRow: Arbitrary[SummaryListRow] = Arbitrary {
    for {
      key     <- arbitrary[Key]
      value   <- arbitrary[Value]
      classes <- Gen.alphaNumStr
      actions <- arbitrary[Option[Actions]]
    } yield SummaryListRow(key, value, classes, actions)
  }

  implicit lazy val arbitrarySection: Arbitrary[SummarySection] = Arbitrary {
    for {
      sectionTitle <- Gen.alphaNumStr
      length       <- Gen.choose(1, maxSeqLength)
      rows         <- Gen.containerOfN[Seq, SummaryListRow](length, arbitrary[SummaryListRow])
    } yield SummarySection(sectionTitle, rows)
  }
}
