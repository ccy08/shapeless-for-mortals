addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.5.1")

scalacOptions in Compile ++= Seq("-feature", "-deprecation")

// sbt, STFU...
ivyLoggingLevel := UpdateLogging.Quiet
