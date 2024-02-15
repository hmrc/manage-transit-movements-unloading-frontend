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

package base

import config.FrontendAppConfig
import models.{ArrivalId, EoriNumber, Index, MovementReferenceNumber, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.{ActionItem, Content, Key, Value}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with TryValues
    with EitherValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with GuiceOneAppPerSuite
    with TestMessageData {

  val arrivalId: ArrivalId = ArrivalId("AB123")
  val messageId: String    = "12344565gf"

  val configKey = "config"

  def injector: Injector = app.injector

  val mrn: MovementReferenceNumber = MovementReferenceNumber("19GB1234567890123")
  val eoriNumber: EoriNumber       = EoriNumber("id")

  val index: Index                  = Index(0)
  val hcIndex: Index                = Index(0)
  val equipmentIndex: Index         = Index(0)
  val sealIndex: Index              = Index(0)
  val houseConsignmentIndex: Index  = Index(0)
  val itemIndex: Index              = Index(0)
  val packageIndex: Index           = Index(0)
  val documentIndex: Index          = Index(0)
  def emptyUserAnswers: UserAnswers = UserAnswers(arrivalId, mrn, eoriNumber, basicIe043, Json.obj(), Instant.now())

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  def messagesApi: MessagesApi    = app.injector.instanceOf[MessagesApi]
  implicit def messages: Messages = messagesApi.preferred(fakeRequest)
  implicit val hc: HeaderCarrier  = HeaderCarrier()

  implicit def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  implicit class RichUserAnswers(userAnswers: UserAnswers) {

    def getValue[T](page: QuestionPage[T])(implicit rds: Reads[T]): T =
      userAnswers.get(page).value

    def setValue[T](page: QuestionPage[T], value: T)(implicit wts: Writes[T]): UserAnswers =
      userAnswers.set(page, value).success.value

    def setValue[T](page: QuestionPage[T], value: Option[T])(implicit wts: Writes[T]): UserAnswers =
      value.map(setValue(page, _)).getOrElse(userAnswers)

    def removeValue(page: QuestionPage[_]): UserAnswers =
      userAnswers.remove(page).success.value
  }

  implicit class RichContent(c: Content) {
    def value: String = c.asHtml.toString()
  }

  implicit class RichValue(v: Value) {
    def value: String = v.content.value
  }

  implicit class RichAction(ai: ActionItem) {
    def id: String = ai.attributes.get("id").value
  }

  implicit class RichKey(k: Key) {
    def value: String = k.content.value
  }

}
