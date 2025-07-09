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

import play.api.Logging
import play.api.libs.json.*
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.ngrnotifyproto.model.EmailTemplate.*
import uk.gov.hmrc.ngrnotifyproto.model.email.*
import uk.gov.hmrc.ngrnotifyproto.model.{EmailTemplate, OperatorNotification, UserNotification}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.collection.Seq
import scala.concurrent.Future

/**
  * @author Yuriy Tumakha
  */
@Singleton
class EmailSenderController @Inject() (cc: ControllerComponents) extends BackendController(cc) with Logging:

  private val operatorEmail = "operator@email.com" // TODO: get from config

  def sendEmail(emailTemplateId: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    EmailTemplate.valueOf(emailTemplateId) match {
      case `ngr_registration_successful`            =>
        sendEmail(ngr_registration_successful, parse[RegistrationSuccessful])
      case `ngr_registration_operator_notification` =>
        sendEmail(ngr_registration_operator_notification, parse[RegistrationOperatorNotification])
      case `ngr_add_property_request_sent`          =>
        sendEmail(ngr_add_property_request_sent, parse[AddPropertyRequestSent])
    }
  }

  private def parse[T](using request: Request[JsValue], rds: Reads[T]): T =
    request.body.validate[T] match {
      case JsSuccess(v: T, _)                                       => v
      case JsError(errors: Seq[(JsPath, Seq[JsonValidationError])]) => throw new BadRequestException(errors.toString)
    }

  private def sendEmail(emailTemplate: EmailTemplate, userNotification: UserNotification): Future[Result] =
    sendEmail(emailTemplate, userNotification.user.email, userNotification.toParams)

  private def sendEmail(emailTemplate: EmailTemplate, operatorNotification: OperatorNotification): Future[Result] =
    sendEmail(emailTemplate, operatorEmail, operatorNotification.toParams)

  private def sendEmail(emailTemplate: EmailTemplate, email: String, parametersJson: JsObject): Future[Result] =
    logger.info(s"Send $emailTemplate to $email. Template params: $parametersJson")

    Future.successful(
      Created(""""Email dispatch task successfully created."""")
    )
