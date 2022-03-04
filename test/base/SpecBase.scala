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

package base

import cats.data.NonEmptyList
import models.{ArrivalId, EoriNumber, GoodsItem, MovementReferenceNumber, Packages, ProducedDocument, UserAnswers}
import org.scalatest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar {

  val arrivalId: ArrivalId = ArrivalId(1)

  val configKey = "config"

  val mrn: MovementReferenceNumber  = MovementReferenceNumber("19", "GB", "1234567890123")
  val eoriNumber: EoriNumber        = EoriNumber("id")
  def emptyUserAnswers: UserAnswers = UserAnswers(arrivalId, mrn, eoriNumber, Json.obj())

  protected lazy val packages: Packages = Packages(Some("Ref."), "BX", Some(1), None)

  protected lazy val producedDocuments: ProducedDocument = ProducedDocument("235", Some("Ref."), None)

  protected lazy val goodsItemMandatory: GoodsItem = GoodsItem(
    itemNumber = 1,
    commodityCode = None,
    description = "Flowers",
    grossMass = Some("1000"),
    netMass = Some("999"),
    producedDocuments = Seq.empty,
    containers = Seq.empty,
    packages = NonEmptyList(packages, Nil),
    sensitiveGoodsInformation = Seq.empty
  )

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  implicit def messages: Messages = Helpers.stubMessages()
}
