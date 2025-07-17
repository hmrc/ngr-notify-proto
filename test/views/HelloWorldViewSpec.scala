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
import views.html.HelloWorldView
import scala.language.implicitConversions
import scala.language.reflectiveCalls
import testutils.ViewBaseSpec

class HelloWorldViewSpec extends ViewBaseSpec {
  val view: HelloWorldView = inject[HelloWorldView]
  val heading = "Hello World Page"
  val helloWorldText = "Hello World"

  object Selectors {
    val heading = "div > div > h1"
    val helloWorld = "div > div > p1"
  }

  "HipView" must {
    val hipView = view(helloWorldText)
    implicit val document: Document = Jsoup.parse(hipView.body)
    val htmlApply = view.apply(helloWorldText).body
    val htmlRender = view.render(helloWorldText, request).body

    "apply must be the same as render" in {
      htmlApply mustBe htmlRender
    }

    "render is not empty" in {
      htmlRender must not be empty
    }

    "show correct heading" in {
      elementText(Selectors.heading) mustBe heading
    }

    "show correct text" in {
      elementText(Selectors.helloWorld) mustBe helloWorldText
    }
  }
}
