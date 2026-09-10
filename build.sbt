import BuildHelper.{noDoc, stdSettings}

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion      := "3.3.8"
ThisBuild / scalafmtCheck     := true
ThisBuild / scalafmtSbtCheck  := true
ThisBuild / scalafmtOnCompile := !insideCI.value
ThisBuild / scalafixOnCompile := !insideCI.value
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision // use Scalafix compatible version
ThisBuild / scalafixDependencies ++= List(
  "com.github.vovapolu"                      %% "scaluzzi" % "0.1.23",
  "io.github.ghostbuster91.scalafix-unified" %% "unified"  % "0.0.9",
)

// ### Aliases ###

addCommandAlias("tc", "Test/compile")
addCommandAlias("ctc", "clean; tc")
addCommandAlias("rctc", "reload; ctc")
addCommandAlias("fix", "scalafixAll; scalafmtAll; scalafmtSbt")
addCommandAlias("check", "scalafixAll --check; scalafmtCheckAll; scalafmtSbtCheck")

// ### Dependencies ###

lazy val zioVersion = "2.1.26"

// ### Modules ###

lazy val root =
  project
    .in(file("."))
    .settings(noDoc *)
    .settings(publish / skip := true)
    .settings(crossScalaVersions := Nil) // https://www.scala-sbt.org/1.x/docs/Cross-Build.html#Cross+building+a+project+statefully
    .aggregate(`zio-aes`)

lazy val `zio-aes` =
  project
    .in(file("zio-aes"))
    .settings(stdSettings *)
    .settings(
      name := "zio-aes",
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio"          % zioVersion,
        "dev.zio" %% "zio-prelude"  % "1.0.0-RC48",
        "dev.zio" %% "zio-test"     % zioVersion % Test,
        "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      ),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    )

inThisBuild(
  List(
    organization := "com.guizmaii",
    homepage     := Some(url("https://github.com/guizmaii-opensource/zio-aes")),
    licenses     := List("Apache 2.0" -> url("https://opensource.org/license/apache-2.0")),
    developers   := List(
      Developer(
        "guizmaii",
        "Jules Ivanic",
        "",
        url("https://github.com/guizmaii"),
      )
    ),
  )
)
