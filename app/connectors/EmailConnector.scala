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

package connectors

import config.AppConfig
import models.{EmailDifficulties, EmailResponse, SendEmailRequest}
import play.api.mvc.Request
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmailConnector @Inject()(val httpClient: HttpClientV2, appConfig: AppConfig)(implicit val ec: ExecutionContext) {

  val sendEmailUrl: String = "http://localhost:8300/hmrc/email"

//  def requestEmailToBeSent(emailRequest: SendEmailRequest)(implicit hc: HeaderCarrier, request: Request[_]): Future[EmailResponse] = {
//
//    //    httpClient.post(sendEmailURL, emailRequest)(SendEmailRequest.format, requestEmailToBeSentHttpReads(emailRequest), hc, ec) recover {
//    //      case e: Exception =>
//    //        //TODO log error
//    //        EmailDifficulties
//    //    }
//    //
//    //    httpClient
//    //      .post(url"$upscanInitiateUri")
//    //      .withBody(Json.toJson(request))
//    //      .setHeader(HeaderNames.CONTENT_TYPE -> "application/json")
//    //      .execute[UpscanInitiateResponse]
//    //
//    //
//    //      [SendEmailRequest, EmailResponse]
//    //  }
//  }
}
