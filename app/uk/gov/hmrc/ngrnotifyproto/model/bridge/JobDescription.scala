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

package uk.gov.hmrc.ngrnotifyproto.model.bridge

/**
  * Represents a Bridge API job description
  */
case class JobDescription(
  id: Option[Int | String],
  name: String,
  // category: Category,
  compartments: Compartments
)

object JobDescription:
  import play.api.libs.json.*
  import uk.gov.hmrc.ngrnotifyproto.model.given

  given Format[JobDescription] = Json.format
  // See https://docs.scala-lang.org/scala3/book/ca-context-parameters.html#given-instances-implicit-definitions-in-scala-2
