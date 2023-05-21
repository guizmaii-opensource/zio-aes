ThisBuild / scalaVersion := "3.2.2"
ThisBuild / version      := "0.0.1"
ThisBuild / organization := "zio.aes"
ThisBuild / developers   := List(
  Developer(
    "guizmaii",
    "Jules Ivanic",
    "",
    url("https://github.com/guizmaii"),
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "zio-AES",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % "2.0.13",
      "dev.zio" %% "zio-prelude"  % "1.0.0-RC19",
      "dev.zio" %% "zio-test"     % "2.0.13" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.13" % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )
