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

package audit.services

import audit.models.{AuditAutoInput, AuditEventData, AuditUserInput}
import base.SpecBase
import generators.Generators
import models.requests.DataRequest
import models.{UnloadingPermission, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{doNothing, reset, verify}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global

class AuditEventSubmissionServiceSpec extends SpecBase with BeforeAndAfterEach with ScalaCheckPropertyChecks with Generators {

  private val mockAuditConnector = mock[AuditConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditConnector)
    doNothing().when(mockAuditConnector).sendExplicitAudit(any[String](), any[AuditEventData]())(any(), any(), any())
  }

  private val service = new AuditEventSubmissionService(mockAuditConnector)

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  "AuditEventSubmissionService" - {
    "auditUnloadingRemarks" - {
      "must audit unloading remarks" in {
        forAll(arbitrary[String], arbitrary[UserAnswers], arbitrary[UnloadingPermission]) {
          (auditType, userAnswers, unloadingPermission) =>
            beforeEach()

            implicit val request: DataRequest[_] = DataRequest(fakeRequest, eoriNumber, userAnswers)

            service.auditUnloadingRemarks(unloadingPermission, auditType)

            verify(mockAuditConnector)
              .sendExplicitAudit(
                eqTo(auditType),
                eqTo(AuditEventData(AuditUserInput(userAnswers), AuditAutoInput(unloadingPermission)))
              )(any(), any(), any())
        }
      }
    }
  }
}
