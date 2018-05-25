#![feature(plugin)]
#![plugin(rocket_codegen)]


extern crate rocket;

use rocket::response::NamedFile;
use std::path::{Path, PathBuf};

#[get("/resources/<file..>")]
fn resources(file: PathBuf) -> Option<NamedFile> {
    NamedFile::open(Path::new("resources/").join(file)).ok()
}

#[get("/tests")]
fn apiTests() -> Option<NamedFile>{
    NamedFile::open(Path::new("html/tests.html")).ok()
}

#[get("/")]
fn index() -> Option<NamedFile> {
    NamedFile::open(Path::new("html/index.html")).ok()
}

fn main() {
    println!("Bike Parking Finder Server v{} is started", "0.1");
    rocket::ignite().mount("/", routes![index, apiTests, resources]).launch();

}
