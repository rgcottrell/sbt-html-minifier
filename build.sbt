sbtPlugin := true

organization := "com.slidingautonomy.sbt"

name := "sbt-html-minifier"

version := "1.0.0"

scalaVersion := "2.10.4"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "com.typesafe" % "jstranspiler" % "1.0.0",
  "org.webjars" % "mkdirp" % "0.3.5",
  "org.webjars" % "when-node" % "3.2.2"
)

resolvers ++= Seq(
  "Typesafe Releases Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  Resolver.mavenLocal
)

addSbtPlugin("com.typesafe.sbt" % "sbt-js-engine" % "1.0.1")

scriptedSettings

scriptedLaunchOpts <+= version apply { v => s"-Dproject.version=$v" }

publishMavenStyle := true

publishTo := {
  if (isSnapshot.value) Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
  else Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
}

pomExtra :=
  <url>https://github.com/rgcottrell/sbt-html-minifier</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:rgcottrell/sbt-html-minifier.git</url>
    <connection>scm:git:git@github.com:rgcottrell/sbt-html-minifier.git</connection>
  </scm>
  <developers>
    <developer>
      <id>rgcottrell</id>
      <name>Robert Cottrell</name>
      <url>https://github.com/rgcottrell</url>
    </developer>
  </developers>
