/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.ngrnotifyproto.repository

import org.bson.codecs.ObjectIdCodec
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import org.mongodb.scala.result.DeleteResult
import org.mongodb.scala.{ObservableFuture, SingleObservableFuture}
import play.api.libs.json.JsObject
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import uk.gov.hmrc.ngrnotifyproto.model.EmailTemplate
import uk.gov.hmrc.ngrnotifyproto.model.db.EmailNotification
import uk.gov.hmrc.ngrnotifyproto.repository.EmailNotificationRepo.saveForDays

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Yuriy Tumakha
  */
@Singleton
class EmailNotificationRepo @Inject() (mongo: MongoComponent)(using
  ec: ExecutionContext
) extends PlayMongoRepository[EmailNotification](
      collectionName = "emailNotification",
      mongoComponent = mongo,
      domainFormat = EmailNotification.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("createdAt"),
          IndexOptions().name("emailNotificationTTL").expireAfter(saveForDays, TimeUnit.DAYS)
        )
      ),
      extraCodecs = Seq(
        new ObjectIdCodec,
        Codecs.playFormatCodec(MongoJavatimeFormats.instantFormat)
      )
    ):

  private val _id = "_id"

  private def byId(id: ObjectId): Bson = Filters.equal(_id, id)

  def find(id: ObjectId): Future[Option[EmailNotification]] =
    collection
      .find(byId(id))
      .headOption()

  /**
    * Save email notification.
    *
    * @param emailTemplate EmailTemplate
    * @param email Receiver email
    * @param templateParams Template params in JSON
    * @return ObjectId
    */
  def save(emailTemplate: EmailTemplate, email: String, templateParams: JsObject): Future[ObjectId] =
    collection
      .insertOne(EmailNotification(emailTemplate, email, templateParams))
      .toFuture()
      .map(_.getInsertedId.asObjectId().getValue)

  /**
    * Returns a batch of email notifications with a size less than or equal to the specified limit.
    *
    * @param limit Batch limit
    * @return Email notifications batch
    */
  def getNotificationsBatch(limit: Int): Future[Seq[EmailNotification]] =
    collection
      .find()
      .sort(Sorts.ascending("createdAt"))
      .limit(limit)
      .toFuture()

  def delete(id: ObjectId): Future[DeleteResult] =
    collection
      .deleteOne(byId(id))
      .toFuture()

object EmailNotificationRepo:
  val saveForDays = 90L
