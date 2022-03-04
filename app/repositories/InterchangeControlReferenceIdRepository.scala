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
import models.messages.InterchangeControlReference
import play.api.libs.json.{Json, Reads}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.play.json.collection.Helpers.idWrites
import reactivemongo.play.json.collection._
import services.DateTimeService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class InterchangeControlReferenceIdRepository @Inject() (
  mongo: ReactiveMongoApi,
  dateTimeService: DateTimeService
) {

  private val lastIndexKey = "last-index"

  private val collectionName: String = "interchange-control-reference-ids"

  private val indexKeyReads: Reads[Int] = {
    import play.api.libs.json._
    (__ \ lastIndexKey).read[Int]
  }

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  def nextInterchangeControlReferenceId(): Future[InterchangeControlReference] = {

    val date = dateTimeService.dateFormatted

    val update = Json.obj(
      "$inc" -> Json.obj(lastIndexKey -> 1)
    )

    val selector = Json.obj("_id" -> s"$date")

    collection.flatMap {
      _.findAndUpdate(
        selector = selector,
        update = update,
        fetchNewObject = true,
        upsert = true,
        sort = None,
        fields = None,
        bypassDocumentValidation = false,
        writeConcern = WriteConcern.Default,
        maxTime = None,
        collation = None,
        arrayFilters = Nil
      ).map(
        _.result(indexKeyReads)
          .map(InterchangeControlReference(date, _))
          .getOrElse(throw new Exception(s"Unable to generate InterchangeControlReferenceId for: $date"))
      )
    }
  }
}
