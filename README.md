sbt-html-minifier
=================
[![Build Status](https://travis-ci.org/rgcottrell/sbt-html-minifier.svg?branch=master)](https://travis-ci.org/rgcottrell/sbt-html-minifier)

An [sbt-web](https://github.com/sbt/sbt-web) plugin that uses [html-minifier](https://github.com/kangax/html-minifier)
to minify HTML template files.

Add the plugin to the `project/plugins.sbt` of your project:

```scala
addSbtPlugin("com.slidingautonomy.sbt" % "sbt-html-minifier" % "1.0.0")
```

Add the [Sonatype releases] resolver:

```scala
resolvers += Resolver.sonatypeRepo("releases")
```

Your project's build file also needs to enable sbt-web plugins. For example with build.sbt:

```scala
lazy val root = (project in file(".")).enablePlugins(SbtWeb)
```

As with all sbt-web asset pipeline plugins, you must declare their order of execution. For example:

```scala
pipelineStages := Seq(htmlMinifier)
```

## Options

The plugin is configured with sensible defaults, but can be customized to meet individual requirements. Most of the
same configutation options used by the html-minifier package may be specified. For example, to disable comment
stripping:

```scala
HtmlMinifierKeys.removeComments := false
```

See the html-minifier home page for information on available options.

## File Filters

By default, the plugin scans the assets directory for any file ending in `.htm` or `.html` and creates new minified
versions of those files. The files to be processed can be filtered using the includeFilter and excludeFilter settings.
For example, to limit minification to just `.tpl.html` template files:

```scala
includeFilter in htmlMinifier := "*tpl.html"
```

## Prerequisites

The plugin requires that your project have the html-minifier Node module install. The easiest way to do this is to
include a package.json file at the root of your project:

```json
{
  "dependencies": {
    "html-minifier": "^0.6.3"
  }
}
```
