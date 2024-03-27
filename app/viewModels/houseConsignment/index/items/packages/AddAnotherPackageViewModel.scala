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

package viewModels.houseConsignment.index.items.packages

import config.FrontendAppConfig
import models.{ArrivalId, Index, Mode, RichOptionalJsArray, UserAnswers}
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageTypePage}
import pages.sections.PackagingListSection
import play.api.libs.json.JsArray
import play.api.mvc.Call
import viewModels.{AddAnotherViewModel, ListItem}

case class AddAnotherPackageViewModel(
  listItems: Seq[ListItem],
  onSubmitCall: Call,
  nextIndex: Index
) extends AddAnotherViewModel {
  override val prefix: String = "houseConsignment.index.items.packages.addAnotherPackage"

  override def maxCount(implicit config: FrontendAppConfig): Int = config.maxPackages

}

object AddAnotherPackageViewModel {

  class AddAnotherPackageViewModelProvider {

    def apply(userAnswers: UserAnswers, arrivalId: ArrivalId, houseConsignmentIndex: Index, itemIndex: Index, mode: Mode): AddAnotherPackageViewModel = {

      val array = userAnswers.get(PackagingListSection(houseConsignmentIndex, itemIndex))

      val listItems = array
        .getOrElse(JsArray())
        .value
        .zipWithIndex
        .flatMap {
          case (_, index) =>
            val packageIndex = Index(index)

            userAnswers
              .get(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex))
              .flatMap(
                packageType =>
                  userAnswers
                    .get(NumberOfPackagesPage(houseConsignmentIndex, itemIndex, packageIndex))
                    .map(
                      quantity => s"$quantity * $packageType"
                    )
              )
              .orElse(
                userAnswers
                  .get(PackageTypePage(houseConsignmentIndex, itemIndex, packageIndex))
                  .map(
                    packageType => s"$packageType"
                  )
              )
              .map {
                name =>
                  ListItem(
                    name = name,
                    changeUrl = None,
                    removeUrl = Some(
                      controllers.houseConsignment.index.items.packages.routes.RemovePackageTypeYesNoController
                        .onPageLoad(arrivalId, houseConsignmentIndex, itemIndex, packageIndex, mode)
                        .url
                    )
                  )
              }
        }
        .toSeq

      new AddAnotherPackageViewModel(
        listItems,
        onSubmitCall =
          controllers.houseConsignment.index.items.packages.routes.AddAnotherPackageController.onSubmit(arrivalId, houseConsignmentIndex, itemIndex, mode),
        nextIndex = array.nextIndex
      )
    }
  }
}
