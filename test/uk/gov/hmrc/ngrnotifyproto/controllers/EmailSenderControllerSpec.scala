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

import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.testkit.NoMaterializer
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers, Injecting}
import uk.gov.hmrc.ngrnotifyproto.model.EmailTemplate.ngr_registration_successful

class EmailSenderControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with Injecting:

  private val controller = inject[EmailSenderController]

  given Materializer = NoMaterializer

  "EmailSenderController" should {
    "return 400" in {
      val fakeRequest = FakeRequest("POST", "/")
        .withHeaders("Content-type" -> "application/json;charset=UTF-8")

      val result = controller.sendEmail(ngr_registration_successful.toString)(fakeRequest)
      status(result) shouldBe BAD_REQUEST
    }
  }
