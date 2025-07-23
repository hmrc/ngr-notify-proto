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

import com.google.inject.Provider
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment, Logging}
import uk.gov.hmrc.ngrnotifyproto.infrastructure.{DefaultRegularSchedule, RegularSchedule}

import java.time.Clock
import javax.inject.Singleton

@Singleton
class NGRModule extends Module with Logging {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[?]] =
    Seq(
      bind[RegularSchedule].to[DefaultRegularSchedule],
      bind[NGRImpl].toSelf.eagerly(),
      bind[Clock].toProvider[ClockProvider]
    )
}

class ClockProvider() extends Provider[Clock] {
  override def get(): Clock = Clock.systemUTC()
}
