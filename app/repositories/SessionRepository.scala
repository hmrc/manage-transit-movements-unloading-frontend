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

package repositories

import config.FrontendAppConfig
import models.{ArrivalId, EoriNumber, SensitiveFormats, UserAnswers}
import org.mongodb.scala.model.Indexes.{ascending, compoundIndex}
import org.mongodb.scala.model._
import services.DateTimeService
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import org.mongodb.scala._

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: FrontendAppConfig,
  dateTimeService: DateTimeService
)(implicit ec: ExecutionContext, sensitiveFormats: SensitiveFormats)
    extends PlayMongoRepository[UserAnswers](
      mongoComponent = mongoComponent,
      collectionName = "user-answers",
      domainFormat = UserAnswers.format,
      indexes = SessionRepository.indexes(appConfig),
      replaceIndexes = appConfig.replaceIndexes
    ) {

  def get(id: ArrivalId, eoriNumber: EoriNumber): Future[Option[UserAnswers]] = {
    val filter = Filters.and(
      Filters.eq("_id", id.value),
      Filters.eq("eoriNumber", eoriNumber.value)
    )
    val update = Updates.set("lastUpdated", dateTimeService.now)

    collection
      .findOneAndUpdate(filter, update, FindOneAndUpdateOptions().upsert(false))
      .toFutureOption()
  }

  def set(userAnswers: UserAnswers): Future[Boolean] = {
    val filter             = Filters.eq("_id", userAnswers.id.value)
    val updatedUserAnswers = userAnswers.copy(lastUpdated = dateTimeService.now)

    collection
      .replaceOne(filter, updatedUserAnswers, ReplaceOptions().upsert(true))
      .toFuture()
      .map(_.wasAcknowledged())
  }

  def remove(id: ArrivalId): Future[Boolean] = {
    val filter = Filters.eq("_id", id.value)

    collection
      .deleteOne(filter)
      .toFuture()
      .map(_.wasAcknowledged())
  }
}

object SessionRepository {

  def indexes(appConfig: FrontendAppConfig): Seq[IndexModel] = {
    val userAnswersLastUpdatedIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("lastUpdated"),
      indexOptions = IndexOptions().name("user-answers-last-updated-index").expireAfter(appConfig.cacheTtl.asInstanceOf[Number].longValue, TimeUnit.SECONDS)
    )

    val idAndEoriNumberCompoundIndex: IndexModel = IndexModel(
      keys = compoundIndex(ascending("_id"), ascending("eoriNumber")),
      indexOptions = IndexOptions().name("id-and-eori-number-compound-index")
    )

    Seq(userAnswersLastUpdatedIndex, idAndEoriNumberCompoundIndex)
  }

}
