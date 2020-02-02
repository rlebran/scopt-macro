name := "scopt-generic-parser"

version := "0.1.0"

scalaVersion in ThisBuild := "2.11.12"

lazy val commonSettings = Seq(
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M11" cross CrossVersion.full),
  libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"
)

lazy val macros = project.in(file("macros"))
  .settings(commonSettings: _*)
  .settings(
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies += "org.scalameta" %% "scalameta" % "4.3.0"
  )

lazy val example = project.in(file("example"))
  .settings(commonSettings: _*)
  .dependsOn(macros)

lazy val root = project.in(file("."))
  .aggregate(example)
