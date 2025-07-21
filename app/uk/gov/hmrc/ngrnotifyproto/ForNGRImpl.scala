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

package uk.gov.hmrc.ngrnotifyproto

import org.apache.pekko.actor.ActorSystem
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.ngrnotifyproto.config.AppConfig
import uk.gov.hmrc.ngrnotifyproto.infrastructure.RegularSchedule
import uk.gov.hmrc.ngrnotifyproto.repository.EmailNotificationRepo
import uk.gov.hmrc.ngrnotifyproto.sendSubmission.*

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ForNGRImpl @Inject()(
  actorSystem: ActorSystem,
  tctrConfig: AppConfig,
  systemClock: Clock,
  regularSchedule: RegularSchedule,
  implicit val ec: ExecutionContext,
  mongoLockRepository: MongoLockRepository,
  emailNotificationRepo: EmailNotificationRepo
) {

  import tctrConfig.*

  if submissionExportEnabled then
    val exporter = new ExportConnectedSubmissionsVOA(emailNotificationRepo, systemClock, tctrConfig)
    new ConnectedSubmissionExporter(
      mongoLockRepository,
      exporter,
      exportBatchSize,
      actorSystem.scheduler,
      actorSystem.eventStream,
      regularSchedule
    ).start()
}
