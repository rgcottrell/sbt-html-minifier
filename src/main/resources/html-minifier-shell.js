/*global process, require */

var fs = require("fs"),
    jst = require("jstranspiler"),
    nodefn = require("when/node"),
    minify = require("html-minifier").minify,
    mkdirp = require("mkdirp"),
    path = require("path");

var promised = {
  mkdirp: nodefn.lift(mkdirp),
  readFile: nodefn.lift(fs.readFile),
  writeFile: nodefn.lift(fs.writeFile)
};

var args = jst.args(process.argv);
var minifyOptions = args.options.minifyOptions || {};
var inputExtension = args.options.inputExtension || '.html';
var outputExtension = args.options.outputExtension || '.tmp.html';

function processor(input, output) {
  return promised.readFile(input, "utf8").then(function(contents) {
    try {
      contents = minify(contents, minifyOptions);
    } catch (e) {
      throw parseError(input, contents, e);
    }
    return {
      html: contents
    };
  }).then(function(result) {
    return promised.mkdirp(path.dirname(output)).yield(result);
  }).then(function(result) {
    return promised.writeFile(output, result.html, "utf8").yield(result);
  }).then(function(result) {
    return {
      source: input,
      result: {
        filesRead: [input],
        filesWritten: [output]
      }
    };
  }).catch(function(e) {
    if (jst.isProblem(e)) {
      return e;
    } else {
      throw e;
    }
  });
}

function parseError(input, contents, err) {
  return {
    message: err,
    severity: "error",
    source: input
  };
}

jst.process({processor: processor, inExt: inputExtension, outExt: outputExtension}, args);
