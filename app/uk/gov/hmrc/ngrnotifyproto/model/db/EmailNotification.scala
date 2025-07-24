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

package uk.gov.hmrc.ngrnotifyproto.model.db

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json.{JsObject, Json, OFormat}
import uk.gov.hmrc.ngrnotifyproto.model.EmailTemplate

import java.time.Instant
import java.util.UUID

/**
  * @author Yuriy Tumakha
  */
case class EmailNotification(
  emailTemplateId: EmailTemplate,
  trackerId: UUID,
  sendToEmails: Seq[String],
  templateParams: JsObject,
  callbackUrl: Option[String],
  client: Option[String] = None,
  _id: ObjectId = new ObjectId,
  createdAt: Instant = Instant.now
)

object EmailNotification:

  import uk.gov.hmrc.mongo.play.json.formats.MongoFormats.Implicits.*
  import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits.*

  implicit val format: OFormat[EmailNotification] = Json.format
