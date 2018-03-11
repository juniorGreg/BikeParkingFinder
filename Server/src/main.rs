
#[macro_use] extern crate iron;
extern crate time;
extern crate mount;
extern crate staticfile;
extern crate router;
extern crate handlebars_iron as hbs;
extern crate iron_sessionstorage;


#[macro_use]
extern crate elastic_derive;
#[macro_use]
extern crate serde_derive;

extern crate serde;
#[macro_use]
extern crate serde_json;

extern crate elastic;

extern crate params;

use iron::prelude::*;
use iron::{status, Url};
use iron::modifiers::Redirect;
use iron::{BeforeMiddleware, AfterMiddleware, typemap, Handler};
use iron::headers::{Headers, SetCookie};

use params::{Params, Value};

use hbs::{Template, HandlebarsEngine, DirectorySource};
use hbs::handlebars::to_json;

use time::precise_time_s;

use router::Router;
use staticfile::Static;
use mount::Mount;

use std::path::Path;
use std::string::String;
use std::fmt::format;

use serde_json::value::Map;
use serde_json::Error;

use std::io::Read;

use elastic::prelude::*;
use elastic::types::*;

use iron_sessionstorage::traits::*;
use iron_sessionstorage::SessionStorage;
use iron_sessionstorage::backends::SignedCookieBackend;

mod visualCaptcha;


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



struct BikeParkingHandler;

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
}

impl BikeParkingHandler{
    fn get_geolocation_parking(&self, params: &params::Map) -> String{

        let map = params.to_strict_map::<String>().unwrap();
        let default_radius = String::from("350");
        let mut response_str = String::new();

        if let Some(coord_str) = map.get("coord")
        {
            let radius = map.get("radius").unwrap_or(&default_radius);
            let coord: Vec<f32> = serde_json::from_str(&coord_str).unwrap();
            let query = json!({
                        "query": {
                            "bool" : {
                                "must" : {
                                    "match_all" : {}
                                },
                                "filter" : {
                                    "geo_distance" : {
                                        "distance" : format!("{}m", radius),
                                        "location" : coord
                                    }
                                }
                            }
                        }
                    });

            let client = SyncClientBuilder::new().build().unwrap();

            let res = client.search::<BikeParking>().index("_all").body(query.to_string()).send().unwrap();


            let bike_parkings:Vec<BikeParking> = res.into_hits().into_iter().map(|hit| hit.into_document().unwrap()).collect();

            response_str = serde_json::to_string(&bike_parkings).unwrap();
        }else
        {
            response_str.push_str("[]");
        }

        response_str
    }
}


fn index(_: &mut Request) -> IronResult<Response> {

    let mut resp = Response::new();
    let mut data = Map::new();
    data.insert("version".to_string(), to_json(&"0.1".to_owned()));
    resp.set_mut(Template::new("index", data)).set_mut(status::Ok);

    println!("{:?}", resp);

    Ok(resp)
}

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
}


fn main() {

    //Load template engine
    let mut hbse = HandlebarsEngine::new();
    hbse.add(Box::new(DirectorySource::new("./templates/", ".hbs")));

    // load templates from all registered sources
    if let Err(r) = hbse.reload() {
        panic!("{}", r);
    }

    let bike_parking_handler = BikeParkingHandler{};

    let visual_captcha = visualCaptcha::Captcha{};

    //Routing
    let mut router = Router::new();
    router.get("/", index, "index");
    router.get("/locale/:lang", locale, "locale");
    router.get("/bike_parking/:query", bike_parking_handler, "bike_parking");
    router.get("/bike_parking/visualcaptcha/:query/*count", visual_captcha, "visual_captcha");

    //Create static file mounting
    let mut mount = Mount::new();
    mount.mount("/",router);
    mount.mount("/resources/", Static::new(Path::new("./resources/")));
    

    let my_secret = b"verysecret".to_vec();
    let mut chain = Chain::new(mount);
    chain.link_around(SessionStorage::new(SignedCookieBackend::new(my_secret)));
    chain.link_after(hbse);

    println!("oki");
    Iron::new(chain).http("localhost:3000").unwrap();
}
