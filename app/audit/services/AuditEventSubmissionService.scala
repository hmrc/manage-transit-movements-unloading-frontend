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
import com.google.inject.Inject
import models.UnloadingPermission
import models.requests.DataRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext

class AuditEventSubmissionService @Inject() (auditConnector: AuditConnector) {

  def auditUnloadingRemarks(
    unloadingPermission: Option[UnloadingPermission],
    auditType: String
  )(implicit request: DataRequest[_], hc: HeaderCarrier, ec: ExecutionContext): Unit =
    auditConnector.sendExplicitAudit(
      auditType = auditType,
      detail = AuditEventData(
        userInput = AuditUserInput(request.userAnswers),
        autoInput = unloadingPermission.map(AuditAutoInput(_))
      )
    )
}
