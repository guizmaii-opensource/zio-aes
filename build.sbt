import scala.collection.immutable.Seq

Global / onChangedBuildSource := ReloadOnSourceChanges

enablePlugins(
  ZioSbtEcosystemPlugin,
  ZioSbtCiPlugin,
)

inThisBuild(
  List(
    name                       := "zio-AES",
    organization               := "com.guizmaii",
    homepage                   := Some(url("https://github.com/guizmaii-opensource/zio-AES")),
    zioVersion                 := "2.0.18",
    scala213                   := "2.13.12",
    scala3                     := "3.3.1",
    crossScalaVersions -= scala212.value,
    scalaVersion               := scala3.value,
    ciEnabledBranches          := Seq("main"),
    ciPostReleaseJobs          := Seq.empty,
    Test / parallelExecution   := false,
    Test / fork                := true,
    run / fork                 := true,
    ciJvmOptions ++= Seq("-Xms6G", "-Xmx6G", "-Xss4M", "-XX:+UseG1GC"),
    scalafixDependencies ++= List(
      "com.github.vovapolu"                      %% "scaluzzi" % "0.1.23",
      "io.github.ghostbuster91.scalafix-unified" %% "unified"  % "0.0.9",
    ),
    licenses                 := Seq(License.Apache2),
    developers               := List(
      Developer(
        "guizmaii",
        "Jules Ivanic",
        "",
        url("https://github.com/guizmaii"),
      )
    ),
  )
)

addCommandAlias("updateReadme", "reload;docs/generateReadme")

lazy val root =
  project
    .in(file("."))
    .settings(
      name               := "zio-AES",
      publish / skip     := true,
      crossScalaVersions := Nil,// https://www.scala-sbt.org/1.x/docs/Cross-Build.html#Cross+building+a+project+statefully
    )
    .aggregate(
      `zio-AES`
    )

lazy val `zio-AES` =
  project
    .in(file("zio-AES"))
    .settings(stdSettings(Some("zio-AES"), enableScalafix = false))
    .settings(addOptionsOn("2.13")("-Xsource:3"))
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio"          % zioVersion.value,
        "dev.zio" %% "zio-prelude"  % "1.0.0-RC21",
        "dev.zio" %% "zio-test"     % zioVersion.value % Test,
        "dev.zio" %% "zio-test-sbt" % zioVersion.value % Test,
      ),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    )

lazy val docs =
  project
    .in(file("zio-AES-docs"))
    .settings(
      moduleName                                 := "zio-AES-docs",
      scalacOptions -= "-Yno-imports",
      scalacOptions -= "-Xfatal-warnings",
      projectName                                := "zio-AES",
      mainModuleName                             := (`zio-AES` / moduleName).value,
      projectStage                               := ProjectStage.ProductionReady,
      ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(`zio-AES`),
      readmeLicense += s"\n\nCopyright 2023-${java.time.Year.now()} Jules Ivanic and the zio-AES contributors.",
    )
    .enablePlugins(WebsitePlugin)
    .dependsOn(`zio-AES`)
