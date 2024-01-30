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

sealed trait UnloadingType extends Radioable[UnloadingType] {
  override val messageKeyPrefix: String = UnloadingType.messageKeyPrefix
}

object UnloadingType extends EnumerableType[UnloadingType] {

  val messageKeyPrefix: String = "unloadingType"

  case object Fully extends WithName("1") with UnloadingType {
    override val code: String = this.toString
  }

  case object Partially extends WithName("0") with UnloadingType {
    override val code: String = this.toString
  }

  override val values: Seq[UnloadingType] = Seq(
    Fully,
    Partially
  )
}
