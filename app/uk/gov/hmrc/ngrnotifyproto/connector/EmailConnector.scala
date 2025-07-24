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
import play.api.i18n.Lang
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import uk.gov.hmrc.ngrnotifyproto.model.db.EmailNotification
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.Locale
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Yuriy Tumakha
  */
@Singleton
class EmailConnector @Inject()(
  servicesConfig: ServicesConfig,
  httpClientV2: HttpClientV2,
)(implicit
  ec: ExecutionContext
) extends Logging {

  private val emailServiceBaseUrl = servicesConfig.baseUrl("email")
  private val sendEmailURL        = url"$emailServiceBaseUrl/hmrc/email"
  private val englishLang         = Lang(Locale.UK)
  private val langMap             = Map(
    "en" -> englishLang,
    "cy" -> Lang("cy")
  ).withDefaultValue(englishLang)

  private val ngr_registration_successful        = "ngr_registration_successful"

  def sendEmailNotification(emailNotification: EmailNotification): Future[HttpResponse] =
    val parameters = emailNotification.templateParams
    val emailTemplateId = emailNotification.emailTemplateId
    val emails = emailNotification.sendToEmails
    sendEmail(emails, emailTemplateId.toString, parameters)
  

  private def sendEmail(emails: Seq[String], templateId: String, parametersJson: JsObject): Future[HttpResponse] = {
    val json = Json.obj(
      "to" -> emails,
      "templateId" -> templateId,
      "parameters" -> parametersJson
    )
    val headers = Seq("Content-Type" -> "application/json")
    implicit val hc: HeaderCarrier          = HeaderCarrier()
    
    httpClientV2
      .post(sendEmailURL)
      .withBody(json)
      .setHeader(headers *)
      .execute[HttpResponse]
      .map { res =>
        res.status match {
          case OK | ACCEPTED => logger.info(s"Send email to user successful: ${res.status}")
          case _ => logger.error(s"Send email to user FAILED: ${res.status} ${res.body}")
        }
        res
      }
      .recoverWith { case e: Exception =>
        logger.error(s"Send email to user FAILED: ${e.getMessage}", e)
        Future.failed(e)
      }
  }

}
