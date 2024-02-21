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

import connectors.ReferenceDataConnector
import generated.PackagingType02
import models.reference.PackageType
import models.{Index, UserAnswers}
import pages.houseConsignment.index.items.packaging.{PackagingCountPage, PackagingMarksPage, PackagingTypePage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PackagingTransformer @Inject() (referenceDataConnector: ReferenceDataConnector)(implicit ec: ExecutionContext) extends PageTransformer {

  def transform(packages: Seq[PackagingType02], hcIndex: Index, itemIndex: Index)(implicit hc: HeaderCarrier): UserAnswers => Future[UserAnswers] =
    userAnswers =>
      packages.zipWithIndex.foldLeft(Future.successful(userAnswers))({
        case (accumulatedUA, (packagingType0, i)) =>
          val packageIndex: Index = Index(i)
          def pipeline(packageType: PackageType): UserAnswers => Future[UserAnswers] =
            set(PackagingTypePage(hcIndex, itemIndex, packageIndex), packageType) andThen
              set(PackagingCountPage(hcIndex, itemIndex, packageIndex), packagingType0.numberOfPackages) andThen
              set(PackagingMarksPage(hcIndex, itemIndex, packageIndex), packagingType0.shippingMarks)
          for {
            userAnswers <- accumulatedUA
            packageType <- referenceDataConnector.getPackageType(packagingType0.typeOfPackages)
            ua          <- pipeline(packageType)(userAnswers)
          } yield ua

      })

}
