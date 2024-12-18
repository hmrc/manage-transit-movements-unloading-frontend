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

package pages

import pages.sections.OtherQuestionsSection
import play.api.libs.json.JsPath

// This is similar to NewAuthYesNoPage to determine if the user chooses new procedure
// For navigation we need to know how the user journey starts
// However we cannot use NewAuthYesNoPage for this purpose as in some cases it's updated programmatically
case object DidUserChooseNewProcedurePage extends QuestionPage[Boolean] {

  override def path: JsPath = OtherQuestionsSection.path \ toString

  override def toString: String = "userChoseNewProcedure"

}
