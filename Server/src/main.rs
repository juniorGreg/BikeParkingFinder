extern crate iron;
extern crate time;
extern crate mount;
extern crate staticfile;
extern crate router;
extern crate handlebars_iron as hbs;

extern crate serde;
extern crate serde_json;

#[macro_use]
extern crate serde_derive;

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


const CIRC_EARTH: f32 = 40075016.68557849;

fn meter_to_deg(radius: f32) -> f32 {
    360.0*radius/CIRC_EARTH
}

fn is_in_radius(coord_initial: &Vec<f32>, coord: &Vec<f32>, radius: f32) -> bool{
    let radius_sqr = radius.powf(2.0);
    let distance_sqr = (coord[0]-coord_initial[0]).powf(2.0) + (coord[1]-coord_initial[1]).powf(2.0);
    radius_sqr > distance_sqr
}

//#[derive(Serialize, Deserialize)]
struct BikeParking {
    latitude: f32,
    longitude: f32,
    capacity: i32,
    status: i32,
    idd: i32,
    added: String,
    last_update: String,
}



struct BikeParkingHandler;

impl Handler for BikeParkingHandler{
    fn handle(&self, req: &mut Request) -> IronResult<Response> {
        let params = req.get::<Params>().unwrap();
        let ref query = req.extensions.get::<Router>().unwrap().find("query").unwrap();


        match Some(&*query.to_string()) {
            Some("geolocation") => self.get_geolocation_parking(&params),
            Some("all_locations") => println!("toute"),
            _ => println!("Invalid query",)
        }

        println!("{}", query);
        Ok(Response::with((status::Ok, "oki")))
    }
}

impl BikeParkingHandler{
    fn get_geolocation_parking(&self, params: &params::Map){

        let coord_str = params.find(&["coord"]).unwrap();

        let coord = serde_json::from_str(coord_str.from_value().unwrap()).unwrap();
        println!("{:?}", coord);
    }
}


fn index(_: &mut Request) -> IronResult<Response> {
    let mut resp = Response::new();
    let mut data = Map::new();
    data.insert("version".to_string(), to_json(&"0.1".to_owned()));
    resp.set_mut(Template::new("index", data)).set_mut(status::Ok);

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

    //Routing
    let mut router = Router::new();
    router.get("/", index, "index");
    router.get("/locale/:lang", locale, "locale");
    router.get("/bike_parking/:query", bike_parking_handler, "bike_parking");
    //router.get("/bike_parking/:query/:sub_query", bike_parking_handler.clone(), "bike_parking");

    //Create static file mounting
    let mut mount = Mount::new();
    mount.mount("/resources/", Static::new(Path::new("./resources/")));
    mount.mount("/",router);

    let mut chain = Chain::new(mount);
    chain.link_after(hbse);

    Iron::new(chain).http("localhost:3000").unwrap();
}
