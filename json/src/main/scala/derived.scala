// Copyright (C) 2015 Sam Halliday
// License: http://www.apache.org/licenses/LICENSE-2.0
package s4m.json

import org.slf4j.LoggerFactory

import spray.json._

import shapeless._, labelled._

/**
 * A trimmed down version of the version in spray-json-shapeless.
 *
 * - Exercise 3.1: customise product field names
 * - Exercise 3.2: customise coproduct (flat vs nested)
 * - Exercise 3.3: customise handling of =null= and =Option=
 * - Exercise 3.4: handle default values on products
 */
trait FamilyFormats extends LowPriorityFamilyFormats {
  this: StandardFormats =>
}
object FamilyFormats extends DefaultJsonProtocol with FamilyFormats

trait LowPriorityFamilyFormats {
  this: StandardFormats with FamilyFormats =>

  import JsonFormat.RichJsValue

  private val log = LoggerFactory.getLogger(getClass)

  implicit object hNilFormat extends JsonFormat[HNil] {
    def read(j: JsValue) = HNil
    def write(n: HNil) = JsObject()
  }

  implicit def hListFormat[Key <: Symbol, Value, Remaining <: HList](
    implicit
    key: Witness.Aux[Key],
    lazyJfh: Lazy[JsonFormat[Value]],
    lazyJft: Lazy[JsonFormat[Remaining]]
  ): JsonFormat[FieldType[Key, Value] :: Remaining] = new JsonFormat[FieldType[Key, Value] :: Remaining] {
    val jfh = lazyJfh.value
    val jft = lazyJft.value
    def write(hlist: FieldType[Key, Value] :: Remaining) =
      jft.write(hlist.tail).asJsObject :+
        (key.value.name -> jfh.write(hlist.head))

    def read(json: JsValue) = {
      val fields = json.asJsObject.fields
      val head = jfh.read(fields(key.value.name))
      val tail = jft.read(json)
      field[Key](head) :: tail
    }
  }

  implicit object CNilFormat extends JsonFormat[CNil] {
    def read(j: JsValue) = ???
    def write(n: CNil) = ???
  }

  implicit def coproductFormat[Name <: Symbol, Head, Tail <: Coproduct](
    implicit
    key: Witness.Aux[Name],
    lazyJfh: Lazy[JsonFormat[Head]],
    lazyJft: Lazy[JsonFormat[Tail]]
  ): JsonFormat[FieldType[Name, Head] :+: Tail] = new JsonFormat[FieldType[Name, Head] :+: Tail] {
    val jfh = lazyJfh.value
    val jft = lazyJft.value

    def read(j: JsValue) =
      if (j.asJsObject.fields("type") == JsString(key.value.name))
        Inl(field[Name](jfh.read(j)))
      else
        Inr(jft.read(j))
    def write(lr: FieldType[Name, Head] :+: Tail) = lr match {
      case Inl(found) =>
        jfh.write(found).asJsObject :+ ("type" -> JsString(key.value.name))

      case Inr(tail) =>
        jft.write(tail)
    }
  }

  implicit def familyFormat[T, Repr](
    implicit
    gen: LabelledGeneric.Aux[T, Repr],
    lazySg: Lazy[JsonFormat[Repr]],
    tpe: Typeable[T]
  ): JsonFormat[T] = new JsonFormat[T] {
    val sg = lazySg.value
    if (log.isTraceEnabled)
      log.trace(s"creating ${tpe.describe}")

    def read(j: JsValue): T = gen.from(sg.read(j))
    def write(t: T): JsValue = sg.write(gen.to(t))
  }

}

// extra syntax for spray.json
object JsonFormat {
  def apply[T](implicit f: Lazy[JsonFormat[T]]): JsonFormat[T] = f.value

  implicit class RichJsValue(val j: JsValue) extends AnyVal {
    def :+(kv: (String, JsValue)): JsValue = JsObject(j.asJsObject.fields + kv)
  }
}
