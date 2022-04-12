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

import models.ArrivalId
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.mvc.{ActionBuilder, AnyContent}

import javax.inject.Inject

class Actions @Inject() (
  identifierAction: IdentifierAction,
  dataRetrievalActionProvider: DataRetrievalActionProvider,
  checkArrivalStatusProvider: CheckArrivalStatusProvider,
  dataRequiredAction: DataRequiredAction
) {

  def getDataWithStatus(arrivalId: ArrivalId): ActionBuilder[OptionalDataRequest, AnyContent] =
    identifierAction andThen checkArrivalStatusProvider(arrivalId) andThen dataRetrievalActionProvider(arrivalId)

  def getData(arrivalId: ArrivalId): ActionBuilder[OptionalDataRequest, AnyContent] =
    identifierAction andThen dataRetrievalActionProvider(arrivalId)

  def requireData(arrivalId: ArrivalId): ActionBuilder[DataRequest, AnyContent] =
    getData(arrivalId) andThen dataRequiredAction

  def requireDataWithStatus(arrivalId: ArrivalId): ActionBuilder[DataRequest, AnyContent] =
    getDataWithStatus(arrivalId) andThen dataRequiredAction

}
