// Copyright (C) 2015 Sam Halliday
// License: http://www.gnu.org/licenses/gpl.html
package s4m.exercise3.specs

import org.scalatest._
import spray.json._
import java.util.UUID

import s4m.json._
import s4m.exercise3.api._
import s4m.exercise3.formats._

class FamilyFormatsSpec extends FlatSpec with Matchers {
  import ExamplesFormats._

  "FamilyFormats" should "support case objects" in {
    roundtrip(Baz, "{}")
  }

  it should "support symbols, provided by base spray-json" in {
    // any use of Symbol seems to need the hack above. Known caveat.
    roundtrip('foo, """"foo"""")
  }

  it should "support case classes" in {
    roundtrip(Foo("foo"), """{"s":"foo"}""")
    roundtrip(Bar(), "{}")
  }

  it should "fail when missing required fields" in {
    intercept[NoSuchElementException] {
      """{}""".parseJson.convertTo[Foo]
    }
  }

  it should "support simple sealed families" in {
    roundtrip(Foo("foo"): SimpleTrait, """{"type":"Foo","s":"foo"}""")
    roundtrip(Bar(): SimpleTrait, """{"type":"Bar"}""")
    roundtrip(Baz: SimpleTrait, """{"type":"Baz"}""")
    roundtrip(Fuzz: SimpleTrait, """{"type":"Fuzz"}""")
  }

  it should "fail when missing required coproduct disambiguators" in {
    intercept[NoSuchElementException] {
      """{"s":"foo"}""".parseJson.convertTo[SimpleTrait]
    }
  }

  // EXERCISE 3.2
  // it should "support custom coproduct keys" in {
  //   roundtrip(Fuzz: SubTrait, """{"hint":"Fuzz"}""")
  //   roundtrip(Buzz: Spiel, """{"Buzz":{}}""")
  // }

  // EXERCISE 3.2
  // it should "support custom coproduct field naming rules" in {
  //   roundtrip(Plooba("poo"): Cloda, """{"TYPE":"PLOOBA","THING":"poo"}""")
  // }

  // EXERCISE 3.1
  // it should "support custom product field naming rules" in {
  //   roundtrip(Plooba("poo"), """{"THING":"poo"}""")
  // }

  // EXERCISE 3.3
  // it should "support optional parameters on case classes" in {
  //   roundtrip(Faz(Some("meh")), """{"o":"meh"}""") // note uses optionFormat, not familyFormat
  //   roundtrip(Faz(None), "{}") // should be omitted, not "null"
  // }

  // it should "support custom missing value rules" in {
  //   roundtrip(Huey(Quack, None), """{"duck":null,"witch":null}""")
  //   roundtrip(Dewey(Quack, None), """{"duck":null}""")
  //   roundtrip(Louie(Quack, None), """{}""")

  //   val json = """{"duck":null,"witch":null}""".parseJson
  //   json.convertTo[Huey] shouldBe Huey(Quack, None)
  //   json.convertTo[Dewey] shouldBe Dewey(Quack, Some(Quack))
  //   json.convertTo[Louie] shouldBe Louie(Quack, None)
  // }

  // it should "fail when missing required (null) values" in {
  //   val noduck = """{"witch":null}""".parseJson
  //   val nowitch = """{"duck":null}""".parseJson

  //   intercept[DeserializationException] {
  //     noduck.convertTo[Huey]
  //   }
  //   intercept[DeserializationException] {
  //     noduck.convertTo[Dewey]
  //   }
  //   noduck.convertTo[Louie] shouldBe Louie(Quack, None)

  //   intercept[DeserializationException] {
  //     nowitch.convertTo[Huey]
  //   }
  //   nowitch.convertTo[Dewey] shouldBe Dewey(Quack, None)
  //   nowitch.convertTo[Louie] shouldBe Louie(Quack, None)
  // }

  it should "prefer user customisable JsonFormats" in {
    roundtrip(Schpugel("foo"), """"foo"""")
  }

  it should "prefer user customisable RootJsonFormats" in {
    roundtrip(Smim("foo"), """{"smim":"foo"}""")
  }

  it should "prefer non-trivial user customisable JsonFormats" in {
    // uncomment the next line as an alternative to the redefinition of FloomaFormat
    // def familyFormat = ???
    roundtrip(Flooma("aha"): Smash, """{"flooma":"aha"}""") // via our JsonFormat[Smash]
  }

  it should "fail to compile when a member of the family cannot be serialised" in {
    // this is an example of when this library can be very
    // frustrating. The compiler error when an implicit cannot be
    // created is always the least specific type. Here we're missing a
    // formatter for UUIDs but the compiler warns about Primates. If
    // we narrow it down to Hominidae it also errors... but finding
    // these problems is a human driven search game.

    shapeless.test.illTyped(
      """roundtrip(Hominidae(UUID.randomUUID): Primates)""",
      ".*could not find implicit value for evidence parameter of type spray.json.JsonFormat\\[s4m.exercise3.api.Primates\\].*"
    )

    shapeless.test.illTyped(
      """roundtrip(Hominidae(UUID.randomUUID))""",
      ".*could not find implicit value for evidence parameter of type spray.json.JsonFormat\\[s4m.exercise3.api.Hominidae\\].*"
    )
  }

  def roundtrip[T: JsonFormat](value: T, via: Option[String] = None): Unit = {
    val json = value.toJson

    via match {
      case None =>
        val string = json.compactPrint
        println(s"check and add the following assertion: $value = $string")
      case Some(expected) => json shouldBe expected.parseJson
    }

    val recovered = json.convertTo[T]
    recovered shouldBe value
  }
  def roundtrip[T: JsonFormat](value: T, via: String): Unit = roundtrip(value, Some(via))

}

