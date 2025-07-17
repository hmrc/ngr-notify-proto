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
import views.html.HipView
import scala.language.implicitConversions
import scala.language.reflectiveCalls
import testutils.ViewBaseSpec

class HipViewSpec extends ViewBaseSpec {
  val view: HipView = inject[HipView]
  val title = "HIP Testing Hub"
  val heading = "HIP Testing Hub"
  val hint = "Here you can test various HIP endpoints"
  val helloWorldLinkText = "link placeholder"
  val helloWorldLink = "/ngr-notify-proto/hip-hello-world"

  object Selectors {
    val navTitle = "head > title"
    val heading = "div > div > h1"
    val hint = "div > div > p1"
    val helloWorldLink = "div > div > p2 > a"
  }

  "HipView" must {
    val hipView = view()
    implicit val document: Document = Jsoup.parse(hipView.body)
    val htmlApply = view.apply().body
    val htmlRender = view.render(request, messages).body

    "apply must be the same as render" in {
      htmlApply mustBe htmlRender
    }

    "render is not empty" in {
      htmlRender must not be empty
    }

    "show correct title" in {
      elementText(Selectors.navTitle) mustBe title
    }

    "show correct heading" in {
      elementText(Selectors.heading) mustBe heading
    }

    "show correct hint" in {
      elementText(Selectors.hint) mustBe hint
    }

    "show link to call Hello World" in {
      elementText(Selectors.helloWorldLink) mustBe helloWorldLinkText
    }

    "the Hello World link should be correct" in {
      document.select(Selectors.helloWorldLink).attr("href") mustBe helloWorldLink
    }
  }
}
