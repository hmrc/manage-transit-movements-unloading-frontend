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

package viewModels

import config.FrontendAppConfig
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.viewmodels.NunjucksSupport
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}
import viewModels.sections.Section

class SectionSpec extends AnyFreeSpec with Matchers with NunjucksSupport with GuiceOneAppPerSuite {

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

  "Section" - {
    "must serialise to Json" in {
      val key          = Key(lit"foo")
      val value        = Value(lit"bar")
      val action       = Action(lit"baz", "quux")
      val sectionTitle = lit"Section title"

      val expectedSection = Json.obj(
        "sectionTitle" -> sectionTitle.resolve,
        "rows" -> Json.arr(
          Json.obj(
            "key"   -> key,
            "value" -> value,
            "actions" -> Json.obj(
              "items" -> Json.arr(
                action
              )
            )
          )
        )
      )

      val section = Section(
        sectionTitle = Some(sectionTitle),
        rows = Seq(
          Row(
            key = key,
            value = value,
            actions = List(
              action
            )
          )
        )
      )

      Json.toJson(section) mustBe expectedSection
    }

    "must serialise empty sections" in {
      Json.toJson(Section(None, Nil)) mustBe Json.obj("sectionTitle" -> JsNull, "rows" -> Json.arr())
    }
  }

}
