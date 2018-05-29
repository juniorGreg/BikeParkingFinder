var QUnit = require("qunit");

var api = require("./BikeParkingFinderAPI-0.1");

function error(){

}



QUnit.test("Get Bike Parking Place", function(assert){
  var done = assert.async();

  var response = function() {
     assert.ok( true, "test resumed from async operation 1" );
     done();
   }

   var error = function() {
      assert.notOk( true, "test resumed from async operation 1" );
      done();
   }

   api.getBikeParkingPlaces(response, error, 45.00000, -76.0000);


})



var css = require("./tests.css");
