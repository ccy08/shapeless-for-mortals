// Copyright (C) 2015 Sam Halliday
// License: http://www.apache.org/licenses/LICENSE-2.0
package s4m.exercise3.api

import java.util.UUID

// Example domain models used in the tests. Note that the domain model
// and formatters are defined in sibling packages.
sealed trait SimpleTrait
case class Foo(s: String) extends SimpleTrait
case class Bar() extends SimpleTrait
case object Baz extends SimpleTrait
case class Faz(o: Option[String]) extends SimpleTrait

sealed trait SubTrait extends SimpleTrait
case object Fuzz extends SubTrait

sealed trait Spiel
case object Buzz extends Spiel

case class Schpugel(v: String) // I asked my wife to make up a word
case class Smim(v: String) // I should stop asking my wife to make up words

sealed trait Smash
case class Flooma(label: String) extends Smash
case class Blam(label: String) extends Smash

sealed trait Cloda
case class Plooba(thing: String) extends Cloda // *sigh*

object Quack
case class Huey(duck: Quack.type, witch: Option[Quack.type])
case class Dewey(duck: Quack.type, witch: Option[Quack.type])
case class Louie(duck: Quack.type, witch: Option[Quack.type])

// I love monkeys, you got a problem with that?
sealed trait Primates
sealed trait Strepsirrhini extends Primates
sealed trait Haplorhini extends Primates
sealed trait Tarsiiformes extends Haplorhini
case object Tarsiidae extends Tarsiiformes
sealed trait Simiiformes extends Haplorhini
sealed trait Platyrrhini extends Simiiformes
case object Callitrichidae extends Platyrrhini
case object Cebidae extends Platyrrhini
case object Aotidae extends Platyrrhini
case object Pitheciidae extends Platyrrhini
case object Atelidae extends Platyrrhini
sealed trait Catarrhini extends Simiiformes
sealed trait Cercopithecoidea extends Catarrhini
case object Cercopithecidae extends Cercopithecoidea
sealed trait Hominoidea extends Catarrhini
case object Hylobatidae extends Hominoidea
case class Hominidae(id: UUID) extends Hominoidea
