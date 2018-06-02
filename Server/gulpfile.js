var gulp = require('gulp');
var gutil = require('gulp-util');
var path = require('path');
var _ = require("lodash");
var browserify = require('browserify');
var browserify_css = require('browserify-css');
var sourceStream = require('vinyl-source-stream');
var fs = require("fs");





gulp.task("default", function(){
  return browserify()
      .add('js/index.js')
      .transform(browserify_css, {
          rootDir: 'js',
          inlineImages: true          
      }).bundle().pipe(fs.createWriteStream("resources/index.js"));
})
