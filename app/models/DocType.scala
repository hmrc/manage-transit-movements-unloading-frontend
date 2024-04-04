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

package models

sealed trait DocType {
  val display: String

  override def toString: String = display
}

object DocType extends EnumerableType[DocType] {

  case object Support extends DocType {
    override val display = "Supporting"
  }

  case object Transport extends DocType {
    override val display = "Transport"
  }

  case object Previous extends DocType {
    override val display = "Previous"
  }

  override val values: Seq[DocType] = Seq(Support, Transport, Previous)
}
