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

package controllers

import connectors.HipConnector
import models.HelloWorldResponse
import play.api.mvc.{Action, AnyContent, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import views.html.{HelloWorldView, HipHubView}
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport


@Singleton()
class HipController @Inject()(hipConnector: HipConnector,
                              hipHubView: HipHubView,
                              helloWorldView: HelloWorldView,
                              cc: ControllerComponents)(implicit executionContext: ExecutionContext) extends BackendController(cc) with I18nSupport{
  val helloWorldForm = Form(
    tuple(
      "clientId" -> text,
      "clientSecret" -> text
    )
  )

  def showTestPage(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(hipHubView(helloWorldForm)))
  }

  def hipHelloWorld(): Action[AnyContent] = Action.async { implicit request =>
        val clientId: String = request.headers.get("Client-Id").getOrElse("")
        val clientSecret: String = request.headers.get("Client-Secret").getOrElse("")
        val eventuallyHelloWorldResponse: Future[HelloWorldResponse] = hipConnector.callHelloWorld(clientId, clientSecret)

        eventuallyHelloWorldResponse.map(helloWorldResponse =>
          Ok(s"Response was: ${helloWorldResponse.name}"))
  }

  def hipHelloWorldForm(): Action[AnyContent] = Action.async { implicit request =>
    helloWorldForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Ok(hipHubView(formWithErrors)))
      },
      { case (clientId, clientSecret) =>
        val eventuallyHelloWorldResponse: Future[HelloWorldResponse] = hipConnector.callHelloWorld(clientId, clientSecret)
        eventuallyHelloWorldResponse.map(helloWorldResponse => println(helloWorldResponse.name))
        eventuallyHelloWorldResponse.map(helloWorldResponse =>
          Ok(helloWorldView(helloWorldResponse.name))
        )
      }
    )
  }

//  def hipHelloWorldOrig(): Action[AnyContent] = Action.async { implicit request =>
//    val eventuallyHelloWorldResponse: Future[HelloWorldResponse] = hipConnector.callHelloWorld
//    eventuallyHelloWorldResponse.map(helloWorldResponse => println(helloWorldResponse.name))
//
//    eventuallyHelloWorldResponse.map(helloWorldResponse =>
//      Ok(helloWorldView(helloWorldResponse.name))
//    )
//  }
}