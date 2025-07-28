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

/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.ngrnotifyproto.connector

import play.api.Logging
import play.api.http.Status.{ACCEPTED, OK}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import uk.gov.hmrc.ngrnotifyproto.model.ErrorCode
import uk.gov.hmrc.ngrnotifyproto.model.ErrorCode.*
import uk.gov.hmrc.ngrnotifyproto.model.db.EmailNotification
import uk.gov.hmrc.ngrnotifyproto.model.response.{ActionCallback, ApiFailure}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Yuriy Tumakha
  */
@Singleton
class CallbackConnector @Inject() (
  httpClientV2: HttpClientV2
)(implicit
  ec: ExecutionContext
) extends Logging:

  def callbackOnFailure(notification: EmailNotification, error: Throwable): Future[Unit] =
    callbackOnFailure(notification, Seq(ApiFailure(ACTION_FAILED, error.getMessage)))

  def callbackOnFailure(notification: EmailNotification, code: ErrorCode, reason: String): Future[Unit] =
    callbackOnFailure(notification, Seq(ApiFailure(code, reason)))

  def callbackOnFailure(notification: EmailNotification, failures: Seq[ApiFailure]): Future[Unit] =
    val json = Json.toJsObject(
      ActionCallback(notification.trackerId, notification.emailTemplateId.toString, failures)
    )

    given HeaderCarrier = HeaderCarrier()

    notification.callbackUrl
      .map { callbackUrl =>
        httpClientV2
          .post(url"$callbackUrl")
          .withBody(json)
          .setHeader("Content-Type" -> "application/json")
          .execute[HttpResponse]
          .map { res =>
            res.status match {
              case OK | ACCEPTED => logger.info(s"Callback successful: ${res.status}")
              case _             => logger.error(s"Callback FAILED: ${res.status} ${res.body}")
            }
          }
          .recoverWith { case e: Exception =>
            logger.error(s"Callback FAILED: ${e.getMessage}", e)
            Future.failed(e)
          }
      }
      .getOrElse(Future.unit)
