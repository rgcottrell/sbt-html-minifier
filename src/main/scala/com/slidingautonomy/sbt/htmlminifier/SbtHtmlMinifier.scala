package com.slidingautonomy.sbt.htmlminifier

import sbt._
import sbt.Keys._
import com.typesafe.sbt.jse.{SbtJsEngine, SbtJsTask}
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.web.pipeline.Pipeline
import spray.json.{JsArray, JsBoolean, JsString, JsObject}

object Import {

  val htmlMinifier = TaskKey[Pipeline.Stage]("html-minifier", "Invoke the html-minifier optimizer.")

  object HtmlMinifierKeys {
    val appDir = SettingKey[File]("html-minifier-app-dir", "The top level directory that contains your app html files.")
    val buildDir = SettingKey[File]("html-minifier-build-dir", "The target directory for minified HTML files.")
    val caseSensitive = SettingKey[Boolean]("html-minifier-case-sensitive", "Treat attributes in case sensitive manner (useful for SVG; e.g. viewBox).")
    val collapseBooleanAttributes = SettingKey[Boolean]("html-minifier-collapse-boolean-attributes", "Omit attribute values from boolean attributes.")
    val collapseWhitespace = SettingKey[Boolean]("html-minifier-collapse-whitespace", "Collapse white space that contributes to text nodes in a document tree.")
    val conservativeCollapse = SettingKey[Boolean]("html-minifier-conservative-collapse", "Always collapse to 1 space (never remove it entirely). Must be used in conjunction with collapseWhitespace=true.")
    val ignoreCustomComments = SettingKey[Seq[String]]("html-minifier-ignore-custom-comments", "Array of regex'es that allow to ignore certain comments, when matched.")
    val keepClosingSlash = SettingKey[Boolean]("html-minifier-keep-closing-slash", "Keep the trailing slash on singleton elements.")
    val minifyCSS = SettingKey[Boolean]("html-minifier-minify-css", "Minify CSS in style elements and style attributes (uses clean-css).")
    val minifyJS = SettingKey[Boolean]("html-minifier-minify-js", "Minify Javascript in script elements and on* attributes (uses UglifyJS).")
    val processScripts = SettingKey[Seq[String]]("html-minifier-process-scripts", "Array of strings corresponding to types of script elements to process through minifier (e.g. \"text/ng-template\", \"text/x-handlebars-template\", etc.).")
    val removeAttributeQuotes = SettingKey[Boolean]("html-minifier-remove-attribute-quotes", "Remove quotes around attributes when possible.")
    val removeCDATASectionsFromCDATA = SettingKey[Boolean]("html-minifier-remove-cdata-sections-from-cdata", "Remove CDATA sections from script and style elements.")
    val removeComments = SettingKey[Boolean]("html-minifier-remove-comments", "Strip HTML comments.")
    val removeEmptyAttributes = SettingKey[Boolean]("html-minifier-remove-empty-attributes", "Remove all attributes with whitespace-only values.")
    val removeEmptyElements = SettingKey[Boolean]("html-minifier-remove-empty-elements", "Remove all elements with empty contents.")
    val removeOptionalTags = SettingKey[Boolean]("html-minifier-remove-optional-tags", "Remove unrequired tags.")
    val removeRedundantAttributes = SettingKey[Boolean]("html-minifier-remove-redundant-attributes", "Remove attributes when value matches default.")
    val removeScriptTypeAttributes = SettingKey[Boolean]("html-minifier-remove-script-tTpe-attributes", "Remove script type attributes from JavaScript scripts.")
    val removeStyleLinkTypeAttributes = SettingKey[Boolean]("html-minifier-remove-style-link-type-attributes", "Remove style type attributes from CSS styles.")
    val useShortDoctype = SettingKey[Boolean]("html-minifier-use-short-doctype", "Replaces the doctype with the short (HTML5) doctype.")
  }

}

