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

package navigation

import base.SpecBase
import controllers.routes
import generated.*
import generators.Generators
import models.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.*

class NavigationSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new Navigation

  "Navigator" - {

    "in Normal mode" - {

      val mode = NormalMode

      "must go from GoodsTooLargeForContainerYesNoPage to UnloadingGuidancePage" in {

        val userAnswers = emptyUserAnswers.setValue(GoodsTooLargeForContainerYesNoPage, true)
        navigator
          .nextPage(GoodsTooLargeForContainerYesNoPage, mode, userAnswers)
          .mustBe(routes.UnloadingGuidanceController.onPageLoad(userAnswers.id))
      }

      "must go from NewAuthYesNoPage" - {
        "to RevisedUnloadingProcedureConditionsYesNoPage when answer is Yes" in {

          val userAnswers = emptyUserAnswers.setValue(NewAuthYesNoPage, true)
          navigator
            .nextPage(NewAuthYesNoPage, mode, userAnswers)
            .mustBe(routes.RevisedUnloadingProcedureConditionsYesNoController.onPageLoad(userAnswers.id, mode))
        }

        "to UnloadingGuidancePage when the answer is No" in {

          val userAnswers = emptyUserAnswers.setValue(NewAuthYesNoPage, false)
          navigator
            .nextPage(NewAuthYesNoPage, mode, userAnswers)
            .mustBe(routes.UnloadingGuidanceController.onPageLoad(userAnswers.id))
        }
      }

      "must go from SealsReplacedByCustomsAuthorityYesNoPage to OtherThingsToReportPage" - {
        "when answered true" in {

          val userAnswers = emptyUserAnswers.setValue(SealsReplacedByCustomsAuthorityYesNoPage, true)
          navigator
            .nextPage(SealsReplacedByCustomsAuthorityYesNoPage, mode, userAnswers)
            .mustBe(routes.OtherThingsToReportController.onPageLoad(userAnswers.id, mode))
        }
        "when answered false" in {

          val userAnswers = emptyUserAnswers.setValue(SealsReplacedByCustomsAuthorityYesNoPage, false)
          navigator
            .nextPage(SealsReplacedByCustomsAuthorityYesNoPage, mode, userAnswers)
            .mustBe(routes.OtherThingsToReportController.onPageLoad(userAnswers.id, mode))
        }
      }

      "must go from Unloading type page to unloading date page" in {

        val userAnswers = emptyUserAnswers.setValue(UnloadingTypePage, UnloadingType.Fully)
        navigator
          .nextPage(UnloadingTypePage, mode, userAnswers)
          .mustBe(routes.DateGoodsUnloadedController.onPageLoad(userAnswers.id, NormalMode))
      }

      "must go from date goods unloaded page" - {
        "to can seals be read page when seals exist in transport equipment" in {
          val transportEquipment = arbitrary[TransportEquipmentType05].retryUntil(_.Seal.nonEmpty).sample.value

          val consignment = arbitrary[CUSTOM_ConsignmentType05].sample.value.copy(
            TransportEquipment = Seq(transportEquipment),
            Incident = Nil
          )

          val ie043 = arbitrary[CC043CType].sample.value.copy(Consignment = Some(consignment))

          val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

          navigator
            .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
            .mustBe(routes.CanSealsBeReadController.onPageLoad(userAnswers.id, mode))
        }

        "to can seals be read page when seals exist in incident transport equipment" in {
          val transportEquipment = arbitrary[TransportEquipmentType07].retryUntil(_.Seal.nonEmpty).sample.value

          val incident = arbitrary[IncidentType04].sample.value.copy(TransportEquipment = Seq(transportEquipment))

          val consignment = arbitrary[CUSTOM_ConsignmentType05].sample.value.copy(
            TransportEquipment = Nil,
            Incident = Seq(incident)
          )

          val ie043 = arbitrary[CC043CType].sample.value.copy(Consignment = Some(consignment))

          val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

          navigator
            .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
            .mustBe(routes.CanSealsBeReadController.onPageLoad(userAnswers.id, mode))
        }

        "to add transit unloading permission discrepancies yes/no page when no seals exist" in {
          val ie043 = arbitrary[CC043CType].map(_.copy(Consignment = None)).sample.value

          val userAnswers = emptyUserAnswers.copy(ie043Data = ie043)

          navigator
            .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
            .mustBe(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(arrivalId, mode))
        }
      }

      "must go from can seals be read page" - {
        "to Are any seals broken page when answer is Yes" in {

          val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, true)
          navigator
            .nextPage(CanSealsBeReadPage, mode, userAnswers)
            .mustBe(routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, mode))
        }

        "to Are any seals broken page when the answer is No" in {

          val userAnswers = emptyUserAnswers.setValue(CanSealsBeReadPage, false)
          navigator
            .nextPage(CanSealsBeReadPage, mode, userAnswers)
            .mustBe(routes.AreAnySealsBrokenController.onPageLoad(userAnswers.id, mode))
        }
      }

      "must go from are any seals broken page " - {
        "to add transit unloading permission discrepancies yes/no page when seals are present and not damaged" in {

          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, false)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(arrivalId, mode))
        }

        "to unloading findings when switched from revised to legacy" in {

          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, true)
            .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
            .setValue(GoodsTooLargeForContainerYesNoPage, true)
            .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, false)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }

        "to unloading findings page when seals are not present" in {

          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }

        "to unloading findings page when seals are present but not readable" in {

          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, false)
            .setValue(AreAnySealsBrokenPage, false)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }

        "to unloading finding page when seals are broken NewAuthYesNoPage is false" in {
          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, true)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(userAnswers.id))
        }

        "to unloading findings page when seals are present but broken" in {

          val userAnswers = emptyUserAnswers
            .setValue(NewAuthYesNoPage, false)
            .setValue(CanSealsBeReadPage, true)
            .setValue(AreAnySealsBrokenPage, true)

          navigator
            .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
            .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
      }

      "must go from add transit unloading permission discrepancies yes/no page page" - {
        "when unrevised procedure" - {
          "and yes selected" - {
            "to unloading comments controller" in {
              val userAnswers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, false)
                .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

              navigator
                .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
                .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
            }
          }

          "and no selected" - {
            "to report yes/no page" in {
              val userAnswers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, false)
                .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

              navigator
                .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
                .mustBe(routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(arrivalId, mode))
            }
          }
        }

        "when procedure is CannotUseRevisedDueToDiscrepancies" - {
          "to CannotUseRevisedUnloadingProcedurePage" in {

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, true)
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
              .setValue(GoodsTooLargeForContainerYesNoPage, true)
              .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
            navigator
              .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
              .mustBe(routes.CannotUseRevisedUnloadingProcedureController.onPageLoad(userAnswers.id))
          }
        }

        "when procedure is RevisedAndGoodsTooLarge" - {
          "to CheckYourAnswersPage" in {

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, true)
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
              .setValue(GoodsTooLargeForContainerYesNoPage, true)
              .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)
            navigator
              .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
          }
        }

        "when no existing answers found" - {
          "to session expired controller" in {
            navigator
              .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, emptyUserAnswers)
              .mustBe(routes.SessionExpiredController.onPageLoad())
          }
        }
      }

      "must go from can unloading comments page to do you have anything else to report yes/no page" in {
        val userAnswers = emptyUserAnswers.setValue(UnloadingCommentsPage, "test")
        navigator
          .nextPage(UnloadingCommentsPage, mode, userAnswers)
          .mustBe(routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(arrivalId, mode))
      }

      "must go from do you have anything else to report page" - {
        "when answer is true to other things to report controller" in {
          val userAnswers = emptyUserAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, true)

          navigator
            .nextPage(DoYouHaveAnythingElseToReportYesNoPage, mode, userAnswers)
            .mustBe(routes.OtherThingsToReportController.onPageLoad(arrivalId, mode))
        }

        "when answer is false to check your answers page" in {
          val userAnswers = emptyUserAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, false)

          navigator
            .nextPage(DoYouHaveAnythingElseToReportYesNoPage, mode, userAnswers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
      }

      "must go from other things to report page to check your answers page" in {
        val userAnswers = emptyUserAnswers.setValue(OtherThingsToReportPage, "test")
        navigator
          .nextPage(OtherThingsToReportPage, mode, userAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))

      }

      "must go from a page that doesn't exist in the route map to technical difficulties" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, mode, answers)
              .mustBe(routes.ErrorController.technicalDifficulties())
        }
      }

      "must go from RevisedUnloadingProcedureConditionsYesNoPage" - {
        "when answer is true" - {
          "to GoodsTooLargeForContainerYesNo page" in {
            val userAnswers = emptyUserAnswers
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)

            navigator
              .nextPage(RevisedUnloadingProcedureConditionsYesNoPage, mode, userAnswers)
              .mustBe(routes.GoodsTooLargeForContainerYesNoController.onPageLoad(userAnswers.id, mode))
          }
        }

        "when answer is false" - {
          "to RevisedUnloadingProcedureUnmetConditions page" in {
            val userAnswers = emptyUserAnswers
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, false)

            navigator
              .nextPage(RevisedUnloadingProcedureConditionsYesNoPage, mode, userAnswers)
              .mustBe(routes.RevisedUnloadingProcedureUnmetConditionsController.onPageLoad(userAnswers.id))
          }
        }
      }
    }

    "in Check mode" - {

      val mode = CheckMode

      "must go from NewAuthYesNoPage" - {
        "when answer is Yes" - {
          "and RevisedUnloadingProcedureConditionsYesNoPage is undefined" - {
            "to revisedUnloadingProcedureConditions page" in {
              val userAnswers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, true)

              navigator
                .nextPage(NewAuthYesNoPage, mode, userAnswers)
                .mustBe(routes.RevisedUnloadingProcedureConditionsYesNoController.onPageLoad(userAnswers.id, mode))
            }
          }

          "and RevisedUnloadingProcedureConditionsYesNoPage is defined" - {
            "to CYA page" in {
              val userAnswers = emptyUserAnswers
                .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
                .setValue(NewAuthYesNoPage, true)

              navigator
                .nextPage(NewAuthYesNoPage, mode, userAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
            }
          }
        }

        "when answer is No" - {
          "and unloading type is answered" - {
            "to CYA" in {
              forAll(arbitrary[UnloadingType]) {
                bool =>
                  val userAnswers = emptyUserAnswers
                    .setValue(UnloadingTypePage, bool)
                    .setValue(NewAuthYesNoPage, false)

                  navigator
                    .nextPage(NewAuthYesNoPage, mode, userAnswers)
                    .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
              }
            }
          }

          "and unloading type is unanswered" - {
            "to unloading guidance" in {
              val userAnswers = emptyUserAnswers
                .setValue(NewAuthYesNoPage, false)

              navigator
                .nextPage(NewAuthYesNoPage, mode, userAnswers)
                .mustBe(routes.UnloadingGuidanceController.onPageLoad(userAnswers.id))
            }
          }
        }
      }

      "must go from RevisedUnloadingProcedureConditionsYesNoPage" - {
        "when answer is Yes" - {
          "and GoodsTooLargeForContainerYesNoPage is undefined" - {
            "to GoodsTooLargeForContainerYesNo page" in {
              val userAnswers = emptyUserAnswers
                .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)

              navigator
                .nextPage(RevisedUnloadingProcedureConditionsYesNoPage, mode, userAnswers)
                .mustBe(routes.GoodsTooLargeForContainerYesNoController.onPageLoad(userAnswers.id, mode))
            }
          }

          "and GoodsTooLargeForContainerYesNoPage is defined" - {
            "to CYA page" in {
              val userAnswers = emptyUserAnswers
                .setValue(GoodsTooLargeForContainerYesNoPage, false)
                .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)

              navigator
                .nextPage(RevisedUnloadingProcedureConditionsYesNoPage, mode, userAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
            }
          }
        }

        "when answer is No" - {
          "and UnloadingTypePage is undefined" - {
            "to RevisedUnloadingProcedureUnmetConditionsController" in {
              val userAnswers = emptyUserAnswers
                .setValue(RevisedUnloadingProcedureConditionsYesNoPage, false)

              navigator
                .nextPage(RevisedUnloadingProcedureConditionsYesNoPage, mode, userAnswers)
                .mustBe(routes.RevisedUnloadingProcedureUnmetConditionsController.onPageLoad(userAnswers.id))
            }
          }

          "and UnloadingTypePage is defined" - {
            "to CYA page" in {
              val userAnswers = emptyUserAnswers
                .setValue(UnloadingTypePage, UnloadingType.Fully)
                .setValue(RevisedUnloadingProcedureConditionsYesNoPage, false)

              navigator
                .nextPage(RevisedUnloadingProcedureConditionsYesNoPage, mode, userAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
            }
          }
        }
      }

      "must go from unloading type page to check your answers" in {
        val userAnswers = emptyUserAnswers.setValue(UnloadingTypePage, UnloadingType.Fully)

        navigator
          .nextPage(UnloadingTypePage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from date goods unloaded page to check your answers" in {
        val userAnswers = emptyUserAnswers.setValue(DateGoodsUnloadedPage, arbitraryLocalDate.arbitrary.sample.value)

        navigator
          .nextPage(DateGoodsUnloadedPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from can seals be read page" - {
        "when state of seals is 1" - {
          "and discrepancies yes/no page is answered" - {
            "to check your answers page" in {
              val userAnswers = emptyUserAnswers
                .setValue(CanSealsBeReadPage, true)
                .setValue(AreAnySealsBrokenPage, false)
                .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

              navigator
                .nextPage(CanSealsBeReadPage, mode, userAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
            }
          }

          "and discrepancies yes/no page is unanswered" - {
            "to discrepancies yes/no page" in {
              val userAnswers = emptyUserAnswers
                .setValue(CanSealsBeReadPage, true)
                .setValue(AreAnySealsBrokenPage, false)

              navigator
                .nextPage(CanSealsBeReadPage, mode, userAnswers)
                .mustBe(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(userAnswers.id, CheckMode))
            }
          }
        }

        "when state of seals is 0" - {
          "and UnloadingCommentsPage is undefined" - {
            "must go to unloading findings page" in {
              val userAnswers = emptyUserAnswers
                .setValue(CanSealsBeReadPage, false)
                .setValue(AreAnySealsBrokenPage, false)

              navigator
                .nextPage(CanSealsBeReadPage, mode, userAnswers)
                .mustBe(routes.UnloadingFindingsController.onPageLoad(userAnswers.id))
            }
          }

          "and UnloadingCommentsPage is defined" - {
            "must go to check your answers page" in {
              val userAnswers = emptyUserAnswers
                .setValue(UnloadingCommentsPage, "foo")
                .setValue(CanSealsBeReadPage, false)
                .setValue(AreAnySealsBrokenPage, false)

              navigator
                .nextPage(CanSealsBeReadPage, mode, userAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
            }
          }
        }
      }

      "must go from are any seals broken page" - {
        "when state of seals is 1" - {
          "and discrepancies yes/no page is answered" - {
            "to check your answers page" in {
              val userAnswers = emptyUserAnswers
                .setValue(CanSealsBeReadPage, true)
                .setValue(AreAnySealsBrokenPage, false)
                .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

              navigator
                .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
            }
          }

          "and discrepancies yes/no page is unanswered" - {
            "to discrepancies yes/no page" in {
              val userAnswers = emptyUserAnswers
                .setValue(CanSealsBeReadPage, true)
                .setValue(AreAnySealsBrokenPage, false)

              navigator
                .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
                .mustBe(routes.AddTransitUnloadingPermissionDiscrepanciesYesNoController.onPageLoad(userAnswers.id, CheckMode))
            }
          }

          "to CYA when switched from revised to legacy" in {

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, true)
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
              .setValue(GoodsTooLargeForContainerYesNoPage, true)
              .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
              .setValue(CanSealsBeReadPage, true)
              .setValue(AreAnySealsBrokenPage, false)

            navigator
              .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(arrivalId))
          }
        }

        "when state of seals is 0" - {
          "and UnloadingCommentsPage is undefined" - {
            "must go to unloading findings page" in {
              val userAnswers = emptyUserAnswers
                .setValue(CanSealsBeReadPage, false)
                .setValue(AreAnySealsBrokenPage, false)

              navigator
                .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
                .mustBe(routes.UnloadingFindingsController.onPageLoad(userAnswers.id))
            }
          }

          "and UnloadingCommentsPage is defined" - {
            "must go to check your answers page" in {
              val userAnswers = emptyUserAnswers
                .setValue(UnloadingCommentsPage, "foo")
                .setValue(CanSealsBeReadPage, false)
                .setValue(AreAnySealsBrokenPage, false)

              navigator
                .nextPage(AreAnySealsBrokenPage, mode, userAnswers)
                .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
            }
          }
        }
      }

      "must go from add transit unloading permission discrepancies yes/no page" - {
        "when unrevised procedure" - {
          "when yes selected" - {
            "and OtherCommentsPage undefined" - {
              "to unloading findings page" in {
                val userAnswers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, false)
                  .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

                navigator
                  .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
                  .mustBe(controllers.routes.UnloadingFindingsController.onPageLoad(userAnswers.id))
              }
            }

            "and OtherCommentsPage defined" - {
              "to CYA page" in {
                val userAnswers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, false)
                  .setValue(UnloadingCommentsPage, "foo")
                  .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)

                navigator
                  .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
                  .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
              }
            }
          }

          "when no is selected" - {
            "and DoYouHaveAnythingElseToReportYesNoPage is undefined" - {
              "to DoYouHaveAnythingElseToReportYesNoPage" in {
                val userAnswers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, false)
                  .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

                navigator
                  .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
                  .mustBe(controllers.routes.DoYouHaveAnythingElseToReportYesNoController.onPageLoad(userAnswers.id, mode))
              }
            }

            "and DoYouHaveAnythingElseToReportYesNoPage is defined" - {
              "to CYA page" in {
                val userAnswers = emptyUserAnswers
                  .setValue(NewAuthYesNoPage, false)
                  .setValue(DoYouHaveAnythingElseToReportYesNoPage, true)
                  .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)

                navigator
                  .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
                  .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
              }
            }
          }
        }

        "when procedure is CannotUseRevisedDueToDiscrepancies" - {
          "to CannotUseRevisedUnloadingProcedurePage" in {

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, true)
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
              .setValue(GoodsTooLargeForContainerYesNoPage, true)
              .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, true)
            navigator
              .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
              .mustBe(routes.CannotUseRevisedUnloadingProcedureController.onPageLoad(userAnswers.id))
          }
        }

        "RevisedAndGoodsTooLarge" - {
          "to CheckYourAnswersPage" in {

            val userAnswers = emptyUserAnswers
              .setValue(NewAuthYesNoPage, true)
              .setValue(RevisedUnloadingProcedureConditionsYesNoPage, true)
              .setValue(GoodsTooLargeForContainerYesNoPage, true)
              .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, false)
            navigator
              .nextPage(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, mode, userAnswers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
          }
        }
      }

      "must go from unloading comments page to check your answers" in {
        val userAnswers = emptyUserAnswers.setValue(UnloadingCommentsPage, "comments")

        navigator
          .nextPage(UnloadingCommentsPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from do you have anything else to report yes/no page" - {
        "when true" - {
          "and OtherThingsToReportPage undefined" - {
            "to other things to report page" in {
              val userAnswers = emptyUserAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, true)

              navigator
                .nextPage(DoYouHaveAnythingElseToReportYesNoPage, mode, userAnswers)
                .mustBe(controllers.routes.OtherThingsToReportController.onPageLoad(userAnswers.id, mode))
            }
          }

          "and OtherThingsToReportPage defined" - {
            "to CYA page" in {
              val userAnswers = emptyUserAnswers
                .setValue(OtherThingsToReportPage, "foo")
                .setValue(DoYouHaveAnythingElseToReportYesNoPage, true)

              navigator
                .nextPage(DoYouHaveAnythingElseToReportYesNoPage, mode, userAnswers)
                .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
            }
          }
        }

        "when false" - {
          "to check your answers page" in {
            val userAnswers = emptyUserAnswers.setValue(DoYouHaveAnythingElseToReportYesNoPage, false)

            navigator
              .nextPage(DoYouHaveAnythingElseToReportYesNoPage, mode, userAnswers)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
          }
        }
      }

      "must go from other things to report page to check your answers" in {
        val userAnswers = emptyUserAnswers.setValue(OtherThingsToReportPage, "report")

        navigator
          .nextPage(OtherThingsToReportPage, mode, userAnswers)
          .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
      }

      "must go from a page that doesn't exist in the edit route map  to Check Your Answers" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, mode, answers)
              .mustBe(controllers.routes.CheckYourAnswersController.onPageLoad(arrivalId))
        }
      }

      "must go from gross weight page to Unloading findings page" in {
        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(pages.GrossWeightPage, mode, answers)
              .mustBe(routes.UnloadingFindingsController.onPageLoad(arrivalId))
        }
      }

      "must go from SealsReplacedByCustomsAuthorityYesNoPage" - {
        "to CYA" - {
          "when OtherThingsToReportPage is answered" in {
            forAll(nonEmptyString) {
              otherThingsToReport =>
                val userAnswers = emptyUserAnswers
                  .setValue(OtherThingsToReportPage, otherThingsToReport)

                navigator
                  .nextPage(SealsReplacedByCustomsAuthorityYesNoPage, mode, userAnswers)
                  .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
            }
          }
        }

        "to other things to report in NormalMode" - {
          "when OtherThingsToReportPage is unanswered" in {
            val userAnswers = emptyUserAnswers

            navigator
              .nextPage(SealsReplacedByCustomsAuthorityYesNoPage, mode, userAnswers)
              .mustBe(routes.OtherThingsToReportController.onPageLoad(userAnswers.id, NormalMode))
          }
        }
      }

      "must go from GoodsTooLargeForContainerYesNoPage" - {
        "when Yes is submitted" - {
          "and AddTransitUnloadingPermissionDiscrepanciesYesNoPage is answered" - {
            "to CYA" in {
              forAll(arbitrary[Boolean]) {
                bool =>
                  val userAnswers = emptyUserAnswers
                    .setValue(AddTransitUnloadingPermissionDiscrepanciesYesNoPage, bool)
                    .setValue(GoodsTooLargeForContainerYesNoPage, true)

                  navigator
                    .nextPage(GoodsTooLargeForContainerYesNoPage, mode, userAnswers)
                    .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
              }
            }
          }

          "and AddTransitUnloadingPermissionDiscrepanciesYesNoPage is unanswered" - {
            "to unloading guidance" in {
              val userAnswers = emptyUserAnswers
                .setValue(GoodsTooLargeForContainerYesNoPage, true)

              navigator
                .nextPage(GoodsTooLargeForContainerYesNoPage, mode, userAnswers)
                .mustBe(routes.UnloadingGuidanceController.onPageLoad(userAnswers.id))
            }
          }
        }

        "when No is submitted" - {
          "and SealsReplacedByCustomsAuthorityYesNoPage is answered" - {
            "to CYA" in {
              forAll(arbitrary[Boolean]) {
                bool =>
                  val userAnswers = emptyUserAnswers
                    .setValue(SealsReplacedByCustomsAuthorityYesNoPage, bool)
                    .setValue(GoodsTooLargeForContainerYesNoPage, false)

                  navigator
                    .nextPage(GoodsTooLargeForContainerYesNoPage, mode, userAnswers)
                    .mustBe(routes.CheckYourAnswersController.onPageLoad(userAnswers.id))
              }
            }
          }

          "and SealsReplacedByCustomsAuthorityYesNoPage is unanswered" - {
            "to unloading guidance" in {
              val userAnswers = emptyUserAnswers
                .setValue(GoodsTooLargeForContainerYesNoPage, false)

              navigator
                .nextPage(GoodsTooLargeForContainerYesNoPage, mode, userAnswers)
                .mustBe(routes.UnloadingGuidanceController.onPageLoad(userAnswers.id))
            }
          }
        }
      }
    }
  }
}
