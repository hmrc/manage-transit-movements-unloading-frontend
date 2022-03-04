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

package controllers

import config.FrontendAppConfig
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

trait TechnicalDifficultiesPage {
  self: FrontendBaseController =>

  val appConfig: FrontendAppConfig
  val renderer: Renderer

  def renderTechnicalDifficultiesPage(implicit request: Request[_], ec: ExecutionContext): Future[Result] = {
    val json = Json.obj(
      "contactUrl" -> appConfig.nctsEnquiriesUrl
    )

    renderer.render("technicalDifficulties.njk", json).map(InternalServerError(_))
  }

}
