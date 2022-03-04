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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.requests.{IdentifierRequest, OptionalDataRequest}
import models.{EoriNumber, MovementReferenceNumber, UserAnswers}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.mvc.{AnyContent, Request, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with AppWithDefaultMockFixtures with Generators {

  def harness(mrn: MovementReferenceNumber, f: OptionalDataRequest[AnyContent] => Unit): Unit = {

    lazy val actionProvider = app.injector.instanceOf[DataRetrievalActionProviderImpl]

    actionProvider(arrivalId)
      .invokeBlock(
        IdentifierRequest(FakeRequest(GET, "/").asInstanceOf[Request[AnyContent]], EoriNumber("")),
        {
          request: OptionalDataRequest[AnyContent] =>
            f(request)
            Future.successful(Results.Ok)
        }
      )
      .futureValue
  }

  "a data retrieval action" - {

    "must return an OptionalDataRequest with an empty UserAnswers" - {

      "where there are no existing answers for this MRN" in {

        when(mockSessionRepository.get(any(), any())) thenReturn Future.successful(None)

        harness(mrn, request => request.userAnswers must not be defined)
      }
    }

    "must return an OptionalDataRequest with some defined UserAnswers" - {

      "when there are existing answers for this MRN" in {

        when(mockSessionRepository.get(any(), any())) thenReturn Future.successful(Some(UserAnswers(arrivalId, mrn, eoriNumber)))

        harness(mrn, request => request.userAnswers mustBe defined)
      }
    }
  }
}
