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

package repositories

import com.google.inject.{Inject, Singleton}
import com.mongodb.client.model.ReturnDocument
import models.messages.InterchangeControlReference
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}
import services.DateTimeService
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class InterchangeControlReferenceIdRepository @Inject() (
  mongoComponent: MongoComponent,
  dateTimeService: DateTimeService
) extends PlayMongoRepository[InterchangeControlReference](
      mongoComponent = mongoComponent,
      collectionName = "interchange-control-reference-ids",
      domainFormat = InterchangeControlReference.format,
      indexes = Nil
    ) {

  def nextInterchangeControlReferenceId(): Future[InterchangeControlReference] = {

    val filter = Filters.eq("_id", dateTimeService.dateFormatted)

    val update = Updates.inc("last-index", 1)

    collection
      .findOneAndUpdate(filter, update, FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER))
      .toFuture()
  }
}
