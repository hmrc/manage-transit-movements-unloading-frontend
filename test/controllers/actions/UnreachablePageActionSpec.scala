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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.EoriNumber
import models.requests.DataRequest
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Results._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class UnreachablePageActionSpec extends SpecBase with BeforeAndAfterEach with Generators with AppWithDefaultMockFixtures {

  private def fakeOkResult[A]: A => Future[Result] =
    _ => Future.successful(Ok)

  private val id = "6639db9d06806dde"

  private val reachablePages = {
    val otherPages = Seq(
      s"/manage-transit-movements/unloading/$id/transit-unloading-permission-discrepancies",
      s"/manage-transit-movements/unloading/$id/transit-unloading-permission-discrepancies/house-consignment/1",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/gross-weight"
    )

    val consignmentItemPages = Seq(
      s"/manage-transit-movements/unloading/$id/house-consignment/1/items/add-another"
    )

    val consignmentItemAdditionalReferencesPages = Seq(
      s"/manage-transit-movements/unloading/$id/house-consignment/1/item/1/additional-references/add-another",
      s"/manage-transit-movements/unloading/$id/house-consignment/1/item/1/additional-reference/1/type",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/change-item/1/additional-references/add-another",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/change-item/1/change-additional-reference/1/type"
    )

    val consignmentItemDocumentsPages = Seq(
      s"/manage-transit-movements/unloading/$id/house-consignment/1/item/1/documents/add-another",
      s"/manage-transit-movements/unloading/$id/house-consignment/1/item/1/document/1/type",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/change-item/1/documents/add-another",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/change-item/1/change-document/1/type"
    )

    val consignmentItemDepartureMeansOfTransportPages = Seq(
      s"/manage-transit-movements/unloading/$id/house-consignment/1/item/1/departure-means-of-transport/add-another",
      s"/manage-transit-movements/unloading/$id/house-consignment/1/item/1/departure-means-of-transport/1/country",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/change-item/1/departure-means-of-transport/add-another",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/change-item/1/change-departure-means-of-transport/1/country"
    )

    otherPages ++
      consignmentItemPages ++
      consignmentItemAdditionalReferencesPages ++
      consignmentItemDocumentsPages ++
      consignmentItemDepartureMeansOfTransportPages
  }

  private val unreachablePages = {
    val addOrRemoveHouseConsignmentPages = Seq(
      s"/manage-transit-movements/unloading/$id/house-consignments/add-another",
      s"/manage-transit-movements/unloading/$id/change-house-consignments/add-another",
      s"/manage-transit-movements/unloading/$id/house-consignment/1/remove",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/remove"
    )

    val multiHouseConsignmentPages = Seq(
      s"/manage-transit-movements/unloading/$id/transit-unloading-permission-discrepancies/house-consignment/2",
      s"/manage-transit-movements/unloading/$id/transit-unloading-permission-discrepancies/house-consignment/10",
      s"/manage-transit-movements/unloading/$id/house-consignment/2/gross-weight",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/2/gross-weight"
    )

    val houseConsignmentAdditionalReferencesPages = Seq(
      s"/manage-transit-movements/unloading/$id/house-consignment/1/additional-references/add-another",
      s"/manage-transit-movements/unloading/$id/house-consignment/1/additional-reference/1/type",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/additional-references/add-another",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/change-additional-reference/1/type"
    )

    val houseConsignmentDocumentsPages = Seq(
      s"/manage-transit-movements/unloading/$id/house-consignment/1/documents/add-another",
      s"/manage-transit-movements/unloading/$id/house-consignment/1/document/1/type",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/documents/add-another",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/change-document/1/type"
    )

    val houseConsignmentDepartureMeansOfTransportPages = Seq(
      s"/manage-transit-movements/unloading/$id/house-consignment/1/departure-means-of-transport/add-another",
      s"/manage-transit-movements/unloading/$id/house-consignment/1/departure-means-of-transport/1/country",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/departure-means-of-transport/add-another",
      s"/manage-transit-movements/unloading/$id/change-house-consignment/1/change-departure-means-of-transport/1/country"
    )

    addOrRemoveHouseConsignmentPages ++
      multiHouseConsignmentPages ++
      houseConsignmentAdditionalReferencesPages ++
      houseConsignmentDocumentsPages ++
      houseConsignmentDepartureMeansOfTransportPages
  }

  "UnreachablePageAction" - {

    "when during transition" - {
      val app = transitionApplicationBuilder().build()
      running(app) {

        "must return None" - {
          "when a reachable page" in {
            forAll(Gen.oneOf(reachablePages)) {
              reachablePage =>
                val action = app.injector.instanceOf[UnreachablePageAction]

                val testRequest = DataRequest(
                  FakeRequest(GET, reachablePage),
                  EoriNumber("eori"),
                  emptyUserAnswers
                )

                val result: Future[Result] = action.invokeBlock(testRequest, fakeOkResult)

                status(result) mustEqual OK
            }
          }
        }

        "must redirect to page not found" - {
          "when an unreachable page" in {
            forAll(Gen.oneOf(unreachablePages)) {
              unreachablePage =>
                val action = app.injector.instanceOf[UnreachablePageAction]

                val testRequest = DataRequest(
                  FakeRequest(GET, unreachablePage),
                  eoriNumber,
                  emptyUserAnswers
                )

                val result: Future[Result] = action.invokeBlock(testRequest, fakeOkResult)

                status(result) mustEqual SEE_OTHER

                redirectLocation(result).value mustBe
                  controllers.routes.ErrorController.notFound().url
            }
          }
        }
      }
    }

    "when post-transition" - {
      val app = postTransitionApplicationBuilder().build()
      running(app) {

        "must return None" in {
          val pages = reachablePages ++ unreachablePages
          forAll(Gen.oneOf(pages)) {
            page =>
              val action = app.injector.instanceOf[UnreachablePageAction]

              val testRequest = DataRequest(
                FakeRequest(GET, page),
                EoriNumber("eori"),
                emptyUserAnswers
              )

              val result: Future[Result] = action.invokeBlock(testRequest, fakeOkResult)

              status(result) mustEqual OK
          }
        }
      }
    }
  }
}
