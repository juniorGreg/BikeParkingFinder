var QUnit = require("qunit");

var api = require("./BikeParkingFinderAPI-0.1");

function error(){

}



QUnit.test("Get Bike Parking Place", function(assert){
  var done = assert.async();

  var response = function(response) {

     var bike_parking_places = response.data;

     assert.ok(Array.isArray(bike_parking_places), "Api returned a array.");

     if(bike_parking_places.length > 0)
     {
        var bike_parking_place= bike_parking_places[0];

        assert.ok('id' in bike_parking_place, "Object bike_parking has id property");
        assert.ok('latitude' in bike_parking_place, "Object bike_parking has latitude property");
        assert.ok('longitude' in bike_parking_place, "Object bike_parking has longitude property");
        assert.ok('name' in bike_parking_place, "Object bike_parking has name property");
        assert.ok('description' in bike_parking_place, "Object bike_parking has description property");
        assert.ok('capacity' in bike_parking_place, "Object bike_parking has capacity property");
        assert.ok('indoor' in bike_parking_place, "Object bike_parking has indoor property");
        assert.ok('indoor' in bike_parking_place, "Object bike_parking has indoor property");

        assert.ok(typeof(bike_parking_place.id) == "string", "Id property is a string");
        assert.ok(typeof(bike_parking_place.latitude) == "number", "Latitude property is a number");
        assert.ok(typeof(bike_parking_place.longitude) == "number", "Longitude property is a number");
        assert.ok(typeof(bike_parking_place.name) == "string", "Name property is a string");
        assert.ok(typeof(bike_parking_place.description) == "string", "Description property is a string");
        assert.ok(typeof(bike_parking_place.capacity) == "number", "Capacity property is a number");
        assert.ok(typeof(bike_parking_place.indoor) == "boolean", "Indoor property is a boolean");
     }

     done();
   }

   var error = function() {
      assert.notOk( true, "test resumed from async operation 1" );
      done();
   }

   api.getBikeParkingPlaces(response, error, 45.46085305860483, -73.57089042663576, 10000);


})

QUnit.test("Create Bike Parking Place", function(assert){
  var done = assert.async();

  var response = function(response) {

     var bike_parking_places = response.data;

     assert.ok(Array.isArray(bike_parking_places), "Api returned a array.");

     if(bike_parking_places.length > 0)
     {
        var bike_parking_place= bike_parking_places[0];

        assert.ok('id' in bike_parking_place, "Object bike_parking has id property");
        assert.ok('latitude' in bike_parking_place, "Object bike_parking has latitude property");
        assert.ok('longitude' in bike_parking_place, "Object bike_parking has longitude property");
        assert.ok('name' in bike_parking_place, "Object bike_parking has name property");
        assert.ok('description' in bike_parking_place, "Object bike_parking has description property");
        assert.ok('capacity' in bike_parking_place, "Object bike_parking has capacity property");
        assert.ok('indoor' in bike_parking_place, "Object bike_parking has indoor property");
        assert.ok('indoor' in bike_parking_place, "Object bike_parking has indoor property");

        assert.ok(typeof(bike_parking_place.id) == "string", "Id property is a string");
        assert.ok(typeof(bike_parking_place.latitude) == "number", "Latitude property is a number");
        assert.ok(typeof(bike_parking_place.longitude) == "number", "Longitude property is a number");
        assert.ok(typeof(bike_parking_place.name) == "string", "Name property is a string");
        assert.ok(typeof(bike_parking_place.description) == "string", "Description property is a string");
        assert.ok(typeof(bike_parking_place.capacity) == "number", "Capacity property is a number");
        assert.ok(typeof(bike_parking_place.indoor) == "boolean", "Indoor property is a boolean");
     }

     done();
   }

   var error = function() {
      assert.notOk( true, "test resumed from async operation 1" );
      done();
   }

   var bike_parking_place = {

   }

   api.createBikeParkingPlaces(response, error, bike_parking_place);


})



var css = require("./tests.css");
