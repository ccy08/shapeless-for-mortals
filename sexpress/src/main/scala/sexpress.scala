// Copyright (C) 2015 Sam Halliday
// License: http://www.apache.org/licenses/LICENSE-2.0
package s4m.sexpress

/**
 * This is for Exercise 2. The sexpress core library from ENSIME is on
 * the classpath, you are advised to study the domain model
 * =org.ensime.sexp.Sexp= and the typeclass =SexpFormat=.
 *
 * This is effectively reimplementing the JSON marshallers (but in a
 * far superior data format).
 *
 * - Exercise 2.1: implement =SexpFormat[T]= for sealed traits.
 * - Exercise 2.2: customise products as "data" or "alist" forms.
 */
trait SexpFamilyFormats
