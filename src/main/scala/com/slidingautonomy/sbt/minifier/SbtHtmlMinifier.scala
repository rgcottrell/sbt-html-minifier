package com.slidingautonomy.sbt.minifier

import sbt._
import sbt.Keys._
import com.typesafe.sbt.jse.SbtJsTask
import com.typesafe.sbt.web.SbtWeb
import spray.json.{JsArray, JsBoolean, JsString, JsObject}

object Import {

  object HtmlMinifierKeys {
    val caseSensitive = SettingKey[Boolean]("htmlMinifier-caseSensitive", "Treat attributes in case sensitive manner (useful for SVG; e.g. viewBox).")
    val collapseBooleanAttributes = SettingKey[Boolean]("htmlMinifier-collapseBooleanAttributes", "Omit attribute values from boolean attributes.")
    val collapseWhitespace = SettingKey[Boolean]("htmlMinifier-collapseWhitespace", "Collapse white space that contributes to text nodes in a document tree.")
    val conservativeCollapse = SettingKey[Boolean]("htmlMinifier-conservativeCollapse", "Always collapse to 1 space (never remove it entirely). Must be used in conjunction with collapseWhitespace=true.")
    val htmlMinifier = TaskKey[Seq[File]]("htmlMinifier", "Invoke the Angular template compiler.")
    val ignoreCustomComments = SettingKey[Seq[String]]("htmlMinifier-ignoreCustomComments", "Array of regex'es that allow to ignore certain comments, when matched.")
    val inputExtension = SettingKey[String]("htmlMinifier-inputExtension", "The file extension of source files.");
    val keepClosingSlash = SettingKey[Boolean]("htmlMinifier-keepClosingSlash", "Keep the trailing slash on singleton elements.")
    val minifyCSS = SettingKey[Boolean]("htmlMinifier-minifyCSS", "Minify CSS in style elements and style attributes (uses clean-css).")
    val minifyJS = SettingKey[Boolean]("htmlMinifier-minifyJS", "Minify Javascript in script elements and on* attributes (uses UglifyJS).")
    val outputExtension = SettingKey[String]("htmlMinifier-outputExtension", "The file extension for processed assets.")
    val processScripts = SettingKey[Seq[String]]("htmlMinifier-processScripts", "Array of strings corresponding to types of script elements to process through minifier (e.g. \"text/ng-template\", \"text/x-handlebars-template\", etc.).")
    val removeAttributeQuotes = SettingKey[Boolean]("htmlMinifier-removeAttributeQuotes", "Remove quotes around attributes when possible.")
    val removeCDATASectionsFromCDATA = SettingKey[Boolean]("htmlMinifier-removeCDATASectionsFromCDATA", "Remove CDATA sections from script and style elements.")
    val removeComments = SettingKey[Boolean]("htmlMinifier-removeComments", "Strip HTML comments.")
    val removeEmptyAttributes = SettingKey[Boolean]("htmlMinifier-removeEmptyAttributes", "Remove all attributes with whitespace-only values.")
    val removeEmptyElements = SettingKey[Boolean]("htmlMinifier-removeEmptyElements", "Remove all elements with empty contents.")
    val removeOptionalTags = SettingKey[Boolean]("htmlMinifier-removeOptionalTags", "Remove unrequired tags.")
    val removeRedundantAttributes = SettingKey[Boolean]("htmlMinifier-removeRedundantAttributes", "Remove attributes when value matches default.")
    val removeScriptTypeAttributes = SettingKey[Boolean]("htmlMinifier-removeScriptTypeAttributes", "Remove script type attributes from JavaScript scripts.")
    val removeStyleLinkTypeAttributes = SettingKey[Boolean]("htmlMinifier-removeStyleLinkTypeAttributes", "Remove style type attributes from CSS styles.")
    val useShortDoctype = SettingKey[Boolean]("htmlMinifier-useShortDoctype", "Replaces the doctype with the short (HTML5) doctype.")
  }

}

object SbtHtmlMinifier extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport.HtmlMinifierKeys._

  val htmlMinifierUnscopedSettings = Seq(
    includeFilter := GlobFilter("*.html"),
    jsOptions := JsObject(
      "inputExtension" -> JsString(inputExtension.value),
      "outputExtension" -> JsString(outputExtension.value),
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
  )

  override def projectSettings = Seq(
    caseSensitive := false,
    collapseBooleanAttributes := true,
    collapseWhitespace := true,
    conservativeCollapse := false,
    keepClosingSlash := false,
    ignoreCustomComments := Seq(),
    inputExtension := ".html",
    minifyCSS := false,
    minifyJS := false,
    outputExtension := ".min.html",
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
    useShortDoctype := true
  ) ++ inTask(htmlMinifier) {
    SbtJsTask.jsTaskSpecificUnscopedSettings ++
    inConfig(Assets)(htmlMinifierUnscopedSettings) ++
    inConfig(TestAssets)(htmlMinifierUnscopedSettings) ++
    Seq(
      moduleName := "htmlMinifier",
      shellFile := getClass.getClassLoader.getResource("html-minifier-shell.js"),
      taskMessage in Assets := "HtmlMinifier processing",
      taskMessage in TestAssets := "HtmlMinifier test processing"
    )
  } ++ SbtJsTask.addJsSourceFileTasks(htmlMinifier) ++ Seq(
    htmlMinifier in Assets := (htmlMinifier in Assets).dependsOn(nodeModules in Assets).value,
    htmlMinifier in TestAssets := (htmlMinifier in TestAssets).dependsOn(nodeModules in TestAssets).value
  )

}
