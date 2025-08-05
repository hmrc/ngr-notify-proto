package uk.gov.hmrc.ngrnotifyproto.model

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import uk.gov.hmrc.ngrnotifyproto.model.bridge.*
import uk.gov.hmrc.ngrnotifyproto.model.bridge.TitleCommon.*

class JobDescriptionSpec extends AnyWordSpec with Matchers:

  "The ngr-notify service" when {
    "receiving the incoming JSON text" should {
      "fully deserialize it into our Scala model" in {

        // Receive the incoming text,
        // parse it into an abstract syntax tree
        // and then convert it to our model
        val incomingText  = loadText("incoming_job.json")
        val absSyntaxTree  = Json.parse(incomingText)
        val actualModel = Json.fromJson[JobDescription](absSyntaxTree)

        // Assert the actual model is the same as the expected one
        actualModel mustBe a[JsSuccess[JobDescription]]
        actualModel.get mustBe jobDescr
      }
    }
    "transmitting the outgoing JSON text" should {
      "fully serialize it from our Scala model" in {

        // Turn our model into a JSON abstract syntax tree
        // and the pretty print it to text
        val abst = Json.toJson(jobDescr)
        val actualText = Json.prettyPrint(abst)

        // Assert the actual text is the same as the expected one
        val expectedText = loadText("outgoing_job.json")
        actualText mustBe expectedText
      }
    }
  }


  val jobDescr = JobDescription(
    id = Some("one-two-three"),
    name = "Register Ratepayer",
    compartments = Compartments(
      persons = List(
        Person(
          id = None,
          idx = "1.2.1",
          data = Data(
            names = Names(
              titleCommon = Mr,
            ),
            communications = Communications(
              email = Some("somebody@example.com")
            )
          )
        )
      )
    )
  )


  def loadText(resource: String): String =
    scala.io.Source.fromResource(resource).getLines().mkString
