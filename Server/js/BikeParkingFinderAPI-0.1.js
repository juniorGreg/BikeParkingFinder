var axios = require('axios');
var format = require('string-format');

format.extend(String.prototype, {});

function getBikeParkingPlaces(response, error, latitude, longitude, radius=5, count=10)
{
    url = "/api/bike_parking?latitude={0}&longitude={1}&radius={2}&count={3}".format(
      latitude,
      longitude,
      radius,
      count
    );
    axios.get(url).then(response).catch(error);
}

function createBikeParkingPlace()
{

}

function confirmBikeParkingPlace()
{

}

function removeBikeParkingPlace()
{

}

function modifyBikeParkingPlace()
{

}

exports.getBikeParkingPlaces = getBikeParkingPlaces;
