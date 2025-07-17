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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.data.Forms.{text, tuple}

import scala.language.implicitConversions
import scala.language.reflectiveCalls
import testutils.ViewBaseSpec
import views.html.HipHubView

class HipHubViewSpec extends ViewBaseSpec {
  val view: HipHubView = inject[HipHubView]
  val heading = "NGR Notify Hub Page"
  val hint = "Here you can test various ngr-notify endpoints"
  val helloWorldLinkText = "Hello World Test"
  val helloWorldLink = "/ngr-notify-proto/hip-hello-world"

  val helloWorldForm = Form(
    tuple(
      "clientId" -> text,
      "clientSecret" -> text
    )
  )

  object Selectors {
    val heading = "div > div > h1"
    val hint = "div > div > p1"
    val helloWorldLinkText = "div > div > p2"
    val helloWorldLink = "div > div > p2 > a"
  }

  "HipView" must {
    val hipView = view(helloWorldForm)
    implicit val document: Document = Jsoup.parse(hipView.body)
    val htmlApply = view.apply(helloWorldForm).body
    val htmlRender = view.render(helloWorldForm, request, messages).body

    "apply must be the same as render" in {
      htmlApply mustBe htmlRender
    }

    "render is not empty" in {
      htmlRender must not be empty
    }

    "show correct heading" in {
      elementText(Selectors.heading) mustBe heading
    }

    "show correct hint" in {
      elementText(Selectors.hint) mustBe hint
    }

    "show link to call Hello World" in {
      elementText(Selectors.helloWorldLinkText) mustBe helloWorldLinkText
    }

    "the Hello World link should be correct" in {
      document.select(Selectors.helloWorldLink).attr("href") mustBe helloWorldLink
    }
  }
}
