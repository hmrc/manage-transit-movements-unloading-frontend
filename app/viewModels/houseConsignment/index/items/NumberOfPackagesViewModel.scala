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

package viewModels.houseConsignment.index.items

import models.{Index, Mode}
import play.api.i18n.Messages
import viewModels.ModeViewModelProvider

import javax.inject.Inject

case class NumberOfPackagesViewModel(heading: String, title: String, args: Seq[String])

object NumberOfPackagesViewModel {

  class NumberOfPackagesViewModelProvider @Inject() extends ModeViewModelProvider {

    override val prefix = "houseConsignment.index.item.numberOfPackages"

    def apply(houseConsignmentIndex: Index, itemIndex: Index, mode: Mode)(implicit message: Messages): NumberOfPackagesViewModel =
      new NumberOfPackagesViewModel(
        heading(mode, houseConsignmentIndex, itemIndex),
        title(mode, houseConsignmentIndex, itemIndex),
        Seq(houseConsignmentIndex.display.toString, itemIndex.display.toString)
      )
  }
}
