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

package utils.answersHelpers

import base.SpecBase
import controllers.routes
import models.CheckMode
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Value}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Key, SummaryListRow}
import utils.Format.cyaDateFormatter

import java.time.LocalDate

class CheckYourAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks {

  "CheckYourAnswersHelper" - {

    "newAuth" - {
      "must return row" - {
        "when answered Yes" in {

          val answers = emptyUserAnswers.setValue(NewAuthYesNoPage, true)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.newProcedure

          result mustBe Some(
            SummaryListRow(
              key = Key("Do you want to use the revised unloading procedure?".toText),
              value = Value("Yes".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.NewAuthYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if you want to use the revised unloading procedure"),
                      attributes = Map("id" -> "change-new-auth-yes-no")
                    )
                  )
                )
              )
            )
          )
        }
        "when answered No" in {

          val answers = emptyUserAnswers.setValue(NewAuthYesNoPage, false)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.newProcedure

          result mustBe Some(
            SummaryListRow(
              key = Key("Do you want to use the revised unloading procedure?".toText),
              value = Value("No".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.NewAuthYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if you want to use the revised unloading procedure"),
                      attributes = Map("id" -> "change-new-auth-yes-no")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "goodsTooLarge" - {
      "must return row" - {
        "when answered Yes" in {

          val answers = emptyUserAnswers.setValue(GoodsTooLargeForContainerYesNoPage, true)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.goodsTooLarge

          result mustBe Some(
            SummaryListRow(
              key = Key("Are the goods too large to fit into a container?".toText),
              value = Value("Yes".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.GoodsTooLargeForContainerYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if the goods are too large to fit into a container"),
                      attributes = Map("id" -> "change-goods-too-large-yes-no")
                    )
                  )
                )
              )
            )
          )
        }
        "when answered No" in {

          val answers = emptyUserAnswers.setValue(GoodsTooLargeForContainerYesNoPage, false)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.goodsTooLarge

          result mustBe Some(
            SummaryListRow(
              key = Key("Are the goods too large to fit into a container?".toText),
              value = Value("No".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.GoodsTooLargeForContainerYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if the goods are too large to fit into a container"),
                      attributes = Map("id" -> "change-goods-too-large-yes-no")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "largeUnsealedGoodsRecordDiscrepanciesYesNo" - {
      "must return row" - {
        "when answered No" in {

          val answers = emptyUserAnswers.setValue(LargeUnsealedGoodsRecordDiscrepanciesYesNoPage, false)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.largeUnsealedGoodsRecordDiscrepanciesYesNo

          result mustBe Some(
            SummaryListRow(
              key = Key("Were there any discrepancies between the transit movement and unloading permission?".toText),
              value = Value("No".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.LargeUnsealedGoodsRecordDiscrepanciesYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if there were any discrepancies between the transit movement and unloading permission"),
                      attributes = Map("id" -> "change-add-large-unsealed-goods-record-discrepancies")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "sealsReplaced" - {
      "must return row" - {
        "when answered Yes" in {

          val answers = emptyUserAnswers.setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.sealsReplaced

          result mustBe Some(
            SummaryListRow(
              key = Key("Has the external seal been replaced by a customs authority?".toText),
              value = Value("Yes".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.SealsReplacedByCustomsAuthorityYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if the external seal has been replaced by a customs authority"),
                      attributes = Map("id" -> "change-seals-replaced-customs-yes-no")
                    )
                  )
                )
              )
            )
          )
        }
        "when answered No" in {

          val answers = emptyUserAnswers.setValue(SealsReplacedByCustomsAuthorityYesNoPage, false)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.sealsReplaced

          result mustBe Some(
            SummaryListRow(
              key = Key("Has the external seal been replaced by a customs authority?".toText),
              value = Value("No".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.SealsReplacedByCustomsAuthorityYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if the external seal has been replaced by a customs authority"),
                      attributes = Map("id" -> "change-seals-replaced-customs-yes-no")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "goodsUnloadedDate" - {
      "must return row" in {

        forAll(arbitrary[LocalDate]) {
          localDate =>
            val answers = emptyUserAnswers.setValue(DateGoodsUnloadedPage, localDate)
            val helper  = new CheckYourAnswersHelper(answers)
            val result  = helper.goodsUnloadedDate

            result mustBe Some(
              SummaryListRow(
                key = Key("Goods’ unloaded date".toText),
                value = Value(localDate.format(cyaDateFormatter).toText),
                actions = Some(
                  Actions(
                    items = List(
                      ActionItem(
                        content = "Change".toText,
                        href = routes.DateGoodsUnloadedController.onPageLoad(arrivalId, CheckMode).url,
                        visuallyHiddenText = Some("goods’ unloaded date"),
                        attributes = Map("id" -> "change-goods-unloaded-date")
                      )
                    )
                  )
                )
              )
            )
        }
      }
    }

    "anySealsBroken" - {
      "must return row" - {
        "when answered Yes" in {

          val answers = emptyUserAnswers.setValue(AreAnySealsBrokenPage, true)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.anySealsBroken

          result mustBe Some(
            SummaryListRow(
              key = Key("Are any of the seals broken?".toText),
              value = Value("Yes".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.AreAnySealsBrokenController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if any of the seals are broken"),
                      attributes = Map("id" -> "change-any-seals-broken")
                    )
                  )
                )
              )
            )
          )
        }
        "when answered No" in {

          val answers = emptyUserAnswers.setValue(AreAnySealsBrokenPage, false)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.anySealsBroken

          result mustBe Some(
            SummaryListRow(
              key = Key("Are any of the seals broken?".toText),
              value = Value("No".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.AreAnySealsBrokenController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if any of the seals are broken"),
                      attributes = Map("id" -> "change-any-seals-broken")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "canSealsBeRead" - {
      "must return row" - {
        "when answered Yes" in {

          val answers = emptyUserAnswers.setValue(CanSealsBeReadPage, true)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.canSealsBeRead

          result mustBe Some(
            SummaryListRow(
              key = Key("Are all of the seal identification numbers or marks readable?".toText),
              value = Value("Yes".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.CanSealsBeReadController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if all of the seal identification numbers or marks are readable"),
                      attributes = Map("id" -> "change-can-seals-be-read")
                    )
                  )
                )
              )
            )
          )
        }
        "when answered No" in {

          val answers = emptyUserAnswers.setValue(CanSealsBeReadPage, false)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.canSealsBeRead

          result mustBe Some(
            SummaryListRow(
              key = Key("Are all of the seal identification numbers or marks readable?".toText),
              value = Value("No".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.CanSealsBeReadController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if all of the seal identification numbers or marks are readable"),
                      attributes = Map("id" -> "change-can-seals-be-read")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "unloadingCommentsYesNo" - {
      "must return row" - {
        "when answered Yes" in {

          val answers = emptyUserAnswers.setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.addDiscrepanciesYesNo

          result mustBe Some(
            SummaryListRow(
              key = Key("Were there any discrepancies between the transit movement and unloading permission?".toText),
              value = Value("Yes".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some(
                        "if there were any discrepancies between the transit movement and unloading permission"
                      ),
                      attributes = Map("id" -> "change-add-discrepancies")
                    )
                  )
                )
              )
            )
          )
        }
        "when answered No" in {

          val answers = emptyUserAnswers.setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.addDiscrepanciesYesNo

          result mustBe Some(
            SummaryListRow(
              key = Key("Were there any discrepancies between the transit movement and unloading permission?".toText),
              value = Value("No".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if there were any discrepancies between the transit movement and unloading permission"),
                      attributes = Map("id" -> "change-add-discrepancies")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "addCommentsYesNo" - {
      "must return row" - {
        "when answered Yes" in {

          val answers = emptyUserAnswers.setValue(AddCommentsYesNoPage, true)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.addCommentsYesNo

          result mustBe Some(
            SummaryListRow(
              key = Key("Do you want to add any comments?".toText),
              value = Value("Yes".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.AddCommentsYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some(
                        "if you want to add any comments"
                      ),
                      attributes = Map("id" -> "change-add-comments")
                    )
                  )
                )
              )
            )
          )
        }

        "when answered No" in {

          val answers = emptyUserAnswers.setValue(AddCommentsYesNoPage, false)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.addCommentsYesNo

          result mustBe Some(
            SummaryListRow(
              key = Key("Do you want to add any comments?".toText),
              value = Value("No".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.AddCommentsYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if you want to add any comments"),
                      attributes = Map("id" -> "change-add-comments")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "additionalComment" - {
      "must return row" in {

        forAll(Gen.alphaNumStr) {
          comments =>
            val answers = emptyUserAnswers.setValue(UnloadingCommentsPage, comments)
            val helper  = new CheckYourAnswersHelper(answers)
            val result  = helper.additionalComment

            result mustBe Some(
              SummaryListRow(
                key = Key("Comments".toText),
                value = Value(s"$comments".toText),
                actions = Some(
                  Actions(
                    items = List(
                      ActionItem(
                        content = "Change".toText,
                        href = routes.UnloadingCommentsController.onPageLoad(arrivalId, CheckMode).url,
                        visuallyHiddenText = Some("comments"),
                        attributes = Map("id" -> "change-comment")
                      )
                    )
                  )
                )
              )
            )
        }
      }
    }

    "addReportYesNo" - {
      "must return row" - {
        "when answered Yes" in {

          val answers = emptyUserAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, true)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.addReportYesNo

          result mustBe Some(
            SummaryListRow(
              key = Key("Do you have anything else to report?".toText),
              value = Value("Yes".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some(
                        "if you have anything to report"
                      ),
                      attributes = Map("id" -> "change-add-report")
                    )
                  )
                )
              )
            )
          )
        }

        "when answered No" in {

          val answers = emptyUserAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, false)
          val helper  = new CheckYourAnswersHelper(answers)
          val result  = helper.addReportYesNo

          result mustBe Some(
            SummaryListRow(
              key = Key("Do you have anything else to report?".toText),
              value = Value("No".toText),
              actions = Some(
                Actions(
                  items = List(
                    ActionItem(
                      content = "Change".toText,
                      href = routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(arrivalId, CheckMode).url,
                      visuallyHiddenText = Some("if you have anything to report"),
                      attributes = Map("id" -> "change-add-report")
                    )
                  )
                )
              )
            )
          )
        }
      }
    }

    "report" - {
      "must return row" - {
        "when not a revised procedure" in {

          forAll(Gen.alphaNumStr) {
            report =>
              val answers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, false)
                .setValue(OtherThingsToReportPage, report)

              val helper = new CheckYourAnswersHelper(answers)
              val result = helper.report

              result mustBe Some(
                SummaryListRow(
                  key = Key("What do you want to report?".toText),
                  value = Value(s"$report".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.OtherThingsToReportController.onPageLoad(arrivalId, CheckMode).url,
                          visuallyHiddenText = Some("what you want to report"),
                          attributes = Map("id" -> "change-report")
                        )
                      )
                    )
                  )
                )
              )
          }
        }

        "when a revised procedure" - {
          "and seal replaced" in {

            forAll(Gen.alphaNumStr) {
              report =>
                val answers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, true)
                  .setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
                  .setValue(OtherThingsToReportPage, report)

                val helper = new CheckYourAnswersHelper(answers)
                val result = helper.report

                result mustBe Some(
                  SummaryListRow(
                    key = Key("Identification number for replacement external seal".toText),
                    value = Value(s"$report".toText),
                    actions = Some(
                      Actions(
                        items = List(
                          ActionItem(
                            content = "Change".toText,
                            href = routes.OtherThingsToReportController.onPageLoad(arrivalId, CheckMode).url,
                            visuallyHiddenText = Some("identification number for replacement external seal"),
                            attributes = Map("id" -> "change-report")
                          )
                        )
                      )
                    )
                  )
                )
            }
          }

          "and seal not replaced" in {

            forAll(Gen.alphaNumStr) {
              report =>
                val answers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, true)
                  .setValue(SealsReplacedByCustomsAuthorityYesNoPage, false)
                  .setValue(OtherThingsToReportPage, report)

                val helper = new CheckYourAnswersHelper(answers)
                val result = helper.report

                result mustBe Some(
                  SummaryListRow(
                    key = Key("Identification number for external seal".toText),
                    value = Value(s"$report".toText),
                    actions = Some(
                      Actions(
                        items = List(
                          ActionItem(
                            content = "Change".toText,
                            href = routes.OtherThingsToReportController.onPageLoad(arrivalId, CheckMode).url,
                            visuallyHiddenText = Some("identification number for external seal"),
                            attributes = Map("id" -> "change-report")
                          )
                        )
                      )
                    )
                  )
                )
            }
          }
        }
      }
    }
  }
}
