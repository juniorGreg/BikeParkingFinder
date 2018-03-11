use router::Router;
use iron::prelude::*;
use iron::{status, Url, Handler};
use params::{Params, Value};


struct Session{

}

pub struct Captcha;

impl Handler for Captcha{
    fn handle(&self, req: &mut Request) -> IronResult<Response> {
        let params = req.get::<Params>().unwrap();
        let ref query = req.extensions.get::<Router>().unwrap().find("query").unwrap();
        let count: i32 = req.extensions.get::<Router>().unwrap().find("count").unwrap_or("0").parse().unwrap();

        println!("{:?}", count);


        let response = match Some(&*query.to_string()) {
            Some("start") => self.start(&count).to_string(),
            Some("image") => String::from("[]"),
            Some("audio") => String::from("[]"),
            Some("try") => String::from("[]"),
            _ => String::from("[]")
        };


        Ok(Response::with((status::Ok, "response")))
    }
}

impl Captcha {

    fn start(&self, count: & i32) -> String{
            println!("{:?}", count);
            String::from("start")
    }

    fn image(&self){

    }

    fn audio(&self){

    }

    fn try(&self){

    }



}
