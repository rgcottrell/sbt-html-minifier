import JsEngineKeys._

lazy val root = (project in file(".")).enablePlugins(SbtWeb)

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

pipelineStages := Seq(htmlMinifier)

val checkFileContents = taskKey[Unit]("check that file contents are minified")

checkFileContents := {
  val contents = IO.read(file("target/web/stage/index.html"))
  if (!contents.contains("<!DOCTYPE html>")) {
    sys.error(s"Expected short doctype: $contents")
  }
  if (!contents.contains("<meta name=\"charset\" content=\"UTF-8\">")) {
    sys.error(s"Expected unclosed meta tag: $contents")
  }
  if (contents.contains("<!--")) {
    sys.error(s"Expected comments removed: $contents")
  }
  if (contents.contains(("  "))) {
    sys.error(s"Expected collapsed whitespace: $contents")
  }
}