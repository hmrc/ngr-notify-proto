/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.ngrnotifyproto.exporter

import com.google.inject.ImplementedBy
import play.api.Logging
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.ngrnotifyproto.config.{AppConfig, NGRAudit}
import uk.gov.hmrc.ngrnotifyproto.connector.{CallbackConnector, EmailConnector}
import uk.gov.hmrc.ngrnotifyproto.model.ErrorCode.*
import uk.gov.hmrc.ngrnotifyproto.model.db.EmailNotification
import uk.gov.hmrc.ngrnotifyproto.model.response.HmrcSendEmailResponse
import uk.gov.hmrc.ngrnotifyproto.repository.EmailNotificationRepo
import uk.gov.hmrc.ngrnotifyproto.model.EmailTemplate.*

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@ImplementedBy(classOf[ExportEmailNotificationVOA])
trait ExportEmailNotification {
  def exportNow(size: Int): Future[Unit]
}

@Singleton
class ExportEmailNotificationVOA @Inject() (    emailNotificationRepo: EmailNotificationRepo,
                                                clock: Clock,
                                                audit: NGRAudit,
                                                emailConnector: EmailConnector,
                                                callbackConnector: CallbackConnector,
                                                forConfig: AppConfig
                                              )(implicit  ec: ExecutionContext) extends ExportEmailNotification
  with Logging {

  override def exportNow(size: Int): Future[Unit] =
    emailNotificationRepo.getNotificationsBatch(size).flatMap { emailNotifications =>
      logger.warn(s"Found ${emailNotifications.length} email notifications to export")
      processSequentially(emailNotifications)
    }

  private def processSequentially(emailNotifications: Seq[EmailNotification]): Future[Unit] =
    if emailNotifications.isEmpty then Future.unit
    else processNext(emailNotifications.head).flatMap(_ => processSequentially(emailNotifications.tail))

  private def processNext(
    emailNotification: EmailNotification
  )(implicit executionContext: ExecutionContext): Future[Unit] =
    if isTooLongInQueue(emailNotification) then
      logger.warn(s"Unable to send email reached end of retry window, ref: ${emailNotification.trackerId}.")
      // TODO Audit removal from queue
      emailNotificationRepo.delete(emailNotification._id).map(_ => ())
    else
      logger.warn(s"Found ${emailNotification.trackerId} notification with send to ${emailNotification.sendToEmails}")        emailConnector
          .sendEmailNotification(emailNotification)
          .flatMap { res =>
            res.status match {
              case OK | ACCEPTED =>
                auditActionSuccessful(emailNotification)
                emailNotificationRepo.delete(emailNotification._id).map(_ => ())
              case BAD_REQUEST   =>
                auditActionFailed(emailNotification, BAD_REQUEST_BODY.toString, parseBadRequest(res.body))
                callbackConnector.callbackOnFailure(emailNotification, BAD_REQUEST_BODY, parseBadRequest(res.body))
              case _ =>
                auditActionFailed(emailNotification, res.status.toString, res.body)
                callbackConnector.callbackOnFailure(
                  emailNotification,
                  WRONG_RESPONSE_STATUS,
                  s"Send email to user FAILED: ${res.status} ${res.body}"
                )
            }
          }
          .recoverWith { error =>
            auditActionFailed(emailNotification, "ACTION_FAILED", error.getMessage)
            callbackConnector.callbackOnFailure(emailNotification, error)
          }
        emailType(emailNotification)
        Future.unit

  private def parseBadRequest(body: String): String =
    Try {
      val hmrcResponse = Json.parse(body).as[HmrcSendEmailResponse]
      Seq(hmrcResponse.message, hmrcResponse.reason).flatten.mkString(". ")
    }.getOrElse(body)

  private def isTooLongInQueue(emailNotification: EmailNotification): Boolean =
    emailNotification.createdAt.isBefore(Instant.now(clock).minus(forConfig.retryWindowHours, ChronoUnit.HOURS))

  private def emailType(emailNotification: EmailNotification): Unit =
    emailNotification.emailTemplateId match {
      case `ngr_registration_successful`   => auditSubmissionEvent("Email sent: sendRegistrationEmail", emailNotification)
      case `ngr_add_property_request_sent` => auditSubmissionEvent("Email sent: sendPropertyLinkingEmail", emailNotification)
    }

  def eventType(emailNotification: EmailNotification): String =
    emailNotification.emailTemplateId match {
      case `ngr_registration_successful` => "sendRegistrationEmail"
      case `ngr_add_property_request_sent` => "sendPropertyLinkingEmail"
    }

  private def auditSubmissionEvent(eventType: String, emailNotification: EmailNotification): Unit =
    audit(
      eventType,
      Json.obj(
        "emailId" -> emailNotification.emailTemplateId,
        "data"      -> emailNotification.templateParams,
        "result" -> emailNotification
      )
    )

  def auditActionSuccessful(emailNotification: EmailNotification): Unit = {
    val outcome = Json.obj("isSuccessful" -> true)
    audit(
      eventType,
      Json.obj(
        "trackerId" -> emailNotification.trackerId,
        "emailId" -> emailNotification.emailTemplateId,
        "notification" -> emailNotification,
        "outcome" -> outcome
      )
    )
  }

  def auditActionFailed(
                     emailNotification: EmailNotification,
                     failureCategory: String,
                     failureReason: String
                   ): Unit = {
    val outcome = Json.obj(
      "isSuccessful" -> false,
      "failureCategory" -> failureCategory,
      "failureReason" -> failureReason
    )
    audit(
      eventType,
      Json.obj(
        "emailId" -> emailNotification.emailTemplateId,
        "notification" -> emailNotification,
        "outcome" -> outcome
      )
    )


  }
}