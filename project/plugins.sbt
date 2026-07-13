val zioSbtVersion = "0.6.0"

addSbtPlugin("dev.zio" % "zio-sbt-ecosystem" % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-ci"        % zioSbtVersion)
addSbtPlugin("dev.zio" % "zio-sbt-website"   % zioSbtVersion)

addSbtPlugin("org.scalameta"      % "sbt-scalafmt" % "2.6.1")
addSbtPlugin("pl.project13.scala" % "sbt-jmh"      % "0.4.8")
addSbtPlugin("com.timushev.sbt"   % "sbt-updates"  % "0.7.0")
addSbtPlugin("org.typelevel"      % "sbt-tpolecat" % "0.5.7")
