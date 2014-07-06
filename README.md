sbt-html-minifier
=================

An [sbt-web](https://github.com/sbt/sbt-web) plugin that uses [html-minifier](https://github.com/kangax/html-minifier)
to minify HTML template files.

To use the latest version from GitHub, add the following to the `project/plugins.sbt` of your project:

```scala
lazy val root = project.in(file(".")).dependsOn(sbtHtmlMinifier)
lazy val sbtHtmlMinifier = uri("git://github.com/rgcottrell/sbt-html-minifier")
```

Your project's build file also needs to enable sbt-web plugins. For example with build.sbt:

```scala
lazy val root = (project in file(".")).enablePlugins(SbtWeb)
```

## Configuration

The plugin is configured with sensible defaults, but can be customized to meet individual requirements. Most of the
same configutation options used by the html-minifier package may be specified. For example, to disable comment
stripping:

```scala
HtmlMinifierKeys.removeComments := false
```

See the html-minifier home page for information on available options.

## File Filters

By default, the plugin scans the assets directory for any file ending in .html and creates a new minified file ending
with .min.html. The files to be processed can be filtered using the includeFilter and excludeFilter settings.
For example, to limit minification to just tpl.html template files:

```scala
includeFilter in (Assets, HtmlMinifierKeys.htmlMinifier) := "*tpl.html"
```

To change the extensions used by the files:

```scala
HtmlMinifierKeys.inputExtension := ".htm"

HtmlMinifierKeys.outputExtension := "min.htm"

includeFilter in (Assets, HtmlMinifierKeys.htmlMinifier) := "*.htm"
```

## Prerequisites

The plugin requires that your project have the html-minifier node module install. The easiest way to do this is to
include a package.json file at the root of your project:

```json
{
  "dependencies": {
    "html-minifier": "^0.6.3"
  }
}
```
