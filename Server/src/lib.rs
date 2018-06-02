#![feature(plugin, decl_macro, custom_derive)]

#[macro_use]
extern crate elastic_derive;
#[macro_use]
extern crate serde_derive;

extern crate serde;
#[macro_use]
extern crate serde_json;

extern crate elastic;

use serde_json::Value;
use elastic::prelude::*;


#[derive(Serialize, Deserialize, ElasticType)]
pub struct BikeParking {
    pub location: GeoPoint<DefaultGeoPointMapping>,
    pub capacity: i32,
    pub status: i32,
    pub id: String,
    pub added: Date<DefaultDateMapping<EpochMillis>>,
    pub last_update: Date<DefaultDateMapping<EpochMillis>>,
    pub comment: String,
    pub availability_stats: i32
}

#[derive(Serialize, Deserialize)]
pub struct BikeParkingAPI {
    pub id: String,
    pub latitude: f64,
    pub longitude: f64,
    pub name: String,
    pub description: String,
    pub capacity: i32,
    pub indoor: bool
}

fn bike_parking_to_api(bike_parking: BikeParking) -> BikeParkingAPI {
    let bike_parking_api = BikeParkingAPI{
        id: bike_parking.id,
        latitude: bike_parking.location.y(),
        longitude: bike_parking.location.x(),
        name: bike_parking.comment.clone(),
        description: bike_parking.comment,
        capacity: bike_parking.capacity,
        indoor: false
    };

    bike_parking_api
}


pub fn get_bike_parking(latitude: f64, longitude: f64, radius: Option<i32>, count: Option<i32>) -> String{

        let query = json!({
                    "query": {
                        "bool" : {
                            "must" : {
                                "match_all" : {}
                            },
                            "filter" : {
                                "geo_distance" : {
                                    "distance" : format!("{}m", radius.unwrap_or(350)),
                                    "location" : [ longitude, latitude ]
                                }
                            }
                        }
                    }
                });

        let client = SyncClientBuilder::new().build().unwrap();

        let res = client.search::<BikeParking>().index("_all").body(query.to_string()).send().unwrap();

        let bike_parkings:Vec<BikeParkingAPI> = res.into_hits().into_iter().map(|hit| bike_parking_to_api(hit.into_document().unwrap())).collect();

        let response = serde_json::to_string(&bike_parkings).unwrap();
        response
}

#[cfg(test)]
mod tests{
    #[macro_use]
    use super::*;

    #[test]
    fn test_get_bike_parking(){
        let response = get_bike_parking(46.0, -76.0, Some(1000), Some(10));
        assert_eq!(response, "[]");
    }

}