object SbtHtmlMinifier extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsEngine.autoImport.JsEngineKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport._
  import HtmlMinifierKeys._

  override def projectSettings = Seq(
    appDir := (resourceManaged in htmlMinifier).value / "appdir",
    buildDir := (resourceManaged in htmlMinifier).value / "build",
    caseSensitive := false,
    collapseBooleanAttributes := true,
    collapseWhitespace := true,
    conservativeCollapse := false,
    excludeFilter in htmlMinifier := HiddenFileFilter,
    htmlMinifier := runMinifier.dependsOn(webJarsNodeModules in Plugin).value,
    keepClosingSlash := false,
    ignoreCustomComments := Seq(),
    includeFilter in htmlMinifier := GlobFilter("*.htm") | GlobFilter("*.html"),
    minifyCSS := false,
    minifyJS := false,
    processScripts := Seq(),
    removeAttributeQuotes := false,
    removeCDATASectionsFromCDATA := false,
    removeComments := true,
    removeEmptyAttributes := true,
    removeEmptyElements := false,
    removeOptionalTags := false,
    removeRedundantAttributes := true,
    removeScriptTypeAttributes := true,
    removeStyleLinkTypeAttributes := true,
    resourceManaged in htmlMinifier := webTarget.value / htmlMinifier.key.label,
    useShortDoctype := true
  )

  private def runMinifier: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>

      val include = (includeFilter in htmlMinifier).value
      val exclude = (excludeFilter in htmlMinifier).value
      val preMappings = mappings.filter(f => !f._1.isDirectory && include.accept(f._1) && !exclude.accept(f._1))
      SbtWeb.syncMappings(
        streams.value.cacheDirectory,
        preMappings,
        appDir.value
      )

      val cacheDirectory = streams.value.cacheDirectory / htmlMinifier.key.label
      val runUpdate = FileFunction.cached(cacheDirectory, FilesInfo.hash) {
        inputFiles =>
          streams.value.log("Minifying HTML files with html-minify")

          val sourceFileMappings = JsArray(inputFiles.filter(_.isFile).map { f =>
            val relativePath = IO.relativize(appDir.value, f).get
            JsArray(JsString(f.getPath), JsString(relativePath))
          }.toList).toString()

          val targetPath = buildDir.value.getPath

          val jsOptions = JsObject(
            "minifyOptions" -> JsObject(
              "caseSensitive" -> JsBoolean(caseSensitive.value),
              "collapseBooleanAttributes" -> JsBoolean(collapseBooleanAttributes.value),
              "collapseWhitespace" -> JsBoolean(collapseWhitespace.value),
              "conservativeCollapse" -> JsBoolean(conservativeCollapse.value),
              "keepClosingSlash" -> JsBoolean(keepClosingSlash.value),
              "ignoreCustomComments" -> JsArray(ignoreCustomComments.value.toList.map(JsString(_))),
              "minifyCSS" -> JsBoolean(minifyCSS.value),
              "minifyJS" -> JsBoolean(minifyJS.value),
              "processScripts" -> JsArray(processScripts.value.toList.map(JsString(_))),
              "removeAttributeQuotes" -> JsBoolean(removeAttributeQuotes.value),
              "removeCDATASectionsFromCDATA" -> JsBoolean(removeCDATASectionsFromCDATA.value),
              "removeComments" -> JsBoolean(removeComments.value),
              "removeEmptyAttributes" -> JsBoolean(removeEmptyAttributes.value),
              "removeEmptyElements" -> JsBoolean(removeEmptyElements.value),
              "removeOptionalTags" -> JsBoolean(removeOptionalTags.value),
              "removeRedundantAttributes" -> JsBoolean(removeRedundantAttributes.value),
              "removeScriptTypeAttributes" -> JsBoolean(removeScriptTypeAttributes.value),
              "removeStyleLinkTypeAttributes" -> JsBoolean(removeStyleLinkTypeAttributes.value),
              "useShortDoctype" -> JsBoolean(useShortDoctype.value)
            )
          ).toString()

          val shellFile = SbtWeb.copyResourceTo(
            (resourceManaged in htmlMinifier).value,
            getClass.getClassLoader.getResource("html-minifier-shell.js"),
            streams.value.cacheDirectory
          )

          SbtJsTask.executeJs(
            state.value,
            (engineType in htmlMinifier).value,
            (command in htmlMinifier).value,
            (nodeModuleDirectories in Plugin).value.map(_.getPath),
            shellFile,
            Seq(sourceFileMappings, targetPath, jsOptions),
            (timeoutPerSource in htmlMinifier).value * preMappings.size
          )

          buildDir.value.***.get.toSet
      }

      val postMappings = runUpdate(appDir.value.***.get.toSet).filter(_.isFile).pair(relativeTo(buildDir.value))
      (mappings.toSet -- preMappings ++ postMappings).toSeq
  }
}
