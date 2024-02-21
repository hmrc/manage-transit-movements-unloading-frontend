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

package services.submission

import base.{AppWithDefaultMockFixtures, SpecBase}
import models.AuditType.UnloadingRemarks
import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

class AuditServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val mockAuditConnector = mock[AuditConnector]

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .overrides(
        bind[AuditConnector].toInstance(mockAuditConnector)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditConnector)
  }

  "audit" - {
    "must audit event" - {
      "ArrivalNotification" in {
        val service = app.injector.instanceOf[AuditService]

        val userAnswers = emptyUserAnswers
        service.audit(UnloadingRemarks, userAnswers)

        val expectedDetail = Json.parse(s"""
            |{
            |  "channel" : "web",
            |  "detail" : ${Json.toJson(userAnswers)(UserAnswers.auditWrites)}
            |}
            |""".stripMargin)

        verify(mockAuditConnector).sendExplicitAudit(eqTo(UnloadingRemarks.name), eqTo(expectedDetail))(any(), any(), any())
      }
    }
  }

}
