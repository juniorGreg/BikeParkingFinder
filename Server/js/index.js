var Vue = require("vue");
var { LMap, LTileLayer, LMarker, LCircle} = require("vue2-leaflet");

var api = require("./BikeParkingFinderAPI-0.1");
var locales = require("./locales");
const i18n = locales.i18n;

//LIconDefault.props.imagePath.default = "/resources/images";
//L.Icon.Default.imagePath = "/resources/images/";


var bikeIcon = L.icon({
    iconUrl: 'resources/images/ic_directions_bike_grey600_24dp.png',
    iconRetinaUrl: 'resources/images/ic_directions_bike_grey600_48dp.png',
    iconSize: [48, 48],

    popupAnchor: [0, -20],
    shadowUrl: 'resources/images/bike-shadow.png',
    shadowRetinaUrl: 'resources/images/marker-shadow.png',
    shadowSize: [90, 24],
    shadowAnchor: [35, 5]

});

var LocaleSelector = Vue.component('locale_selector', {

    props: ["current_locale", "global_locale"],

    methods: {
      update_locale: function(){
          this.$emit('update_locale', this.current_locale);
      }
    },

    template:`
        <div class="navbar-item" @click="update_locale">
            <span v-if="current_locale === global_locale" class="icon">
                <i class="fa fa-check"></i>
            </span>
          {{current_locale}}
      </div>
      `
});




var app = new Vue({
  i18n,
  el: '#app',
  components: { LMap, LTileLayer, LMarker, LCircle, LocaleSelector},
  data: {
    url:'http://{s}.tile.osm.org/{z}/{x}/{y}.png',
    attribution:'&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
    bike_parking_places: [],
    bike_icon: bikeIcon,
    radius: 350,
    center:[45.46085305860483, -73.57089042663576],
    bike_position: [45.46085305860483, -73.57089042663576]

  },



  mounted: function(){
      this.get_current_position();
  },

  methods:{

    on_locale_updated: function(event){
       i18n.locale = event;
    },


    update_bike_parking_place: function(response){
      this.bike_parking_places = response.data;
      //alert(JSON.stringify(response.data));
    },

    callback_error: function(error){

    },
    get_current_position: function(){
        if(navigator.geolocation){
            navigator.geolocation.getCurrentPosition(
              function(position){
                api.getBikeParkingPlaces(this.update_bike_parking_place,
                  this.callback_error,
                  position.coords.latitude,
                  position.coords.longitude,
                  this.radius);
                  //alert("okidoo");
              }
            );
        }
    },

    get_bike_parking: function(event){

      let latitude = event.latlng.lat;
      let longitude = event.latlng.lng;
      this.center = event.latlng;
      this.bike_position = [latitude, longitude];

      api.getBikeParkingPlaces(this.update_bike_parking_place, this.callback_error, latitude, longitude, this.radius);
    },

    get_location: function(bike_parking_place){
      //alert(bike_parking_place.latitude);
      return [bike_parking_place.latitude, bike_parking_place.longitude];
    }
  }
});

var css = require("./index.css");
