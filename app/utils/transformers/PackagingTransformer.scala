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

package utils.transformers

import generated.PackagingType01
import models.{Index, UserAnswers}
import pages.houseConsignment.index.items.packages.{NumberOfPackagesPage, PackageShippingMarkPage, PackageTypePage}
import pages.sections.PackagingSection
import services.ReferenceDataService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackagingTransformer @Inject() (
  referenceDataService: ReferenceDataService
)(implicit ec: ExecutionContext)
    extends PageTransformer {

  def transform(packages: Seq[PackagingType01], hcIndex: Index, itemIndex: Index)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      packages.zipWithIndex
        .foldLeft(Future.successful(userAnswers)) {
          case (acc, (value, i)) =>
            val packageIndex: Index = Index(i)
            acc.flatMap {
              setSequenceNumber(PackagingSection(hcIndex, itemIndex, packageIndex), value.sequenceNumber) andThen
                set(PackageTypePage(hcIndex, itemIndex, packageIndex), value.typeOfPackages, referenceDataService.getPackageType) andThen
                set(NumberOfPackagesPage(hcIndex, itemIndex, packageIndex), value.numberOfPackages) andThen
                set(PackageShippingMarkPage(hcIndex, itemIndex, packageIndex), value.shippingMarks)
            }
        }
}
