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

import com.google.inject.{Inject, Singleton}
import config.PhaseConfig
import logging.Logging
import models.Phase
import models.requests.DataRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

@Singleton
class UnreachablePageActionImpl @Inject() (implicit val executionContext: ExecutionContext, phaseConfig: PhaseConfig)
    extends UnreachablePageAction
    with Logging {

  override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] =
    phaseConfig.phase match {
      case Phase.Transition =>
        val addHouseConsignmentPattern =
          """^.*/(change-)?house-consignments/.*$""".r

        val removeHouseConsignmentPattern =
          """^.*/(change-)?house-consignment/(\d+)/remove.*$""".r

        val multiHouseConsignmentPattern =
          """^.*/(change-)?house-consignment/(\d+).*$""".r

        val unreachableSectionsPattern: Regex =
          """^.*/(change-)?house-consignment/1/((change-)?(additional-reference(s)?|document(s)?|departure-means-of-transport))/.*$""".r

        def redirect: Future[Option[Result]] = {
          logger.warn(s"${request.uri} is not available during transition")
          Future.successful(Some(Redirect(controllers.routes.ErrorController.notFound())))
        }

        request.uri match {
          case addHouseConsignmentPattern(_) =>
            redirect
          case removeHouseConsignmentPattern(_, _) =>
            redirect
          case multiHouseConsignmentPattern(_, index) if index.toInt > 1 =>
            redirect
          case unreachableSectionsPattern(_, _, _, _, _, _) =>
            redirect
          case _ =>
            Future.successful(None)
        }
      case Phase.PostTransition =>
        Future.successful(None)
    }
}

trait UnreachablePageAction extends ActionFilter[DataRequest]
