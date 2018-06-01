var QUnit = require("qunit");

var api = require("./BikeParkingFinderAPI-0.1");

function error(){

}



QUnit.test("Get Bike Parking Place", function(assert){
  var done = assert.async();

  var response = function(response) {
     assert.ok( true, "test resumed from async operation 1" );
     alert(JSON.stringify(response.data));
     done();
   }

   var error = function() {
      assert.notOk( true, "test resumed from async operation 1" );
      done();
   }

   api.getBikeParkingPlaces(response, error, 45.46085305860483, -73.57089042663576, 10000);


})



var css = require("./tests.css");
