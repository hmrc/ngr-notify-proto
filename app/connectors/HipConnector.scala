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
import models.{HelloWorldResponse, HipRequest, HipResponse}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.writeableOf_JsValue
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.AuthHeaderBuilder

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HipConnector @Inject()(httpClient: HttpClientV2, appConfig: AppConfig)(implicit ec: ExecutionContext) {
//  private val hipURL: URL = url"xxx"
  private val hipRequest: HipRequest = HipRequest("Test Body")

  def callHelloWorld(clientId: String, clientSecret: String)(implicit headerCarrier: HeaderCarrier): Future[JsValue] = {
    val hipHelloWorldURL: URL = url"https://hip.ws.ibt.hmrc.gov.uk/demo/hello-world"
    val authHeader: String = AuthHeaderBuilder.buildAuthHeader(clientId, clientSecret)

    httpClient
      .get(hipHelloWorldURL)
      .setHeader(HeaderNames.AUTHORIZATION -> authHeader)
      .setHeader(HeaderNames.ACCEPT -> "application/json")
      .setHeader(HeaderNames.CONTENT_TYPE -> "application/json")
      .setHeader("OriginatorId" -> "NGR")
      .execute[JsValue]
  }

//  def callHelloWorldOrig(implicit headerCarrier: HeaderCarrier): Future[HelloWorldResponse] = {
//    val hipHelloWorldURL: URL = url"https://hip.ws.ibt.hmrc.gov.uk/demo/hello-world"
//
////    headerCarrier.withExtraHeaders(
////      HeaderNames.AUTHORIZATION -> AuthHeaderBuilder.buildAuthHeader,
////      HeaderNames.ACCEPT -> "application/json",
////      HeaderNames.CONTENT_TYPE -> "application/json",
////      "OriginatorId" -> "NGR")
//    httpClient
//      .get(hipHelloWorldURL)
//      .setHeader(HeaderNames.AUTHORIZATION -> AuthHeaderBuilder.buildAuthHeader)
//      .setHeader(HeaderNames.ACCEPT -> "application/json")
//      .setHeader(HeaderNames.CONTENT_TYPE -> "application/json")
//      .setHeader("OriginatorId" -> "NGR")
//      .execute[HelloWorldResponse]
//  }

//  def post(implicit headerCarrier: HeaderCarrier): Future[HipResponse] = {
//    httpClient
//      .post("hipURL")
//      .withBody(Json.toJson(hipRequest))
//      .setHeader(HeaderNames.CONTENT_TYPE -> "application/json")
//      .execute[HipResponse]
//  }
}
