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

package uk.gov.hmrc.ngrnotifyproto.controllers

import play.api.mvc.{Action, AnyContent, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.libs.json.JsValue
import uk.gov.hmrc.ngrnotifyproto.connectors.HipConnector

@Singleton()
class HipController @Inject() (hipConnector: HipConnector, cc: ControllerComponents)(implicit
  executionContext: ExecutionContext
) extends BackendController(cc) {

  def hipHelloWorld(): Action[AnyContent] = Action.async { implicit request =>
    val clientId: String                              = request.headers.get("Client-Id").getOrElse("")
    val clientSecret: String                          = request.headers.get("Client-Secret").getOrElse("")
    val eventuallyHelloWorldResponse: Future[JsValue] = hipConnector.callHelloWorld(clientId, clientSecret)

    eventuallyHelloWorldResponse.map(helloWorldResponse => Ok(s"Response was: $helloWorldResponse"))
  }
}
