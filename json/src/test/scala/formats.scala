// Copyright (C) 2015 Sam Halliday
// License: http://www.gnu.org/licenses/gpl.html
package s4m.exercise3.formats

import spray.json._
import s4m.json._
import shapeless._
import java.util.UUID

import s4m.exercise3.api._

trait LowPriorityUserFormats {
  ///////////////////////////////////////////////
  // non-trivial user-defined JsonFormat (not RootJsonFormat)
  //
  // This is in a lower priority implicit scope than the familyFormats
  // (caveat #2), so we have two options to force its visibility:
  //
  // 1. do the same trick as with SymbolJsonFormat
  // 2. shadow familyFormat with a non-implicit variant (see below)
  //
  // Most usecases will prefer option 1. Note that the same trick is
  // needed for JsonFormats and RootJsonFormats, there is nothing
  // special about this being JsonFormat.
  implicit val SmashFormat: JsonFormat[Smash] = new JsonFormat[Smash] {
    def read(json: JsValue): Smash = json match {
      case obj: JsObject => obj.fields.head match {
        case ("flooma", JsString(label)) => Flooma(label)
        case ("blam", JsString(label)) => Blam(label)
        case _ => deserializationError("expected (kind,JsString), got " + json)
      }
      case _ => deserializationError("expected JsString, got " + json)
    }
    def write(obj: Smash): JsValue = obj match {
      case Flooma(label) => JsObject("flooma" -> JsString(label))
      case Blam(label) => JsObject("blam" -> JsString(label))
    }
  }
}

object ExamplesFormats extends DefaultJsonProtocol with FamilyFormats with LowPriorityUserFormats {
  // WORKAROUND caveat 2 (interestingly, adding type signatures breaks everything)
  implicit val highPrioritySymbolFormat = SymbolJsonFormat
  implicit val highPrioritySmashFormat = SmashFormat
  //override implicit def optionFormat[T: JsonFormat]: JsonFormat[Option[T]] = super.optionFormat

  ///////////////////////////////////////////////
  // Example of "explicit implicit" for performance
  implicit val SimpleTraitFormat: JsonFormat[SimpleTrait] = cachedImplicit

  ///////////////////////////////////////////////
  // user-defined hinting
  // EXERCISE 3.2
  //implicit object SubTraitHint extends FlatCoproductHint[SubTrait]("hint")
  //implicit object SpielHint extends NestedCoproductHint[Spiel]

  ///////////////////////////////////////////////
  // user-defined field naming rules
  // EXERCISE 3.2
  // implicit object ClodaHint extends FlatCoproductHint[Cloda]("TYPE") {
  //   override def fieldName(orig: String): String = orig.toUpperCase
  // }
  // implicit object PloobaHint extends ProductHint[Plooba] {
  //   override def fieldName[K <: Symbol](k: K): String = k.name.toUpperCase
  // }

  ///////////////////////////////////////////////
  // user-defined /missing value rules

  // EXERCISE 3.1
  // implicit object HueyHint extends ProductHint[Huey] {
  //   override def nulls = AlwaysJsNull
  // }
  // implicit object DeweyHint extends ProductHint[Dewey] {
  //   override def nulls = JsNullNotNone
  // }
  // implicit object LouieHint extends ProductHint[Louie] {
  //   override def nulls = NeverJsNull
  // }

  implicit object QuackFormat extends JsonFormat[Quack.type] {
    // needed something that would serialise to JsNull for testing
    def read(j: JsValue): Quack.type = j match {
      case JsNull => Quack
      case other => deserializationError(s"unexpected $other")
    }
    def write(q: Quack.type): JsValue = JsNull
  }

  ///////////////////////////////////////////////
  // user-defined JsonFormat
  implicit object SchpugelFormat extends JsonFormat[Schpugel] {
    def read(j: JsValue): Schpugel = j match {
      case JsString(v) => Schpugel(v)
      case other => deserializationError(s"unexpected $other")
    }
    def write(s: Schpugel): JsValue = JsString(s.v)
  }

  ///////////////////////////////////////////////
  // user-defined JsonFormat
  implicit object SmimFormat extends JsonFormat[Smim] {
    def read(j: JsValue): Smim = j match {
      case JsObject(els) if els.contains("smim") =>
        els("smim") match {
          case JsString(v) => Smim(v)
          case other => deserializationError(s"unexpected $other")
        }
      case other => deserializationError(s"unexpected $other")
    }
    def write(s: Smim): JsValue = JsObject("smim" -> JsString(s.v))
  }
}
