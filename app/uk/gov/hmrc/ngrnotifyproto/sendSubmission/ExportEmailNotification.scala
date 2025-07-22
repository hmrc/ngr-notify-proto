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

package uk.gov.hmrc.ngrnotifyproto.sendSubmission

import com.google.inject.ImplementedBy
import play.api.Logging
import uk.gov.hmrc.ngrnotifyproto.config.AppConfig
import uk.gov.hmrc.ngrnotifyproto.connector.EmailConnector
import uk.gov.hmrc.ngrnotifyproto.model.db.EmailNotification
import uk.gov.hmrc.ngrnotifyproto.repository.EmailNotificationRepo

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[ExportConnectedSubmissionsVOA])
trait ExportConnectedSubmissions {
  def exportNow(size: Int)(implicit ec: ExecutionContext): Future[Unit]
}

@Singleton
class ExportConnectedSubmissionsVOA @Inject() (
                                                emailNotificationRepo: EmailNotificationRepo,
                                                clock: Clock,
                                                emailConnector: EmailConnector,
                                                //  audit: ForTCTRAudit,
                                                forConfig: AppConfig
                                              ) extends ExportConnectedSubmissions
  with Logging {

  override def exportNow(size: Int)(implicit ec: ExecutionContext): Future[Unit] =
    emailNotificationRepo.getNotificationsBatch(size).flatMap { emailNotifications =>
      logger.warn(s"Found ${emailNotifications.length} email notifications to export")
      processSequentially(emailNotifications)
    }

  private def processSequentially(emailNotifications: Seq[EmailNotification])(implicit ec: ExecutionContext): Future[Unit] =
    if emailNotifications.isEmpty then Future.unit
    else processNext(emailNotifications.head).flatMap(_ => processSequentially(emailNotifications.tail))

  private def processNext(emailNotification: EmailNotification)(implicit executionContext: ExecutionContext): Future[Unit] =
    if isTooLongInQueue(emailNotification) then
      logger.warn(s"Unable to send email reached end of retry window, ref: ${emailNotification.trackerId}.")
      // TODO Audit removal from queue
      emailNotificationRepo.delete(emailNotification._id).map(_ => ())
      Future.unit
    else
      for (sendTO <- emailNotification.sendToEmails)
        logger.warn(s"Found ${emailNotification.trackerId} with send to ${sendTO} notification to send email")
      // TODO Audit send email
      // TODO Add email connector
        emailConnector.sendSubmissionConfirmation(emailNotification)
      // TODO If Success - remove notification from the DB
      // TODO If fail - send callback to frontend
      Future.unit

  private def isTooLongInQueue(emailNotification: EmailNotification): Boolean =
    emailNotification.createdAt.isBefore(Instant.now(clock).minus(forConfig.retryWindowHours, ChronoUnit.HOURS))

}