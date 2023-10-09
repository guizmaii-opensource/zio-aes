val zioSbtVersion = "0.4.0-alpha.21"

addSbtPlugin("dev.zio" % "zio-sbt-ecosystem" % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-ci"        % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-website"   % zioSbtVersion)

addSbtPlugin("org.scalameta"             % "sbt-scalafmt" % "2.5.0")
addSbtPlugin("pl.project13.scala"        % "sbt-jmh"      % "0.4.4")
addSbtPlugin("com.timushev.sbt"          % "sbt-updates"  % "0.6.4")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.2")
