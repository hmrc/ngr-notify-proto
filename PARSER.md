# Parsing strategies

The `JsonDeserializerTest` demonstrates how to deserialize the JSON text replied by the Bridge API into our Scala model classes, by using the underlying PlayFramework's JSON library.

In general, the deserialization process can be summarized as follows:

```text
                      1                             2
  ┌─────────┐    ╔═════════╗    ┌─────────┐    ╔═════════╗    ┌─────────┐
  │  text   │ -→ ║  parse  ║ -→ │  abst   │ -→ ║ convert ║ -→ │  model  │
  └─────────┘    ╚═════════╝    └─────────┘    ╚═════════╝    └─────────┘
```

where, the incoming text gets parsed into an abstract syntax tree (AbST), able to generically represent the incoming data with its structural relationships, which is then converted into our model classes.

## model
Our model classes are defined in the `model` package, and they represent the data structures we need to work with the Bridge API. Those structures are defined by the Bridge API engineers via the attached [bridge-schema.json](./test/resources/bridge-schema.json) file.

### size
By reading the `bridge-schema.json` file, we can see that the model classes are quite large, with many fields and nested structures. This is because the Bridge API provides a lot of information in its responses, which we may not always need.

> DISCUSSION:  
> We can adopt either of the following alternative strategies to deserialize the JSON text into our model classes:
> 
>   1. Full deserialization into a fully blown (superfluous) case classes model.
>   2. Partial deserialization into an essential (minimal) case classes model.
>


### types
By reading the `bridge-schema.json` file, we can also see that, while the most of the properties can be easily turned into 

* Scala simple types (such, `String`, `Int`, `Boolean`), 
* and Scala collection types (such as `List`, `Option`), 
* and Scala custom case classes

a few others have been defined as _"union types"_ and _"enumerated types"_, which require some additional consideration. 


#### union types
The Bridge API schema defines a few properties that can have multiple types, such as the `id` property, which can be either `null`, an integer, or a string. Following is an excerpt from the `bridge-schema.json` definition file:

```json
{
  "properties": {
    "id": {
      "oneOf": [
        {
          "const": null
        },
        {
          "type": "integer",
          "minimum": 1,
          "maximum": 99999999
        },
        {
          "type": "string",
          "minLength": 36,
          "maxLength": 36
        }
      ]
    }
  }
}
```

According to the official Scala 3 reference (see https://docs.scala-lang.org/scala3/reference/new-types/union-types.html) the most appropriate Scala type able to conveniently represent the above JSON property can be expressed as a Scala 3 union type, such as:

```scala
val id: Option[Integer | String]
```

> NOTE  
> The underlying PlayFramework's JSON library does not provide a built-in support for Scala 3 union types. Luckily, we can easily implement a custom solution for it (see below).


#### enumerated types
The Bridge API schema defines a few enumerated types, such as the `title_common` property, which can have one of the following values: `null`, `"Mr"`, `"Ms"`, `"Mrs"` or `"Miss"`. Following is an excerpt from the `bridge-schema.json` definition file:

```json
{
  "properties": {
    "title_common": {
      "oneOf": [
        {
          "const": null
        },
        {
          "enum": [
            "Mr",
            "Ms",
            "Mrs",
            "Miss"
          ]
        }
      ]
    }
  }
}
```

According to the official Scala 3 reference (see https://docs.scala-lang.org/scala3/reference/enums/enums.html) the most appropriate Scala type able to conveniently represent the above JSON property can be expressed as a Scala 3 enum type, such as:

```scala
enum TitleCommon:
  case Mr, Ms, Mrs, Miss
```

> NOTE  
> The underlying PlayFramework's JSON library does not provide a built-in support for Scala 3 enum types. Luckily, we can easily implement a custom solution for it (see below).

 
## deserialization
The following Scala code snippet shows how the incoming JSON text gets deserialized into our model classes, upon processing the incoming HTTP request in a PlayFramework controller:

```scala
class MyController extends BaseController {
  def myAction: Action[MyModel] = Action { (request: Request[MyModel]) =>
    val body: MyModel = request.body
    // the incoming request body text is automatically
    // deserialized into the MyModel case class
    // ...
  }
}
```

### parse
The first step that Playframework takes in the deserialization process is to parse the incoming JSON text into an abstract syntax tree (AbST). This is done (_"under the hood"_) by the PlayFramework's JSON library, which takes the text as input and returns the `JsValue` object representing the abstract syntax tree.

```scala
import play.api.libs.json._
val abst: JsValue = Json.parse(jsonText)
```
    

### convert
The second step is to convert the abstract syntax tree (AbST) into our model classes. This is done in two alternative ways:

1. automatic conversion, where the library automatically maps the branches of the JSON abstract syntax tree into the Scala case class properties.

2. manual conversion, where we traverse the branches of the JSON abstract syntax tree for specific properties (or types) and then construct the corresponding model classes.


#### automatic
The PlayFramework's JSON library can automatically convert the JSON abstract syntax tree into our model classes. That works as long as we give JSON `Reads` instances for each of our model class. 

This is usually done by using the `Json.reads` macro, able to generates all the necessary code at build time, based on the case class definition. We can also give the `Reads` instances manually, as shown below for the union type `Int | String`:

```scala
package uk.gov.hmrc.ngrnotify.model

import play.api.libs.json.*

// Custom model reader (usually in companion object)
object MyModel:
  given Reads[MyModel] = Json.reads
  
// Custom Scala 3 union reader
given Reads[Int | String] = new Reads[Int | String] {
  def reads(jsValue: JsValue): JsResult[Int | String] =
    jsValue match
      case JsNumber(num) => JsSuccess(num.toInt)
      case JsString(str) => JsSuccess(str)
      case _             => JsError("Expected a number or string")
}

// Custom Scala 3 enum reader
given Reads[TitleCommon] = new Reads[TitleCommon] {
  def reads(jsValue: JsValue): JsResult[TitleCommon] =
    jsValue match
      case JsString(str) => Try(TitleCommon.valueOf(str)).map(JsSuccess(_)).getOrElse(JsError("Invalid title_common value"))
      case _             => JsError("Expected enumerated value")
}
```

#### manual conversion
Once the JSON abstract syntax tree (AbST) has been parsed, we can manually traverse it to extract specific properties and convert them into our model classes. 

```scala
val abst = Json.parse(text)

val model = JobDescription(
  id = (abst \ "id").asIntegerOrString,
  name = (abst \ "name").as[String]
)
```