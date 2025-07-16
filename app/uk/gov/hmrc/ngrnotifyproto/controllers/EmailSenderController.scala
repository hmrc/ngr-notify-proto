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

import play.api.{Configuration, Logging}
import play.api.libs.json.*
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.ngrnotifyproto.model.EmailTemplate.*
import uk.gov.hmrc.ngrnotifyproto.model.email.*
import uk.gov.hmrc.ngrnotifyproto.model.response.{ApiFailure, ApiSuccess}
import uk.gov.hmrc.ngrnotifyproto.model.{EmailTemplate, OperatorNotification, UserNotification}
import uk.gov.hmrc.ngrnotifyproto.repository.EmailNotificationRepo
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.collection.Seq
import scala.collection.immutable.ArraySeq
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Yuriy Tumakha
  */
@Singleton
class EmailSenderController @Inject() (
  configuration: Configuration,
  emailNotificationRepo: EmailNotificationRepo,
  cc: ControllerComponents
)(using
  ec: ExecutionContext
) extends BackendController(cc)
    with Logging:

  private val operatorEmail = configuration.get[String]("operator.email")

  def sendEmail(emailTemplateId: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val emailTemplate = EmailTemplate.valueOf(emailTemplateId)

    emailTemplate match {
      case `ngr_registration_successful`            => parse[RegistrationSuccessful]
      case `ngr_registration_operator_notification` => parse[RegistrationOperatorNotification]
      case `ngr_add_property_request_sent`          => parse[AddPropertyRequestSent]
    } match {
      case Right((email, params)) => sendEmail(emailTemplate, email, params)
      case Left(result)           => Future.successful(result)
    }
  }

  private def parse[T](using
    request: Request[JsValue],
    rds: Reads[T],
    tjs: OWrites[T]
  ): Either[Result, (String, JsObject)] =
    request.body.validate[T] match {
      case JsSuccess(notification: T, _)                            =>
        val email          = notification match {
          case userNotification: UserNotification => userNotification.email
          case _: OperatorNotification            => operatorEmail
        }
        val templateParams = Json.toJsObject[T](notification)

        Right(email -> templateParams)
      case JsError(errors: Seq[(JsPath, Seq[JsonValidationError])]) =>
        val failures = errors.map { case (jsPath, jsonErrors) =>
          ApiFailure(
            "JSON_VALIDATION_ERROR",
            s"$jsPath <- ${jsonErrors.map(printValidationError).mkString(" | ")}"
          )
        }
        Left(BadRequest(Json.toJson(failures)))
    }

  private def printValidationError(error: JsonValidationError): String =
    val msgArgs = error.args match {
      case arraySeq: ArraySeq[?] => arraySeq.mkString(", ")
      case any                   => any.toString
    }
    error.message + msgArgs

  private def sendEmail[T](emailTemplate: EmailTemplate, email: String, templateParams: JsObject): Future[Result] =
    logger.info(s"\nSend $emailTemplate to $email. Template params: $templateParams")

    emailNotificationRepo
      .save(emailTemplate, email, templateParams)
      .map { id =>
        logger.info(s"\nSaved email notification with ID = $id")
        Created(Json.toJsObject(ApiSuccess("Success", "Email dispatch task successfully created.")))
      }
      .recover { error =>
        logger.error("Error on save to Mongo", error)
        InternalServerError(
          Json.toJson(
            Seq(ApiFailure("MONGO_DB_ERROR", error.getMessage))
          )
        )
      }
