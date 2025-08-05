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

package uk.gov.hmrc.ngrnotifyproto.model

import play.api.libs.json.*
import play.api.libs.json.JsonNaming.SnakeCase


given JsonConfiguration = JsonConfiguration(SnakeCase)

given Reads[Int | String] = new Reads[Int | String] {
  def reads(jsValue: JsValue): JsResult[Int | String] =
    jsValue match
      case JsNumber(num) => JsSuccess(num.toInt)
      case JsString(str) => JsSuccess(str)
      case _ => JsError("Expected an Int or String")
}

given Writes[Int | String] = new Writes[Int | String] {
  override def writes(intOrString: Int | String): JsValue =
    intOrString match
      case int: Int => JsNumber(int)
      case str: String => JsString(str)
}


// TODO Give more JSON Reads (and Writes) for other Scala types here ...