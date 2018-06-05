var gulp = require('gulp');
var gutil = require('gulp-util');
var path = require('path');
var _ = require("lodash");
var browserify = require('browserify');
var browserify_css = require('browserify-css');
var sourceStream = require('vinyl-source-stream');
var fs = require("fs");
var fse = require('fs-extra');





gulp.task("default", function(){
  return browserify()
      .add('js/index.js')
      .transform(browserify_css, {
          rootDir: 'js',
          processRelativeUrl: function(relativeUrl) {
              var dest_images = "resources/images";
              var dest_fonts = "resources/fonts"
              if (_.includes(['.jpg','.png','.gif'], path.extname(relativeUrl))) {
                   var src_path =path.dirname(_.replace(relativeUrl, "../node_modules", "./node_modules"));
                   var dest_path = path.join(dest_images, path.basename(relativeUrl));
                   console.log(src_path);
                   console.log(dest_path);

                   fse.copySync(src_path, dest_images, {overwrite: false});
                   return dest_path;
               }else  if (relativeUrl.search("fonts") > 0) {
                      var src_path =path.dirname(_.replace(relativeUrl, "../node_modules", "./node_modules"));
                      var dest_path = path.join(dest_fonts, path.basename(relativeUrl));
                      console.log(src_path);
                      console.log(dest_path);

                      fse.copySync(src_path, dest_fonts, {overwrite: false});
                      return dest_path;
                  }
              console.log(relativeUrl);
              return relativeUrl;
          }
      }).bundle().pipe(fs.createWriteStream("resources/index.js"));
})

gulp.task("tests", function(){
  return browserify()
      .add('js/TestBikeParkingFinderAPI-0.1.js')
      .transform(browserify_css, {
          rootDir: 'js',
          processRelativeUrl: function(relativeUrl) {
              var dest_images = "resources/images";
              var dest_fonts = "resources/fonts"
              if (_.includes(['.jpg','.png','.gif'], path.extname(relativeUrl))) {
                   var src_path =path.dirname(_.replace(relativeUrl, "../node_modules", "./node_modules"));
                   var dest_path = path.join(dest_images, path.basename(relativeUrl));
                   console.log(src_path);
                   console.log(dest_path);

                   fse.copySync(src_path, dest_images, {overwrite: false});
                   return dest_path;
               }else  if (relativeUrl.search("fonts") > 0) {
                      var src_path =path.dirname(_.replace(relativeUrl, "../node_modules", "./node_modules"));
                      var dest_path = path.join(dest_fonts, path.basename(relativeUrl));
                      console.log(src_path);
                      console.log(dest_path);

                      fse.copySync(src_path, dest_fonts, {overwrite: false});
                      return dest_path;
                  }
              console.log(relativeUrl);
              return relativeUrl;
          }
      }).bundle().pipe(fs.createWriteStream("resources/tests.js"));
})
