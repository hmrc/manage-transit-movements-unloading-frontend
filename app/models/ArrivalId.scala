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

package models

import play.api.libs.json._
import play.api.mvc.{JavascriptLiteral, PathBindable}

import scala.util.Try

final case class ArrivalId(value: Int) {
  override def toString: String = value.toString
}

object ArrivalId {

  implicit val formatsArrivalId: Format[ArrivalId] = new Format[ArrivalId] {

    override def reads(json: JsValue): JsResult[ArrivalId] = json match {
      case JsNumber(number) =>
        Try(number.toInt)
          .map(ArrivalId(_))
          .map(JsSuccess(_))
          .getOrElse(JsError("Error in converting JsNumber to an Int"))

      case e =>
        JsError(s"Error in deserialization of Json value to an ArrivalId, expected JsNumber got ${e.getClass}")
    }

    override def writes(o: ArrivalId): JsNumber = JsNumber(o.value)
  }

  implicit lazy val pathBindable: PathBindable[ArrivalId] = new PathBindable[ArrivalId] {

    override def bind(key: String, value: String): Either[String, ArrivalId] =
      implicitly[PathBindable[Int]].bind(key, value).map(ArrivalId(_))

    override def unbind(key: String, value: ArrivalId): String =
      value.toString
  }

  implicit val arrivalIdJSLBinder: JavascriptLiteral[ArrivalId] = (value: ArrivalId) => s"""'${value.toString}'"""

}
