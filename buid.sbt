lazy val root = (project in file(".")).
  settings(
    name         := "finch-1",
    version      := "1.0",
    scalaVersion := "2.11.7",

    libraryDependencies ++= Seq(
      "com.github.finagle" %% "finch-core"    % "0.10.0",
      "com.github.finagle" %% "finch-circe"   % "0.10.0",
      "io.circe"           %% "circe-generic" % "0.3.0",

      "com.lihaoyi" % "ammonite-repl" % "0.5.4" % "test" cross CrossVersion.full
    ),

    javaOptions += "-Xms256M",
    initialCommands in (Test, console) := """ammonite.repl.Main.run("")"""


  )