#![feature(plugin, decl_macro, custom_derive)]
#![plugin(rocket_codegen)]

extern crate rocket_contrib;
extern crate rocket;

#[macro_use]
extern crate elastic_derive;
#[macro_use]
extern crate serde_derive;

extern crate serde;
#[macro_use]
extern crate serde_json;

extern crate elastic;



use std::io;
use std::path::{Path, PathBuf};
use std::string::String;
use std::fmt::format;

use serde_json::value::Map;
use serde_json::Error;

use elastic::prelude::*;
use elastic::types::*;

use rocket_contrib::Template;
use rocket::response::NamedFile;



//mod visualCaptcha;


#[derive(ElasticType, Serialize, Deserialize)]
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

#[derive(Serialize)]
struct TemplateContext {
    version: String
}

#[derive(FromForm)]
struct GeoCoord{
    lat: f32,
    long: f32,
    radius: Option<i32>
}


//struct BikeParkingHandler;
/*
impl Handler for BikeParkingHandler{
    fn handle(&self, req: &mut Request) -> IronResult<Response> {
        let params = req.get::<Params>().unwrap();
        let ref query = req.extensions.get::<Router>().unwrap().find("query").unwrap();

        println!("{:?}", query);

        let response = match Some(&*query.to_string()) {
            Some("geolocation") => self.get_geolocation_parking(&params).to_string(),
            Some("all_locations") => String::from("[]"),
            _ => String::from("[]")
        };


        Ok(Response::with((status::Ok, response)))
    }
}*/

#[get("/bike_parking/geolocation?<geoCoord>")]
fn get_geolocation_parking(geoCoord: GeoCoord) -> String{

    let query = json!({
                "query": {
                    "bool" : {
                        "must" : {
                            "match_all" : {}
                        },
                        "filter" : {
                            "geo_distance" : {
                                "distance" : format!("{}m", geoCoord.radius.unwrap_or(350)),
                                "location" : [ geoCoord.long, geoCoord.lat ]
                            }
                        }
                    }
                }
            });

    let client = SyncClientBuilder::new().build().unwrap();

    let res = client.search::<BikeParking>().index("_all").body(query.to_string()).send().unwrap();


    let bike_parkings:Vec<BikeParking> = res.into_hits().into_iter().map(|hit| hit.into_document().unwrap()).collect();

    let response_str = serde_json::to_string(&bike_parkings).unwrap();


    response_str
}


#[get("/")]
fn index() -> Template {

    /*let mut resp = Response::new();
    let mut data = Map::new();
    data.insert("version".to_string(), to_json(&"0.1".to_owned()));*/
    let context = TemplateContext{
        version: String::from("0.1")
    };

    Template::render("index", &context)
}

#[get("/resources/<file..>")]
fn files(file: PathBuf) -> Option<NamedFile>{
    NamedFile::open(Path::new("resources/").join(file)).ok()
}

/*
fn locale(req: &mut Request) -> IronResult<Response> {
    let ref lang = req.extensions.get::<Router>().unwrap().find("lang").unwrap_or("en");

    let cookie = vec![
            String::from(format!("locale={}; Path=/;", lang))
        ];

    let host = req.url.host();
    let port =req.url.port();
    let scheme = req.url.scheme();

    let base_url = format!("{}://{}:{}/", scheme, host, port);
    let url = Url::parse(&base_url.to_string()).unwrap();
    let mut response = Response::with((status::Found, Redirect(url.clone())));
    response.headers.set(SetCookie(cookie));

    println!("{}", lang);


    Ok(response)
}*/


fn main() {
    rocket::ignite()
        .mount("/", routes![index, files, get_geolocation_parking])
        .attach(Template::fairing()).launch();
}
