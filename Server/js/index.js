var Vue = require("vue");
//var App = require('../html/index.html')
var { LMap, LTileLayer, LMarker, LIconDefault } = require("vue2-leaflet");

LIconDefault.props.imagePath.default = "/resources/images";

alert(LIconDefault.props.imagePath.default);

var app = new Vue({
  el: '#app',
  components: { LMap, LTileLayer, LMarker },
  data: {
    url:'http://{s}.tile.osm.org/{z}/{x}/{y}.png',
    attribution:'&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
    marker: L.latLng(47.413220, -1.219482)
  }
});

var css = require("./index.css");
