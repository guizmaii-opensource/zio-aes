import scala.collection.immutable.Seq

Global / onChangedBuildSource := ReloadOnSourceChanges

enablePlugins(
  ZioSbtEcosystemPlugin,
  ZioSbtCiPlugin,
)

inThisBuild(
  List(
    name              := "zio-aes",
    organization      := "com.guizmaii",
    homepage          := Some(url("https://github.com/guizmaii-opensource/zio-aes")),
    zioVersion        := "2.1.21",
    scala212          := "2.12.20",
    scala213          := "2.13.16",
    scala3            := "3.3.7",
    ciEnabledBranches := Seq("main"),
    ciPostReleaseJobs := Seq.empty,
    ciJvmOptions ++= Seq("-Xms6G", "-Xmx6G", "-Xss4M", "-XX:+UseG1GC"),
    scalafixDependencies ++= List(
      "com.github.vovapolu"                      %% "scaluzzi" % "0.1.23",
      "io.github.ghostbuster91.scalafix-unified" %% "unified"  % "0.0.9",
    ),
    licenses          := Seq(License.Apache2),
    developers        := List(
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
      name               := "zio-aes",
      publish / skip     := true,
      crossScalaVersions := Nil, // https://www.scala-sbt.org/1.x/docs/Cross-Build.html#Cross+building+a+project+statefully
    )
    .aggregate(
      `zio-aes`
    )

lazy val `zio-aes` =
  project
    .in(file("zio-aes"))
    .settings(stdSettings(Some("zio-aes")))
    .settings(addOptionsOn("2.13")("-Xsource:3"))
    .settings(addOptionsOn("2.12")("-Xsource:3"))
    .settings(
      libraryDependencies ++= Seq(
        "dev.zio" %% "zio"          % zioVersion.value,
        "dev.zio" %% "zio-prelude"  % "1.0.0-RC41",
        "dev.zio" %% "zio-test"     % zioVersion.value % Test,
        "dev.zio" %% "zio-test-sbt" % zioVersion.value % Test,
      ),
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    )

lazy val docs =
  project
    .in(file("zio-aes-docs"))
    .settings(
      moduleName                                 := "zio-aes-docs",
      scalacOptions -= "-Yno-imports",
      scalacOptions -= "-Xfatal-warnings",
      projectName                                := "zio-aes",
      mainModuleName                             := (`zio-aes` / moduleName).value,
      projectStage                               := ProjectStage.ProductionReady,
      ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(`zio-aes`),
      readmeLicense += s"\n\nCopyright 2023-${java.time.Year.now()} Jules Ivanic and the zio-aes contributors.",
    )
    .enablePlugins(WebsitePlugin)
    .dependsOn(`zio-aes`)
