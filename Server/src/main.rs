#![feature(plugin)]
#![plugin(rocket_codegen)]
#![feature(custom_derive)]

extern crate rocket;
extern crate api;

use rocket::response::NamedFile;


use std::path::{Path, PathBuf};


#[derive(FromForm)]
struct BikeParkingQuery{
    latitude: f64,
    longitude: f64,
    radius: Option<i32>,
    count: Option<i32>
}

#[get("/api/bike_parking?<query>")]
fn get_bike_parking(query: Option<BikeParkingQuery>) ->  String{
    match query{
        Some(q) => api::get_bike_parking(q.latitude, q.longitude, q.radius, q.count),
        None => String::from("error")
    }

}

#[get("/resources/<file..>")]
fn resources(file: PathBuf) -> Option<NamedFile> {
    NamedFile::open(Path::new("resources/").join(file)).ok()
}

#[get("/tests")]
fn api_tests() -> Option<NamedFile>{
    NamedFile::open(Path::new("html/tests.html")).ok()
}

#[get("/")]
fn index() -> Option<NamedFile> {
    NamedFile::open(Path::new("html/index.html")).ok()
}

fn main() {
    println!("Bike Parking Finder Server v{} is started", "0.1");
    rocket::ignite().mount("/", routes![index, api_tests, resources, get_bike_parking,]).launch();

}
