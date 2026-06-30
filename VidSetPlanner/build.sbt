ThisBuild / organization := "vsp"
ThisBuild / version := "1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "VidSetPlanner",
    Compile / mainClass := Some("vsp.Main"),
    run / fork := true,
    Test / fork := true,
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "20.0.0-R31",
      "org.openjfx" % "javafx-controls" % "21",
      "org.openjfx" % "javafx-graphics" % "21",
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.xerial" % "sqlite-jdbc" % "3.45.1.0",
      "org.flywaydb" % "flyway-core" % "9.22.3",
      "com.google.apis" % "google-api-services-calendar" % "v3-rev20240111-2.0.0",
      "com.google.auth" % "google-auth-library-oauth2-http" % "1.23.0",
      "org.json" % "json" % "20240303",
      "com.typesafe.slick" %% "slick" % "3.6.1",
      "org.slf4j" % "slf4j-nop" % "2.0.16"
    )
  )
