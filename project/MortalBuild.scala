import java.io._
import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._

object EnsimeBuild extends Build {

  // TODO: what is the best way to have shared project settings instead of including this explicitly in every project?
  lazy val common = scalariformSettings ++ Seq(
    organization := "com.github.fommil",
    scalaVersion := "2.11.7",
    version := "0.9.10-SNAPSHOT",

    // sbt, STFU...
    ivyLoggingLevel := UpdateLogging.Quiet,

    // WORKAROUND: https://github.com/sbt/sbt/issues/2286
    dependencyOverrides ++= Set(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
      "org.scalamacros" %% "quasiquotes" % "2.0.1"
    ),

    // WORKAROUND https://github.com/sbt/sbt/issues/2253
    fullResolvers -= Resolver.jcenterRepo,

    scalacOptions in Compile ++= Seq(
      // uncomment to debug implicit resolution compilation problems
      //"-Xlog-implicits",
      // break in case of emergency
      //"-Ytyper-debug",
      "-encoding", "UTF-8",
      "-target:jvm-1.6",
      "-feature",
      "-deprecation",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-Xlint",
      "-Yinline-warnings",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      //"-Ywarn-numeric-widen", // bad implicit widening somewhere
      //"-Ywarn-value-discard", // will require a lot of work
      "-Xfuture"
    ) ++ {
      if (scalaVersion.value.startsWith("2.11")) Seq("-Ywarn-unused-import")
      else Nil
    } ++ {
      // fatal warnings can get in the way during the DEV cycle
      if (sys.env.contains("CI")) Seq("-Xfatal-warnings")
      else Nil
    },
    javacOptions in (Compile, compile) ++= Seq(
      "-source", "1.6", "-target", "1.6", "-Xlint:all", "-Werror",
      "-Xlint:-options", "-Xlint:-path", "-Xlint:-processing"
    ),
    javacOptions in doc ++= Seq("-source", "1.6"),
    maxErrors := 1,
    fork := true,
    parallelExecution in Test := true,
    testForkedParallel in Test := true,
    javaOptions := Seq("-Xss2m", "-Xmx2g"),
    javaOptions in Test += "-Dlogback.configurationFile=../logback-test.xml",
    testOptions in Test ++= noColorIfEmacs,

    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % "2.2.5",
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "org.slf4j" % "jul-to-slf4j" % "1.7.12",
      "org.slf4j" % "jcl-over-slf4j" % "1.7.12",
      "org.scalatest" %% "scalatest" % "2.2.5" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
    ),
    libraryDependencies ++= {
      if (scalaVersion.value.startsWith("2.10."))
        Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full))
      else Nil
    }

  )

  // WORKAROUND: https://github.com/scalatest/scalatest/issues/511
  val isEmacs = sys.env.get("INSIDE_EMACS").isDefined
  def noColorIfEmacs = if (isEmacs) Seq(Tests.Argument("-oWF")) else Seq(Tests.Argument("-oF"))

  ////////////////////////////////////////////////
  // modules
  lazy val json = project settings (common)

  lazy val sexpress = project settings (common) settings (
    offline := true,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    libraryDependencies ++= List(
      "org.ensime" %% "sexpress" % "0.9.10-SNAPSHOT"
    )
  )

  lazy val bdsm = project settings (common)

}
