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

import models.ArrivalId
import models.requests.{DataRequest, OptionalDataRequest}
import pages.sections.Section
import play.api.libs.json.JsObject
import play.api.mvc.{ActionBuilder, AnyContent, Call}

import javax.inject.Inject

class Actions @Inject() (
  identifierAction: IdentifierAction,
  checkArrivalStatusProvider: CheckArrivalStatusProvider,
  dataRetrievalAction: DataRetrievalActionProvider,
  dataRequiredAction: DataRequiredAction,
  indexRequiredAction: IndexRequiredActionProvider,
  phase6Action: Phase6ActionProvider
) {

  def getData(arrivalId: ArrivalId): ActionBuilder[OptionalDataRequest, AnyContent] =
    identifierAction andThen
      checkArrivalStatusProvider(arrivalId) andThen
      dataRetrievalAction(arrivalId)

  def requireData(arrivalId: ArrivalId): ActionBuilder[DataRequest, AnyContent] =
    getData(arrivalId) andThen
      dataRequiredAction

  def requireDataOnly(arrivalId: ArrivalId): ActionBuilder[DataRequest, AnyContent] =
    identifierAction andThen
      dataRetrievalAction(arrivalId) andThen
      dataRequiredAction

  def requireIndex(arrivalId: ArrivalId, section: Section[JsObject], addAnother: => Call): ActionBuilder[DataRequest, AnyContent] =
    requireData(arrivalId) andThen
      indexRequiredAction(section, addAnother)

  def requirePhase6(arrivalId: ArrivalId): ActionBuilder[DataRequest, AnyContent] =
    requireData(arrivalId) andThen
      phase6Action()
}
