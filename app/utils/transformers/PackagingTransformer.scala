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

import generated.PackagingType02
import models.reference.PackageType
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

  private case class TempPackaging[T](
    underlying: T,
    typeValue: PackageType
  )

  def transform(packages: Seq[PackagingType02], hcIndex: Index, itemIndex: Index)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    userAnswers => {

      lazy val packageTypeRefLookups = packages.map {
        `package` =>
          referenceDataService
            .getPackageType(`package`.typeOfPackages)
            .map(TempPackaging(`package`, _))
      }

      Future.sequence(packageTypeRefLookups).flatMap {
        _.zipWithIndex.foldLeft(Future.successful(userAnswers)) {
          case (acc, (TempPackaging(underlying, typeValue), i)) =>
            val packageIndex: Index = Index(i)
            acc.flatMap {
              userAnswers =>
                val pipeline: UserAnswers => Future[UserAnswers] =
                  setSequenceNumber(PackagingSection(hcIndex, itemIndex, packageIndex), underlying.sequenceNumber) andThen
                    set(PackageTypePage(hcIndex, itemIndex, packageIndex), typeValue) andThen
                    set(NumberOfPackagesPage(hcIndex, itemIndex, packageIndex), underlying.numberOfPackages) andThen
                    set(PackageShippingMarkPage(hcIndex, itemIndex, packageIndex), underlying.shippingMarks)

                pipeline(userAnswers)
            }
        }
      }
    }
}
