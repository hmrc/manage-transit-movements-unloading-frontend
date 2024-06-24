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

package services.submission

import com.codahale.metrics.MetricRegistry
import play.api.mvc.BaseController
import uk.gov.hmrc.http.HttpErrorFunctions.{is4xx, is5xx}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import javax.inject.Inject

class MetricsService @Inject() (metrics: Metrics) {
  self: BaseController =>

  private lazy val registry: MetricRegistry = metrics.defaultRegistry

  def increment(name: String, response: HttpResponse): Unit =
    increment(name, response.status)

  def increment(name: String, status: Int): Unit =
    status match {
      case status if is4xx(status) => registry.counter(s"$name-4xx").inc()
      case status if is5xx(status) => registry.counter(s"$name-5xx").inc()
      case _                       => registry.counter(name).inc()
    }

}
