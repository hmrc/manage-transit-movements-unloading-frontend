/*
 * Copyright 2024 HM Revenue & Customs
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

package navigation

import com.google.inject.Singleton
import models.{NormalMode, UserAnswers}
import pages._
import play.api.mvc.Call

@Singleton
class NewAuthNavigator extends Navigator {

  override protected def normalRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = {
    case NewAuthYesNoPage =>
      ua =>
        ua.get(NewAuthYesNoPage).map {
          case true  => controllers.routes.OtherThingsToReportController.onPageLoad(ua.id, NormalMode)
          case false => controllers.routes.UnloadingTypeController.onPageLoad(ua.id, NormalMode)
        }
  }

  override protected def checkRoutes: PartialFunction[Page, UserAnswers => Option[Call]] = ???
}
