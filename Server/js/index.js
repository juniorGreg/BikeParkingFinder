var Vue = require("vue");
var { LMap, LTileLayer, LMarker} = require("vue2-leaflet");

var api = require("./BikeParkingFinderAPI-0.1");

//LIconDefault.props.imagePath.default = "/resources/images";
L.Icon.Default.imagePath = "/resources/images/";

alert(L.Icon.Default.imagePath );

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
