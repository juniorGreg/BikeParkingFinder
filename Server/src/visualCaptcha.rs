


struct Session{

}

pub struct Captcha{
    imageOptions: vec<String>,
    audioOptions: vec<String>
}


impl Captcha {

    pub fn new() -> Captcha {

    }

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
