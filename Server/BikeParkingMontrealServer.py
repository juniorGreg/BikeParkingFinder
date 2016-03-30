from tornado.web import RequestHandler
from tornado.web import StaticFileHandler
from tornado.ioloop import IOLoop
from tornado.web import Application
from tornado import locale

import couchdb
import json
import math
import datetime

CIRC_EARTH = 40075016.68557849

class BikeParking():
    def __init__(self):
        self.latitude = 0.0
        self.longitude = 0.0
        self.capacity = 1
        self.status = 100
        self.id = 0
        self.added = str(datetime.datetime.now())
        self.last_update = self.added


def meter_to_deg(radius):
        return 360.0*radius/CIRC_EARTH


def is_in_radius(coord_initial, coord, radius):
    radius_sqr = math.pow(radius, 2.0)
    distance_sqr = math.pow(coord[0]-coord_initial[0], 2.0)+math.pow(coord[1]-coord_initial[1], 2.0)
    return radius_sqr > distance_sqr


class BikeParkingQueryHandler(RequestHandler):
    def initialize(self):
        couchdb.Resource.credentials =  ("supernovae", "Bonsai21")
        self.client = couchdb.Server("http://192.99.54.190:5984/")
        self.bike_parking_db = self.client["bike_parking"]

    def validate_bike_parking_modif(self, bike_parking):
        last_update_str = bike_parking["last_update"]
        last_update = datetime.datetime.strptime(last_update_str, "%Y-%m-%d %H:%M:%S.%f")
        current_time = datetime.datetime.now()
        bike_parking["last_update"] = str(current_time)
        timespan = current_time - last_update
        if timespan.total_seconds() > 60.0:
            return True
        return False

    def get_geolocation_parking(self, coord, radius):
        radius_deg = meter_to_deg(radius)
        startkey = coord[0]-radius_deg
        endkey = coord[0]+radius_deg

        geolocation = self.bike_parking_db.view("bike_parking_query/geolocation", startkey=startkey, endkey=endkey)
        bike_parking_location = map(lambda x: x.value, geolocation.rows)
        filter_bike_parking_location = filter(lambda x: is_in_radius(coord, x["coord"], radius_deg), bike_parking_location)
        return filter_bike_parking_location

    def add_bike_parking(self, coord, radius, capacity):
        if radius > 15.0:
            return {"error": "accuracy_to_low"}

        radius_deg = meter_to_deg(radius)
        startkey = coord[0]-radius_deg
        endkey = coord[0]+radius_deg

        geolocation = self.bike_parking_db.view("bike_parking_query/geolocation", startkey=startkey, endkey=endkey)
        bike_parking_location = map(lambda x: x.value, geolocation.rows)
        filter_bike_parking_location = filter(lambda x: is_in_radius(coord, x["coord"], radius_deg), bike_parking_location)

        if len(filter_bike_parking_location) == 0:
            bike_parking = BikeParking()
            bike_parking.latitude = coord[0]
            bike_parking.longitude = coord[1]
            bike_parking.capacity = capacity
            bike_parking.status = 1
            self.bike_parking_db.save(bike_parking.__dict__)
            return bike_parking.__dict__

        elif len(filter_bike_parking_location) == 1:
            _id = filter_bike_parking_location[0]["_id"]
            bike_parking = self.bike_parking_db[_id]
            status = bike_parking["status"]
            if status < 10:
                status += 1

            if self.validate_bike_parking_modif(bike_parking):
                bike_parking["status"] = status
                self.bike_parking_db.save(bike_parking)
                return bike_parking
            else:
                self.bike_parking_db.save(bike_parking)
                return {"error": "already_count"}
        else:
            return {"error": "too_many_parking_range"}

    def remove_bike_parking(self, _id):
        bike_parking = self.bike_parking_db[_id]
        status = bike_parking["status"]
        status -= 1
        response = {"status": status}
        if self.validate_bike_parking_modif(bike_parking):
            bike_parking["status"] = status
            if status <= 0:
                self.bike_parking_db.delete(bike_parking)
                response = {"status": 0}
            else:
                self.bike_parking_db.save(bike_parking)
        else:
            response = {"error": "already_count"}
            self.bike_parking_db.save(bike_parking)
        return response

    def confirm_bike_confirm(self, _id):
        bike_parking = self.bike_parking_db[_id]
        status = bike_parking["status"]

        if status < 10:
            status += 1
        else:
            return {"status": 10}

        response = {"status": status}
        if self.validate_bike_parking_modif(bike_parking):
            bike_parking["status"] = status
            response = {"status": bike_parking["status"]}
        else:
            response = {"error": "already_count"}

        self.bike_parking_db.save(bike_parking)
        return response

    def get(self, query):
        if query == "geolocation":
            coord = json.loads(self.get_argument("coord", "[45.5077, -73.544]"))
            radius = float(self.get_argument("radius", "350"))
            self.write(json.dumps(self.get_geolocation_parking(coord, radius)))

        elif query == "add_bike_parking":
            coord = json.loads(self.get_argument("coord"))
            radius = float(self.get_argument("radius"))
            capacity = int(self.get_argument("capacity", "1"))
            self.write(json.dumps(self.add_bike_parking(coord, radius, capacity)))

        elif query == "remove_bike_parking":
            _id = self.get_argument("id")
            self.write(json.dumps(self.remove_bike_parking(_id)))

        elif query == "confirm_bike_parking":
            _id = self.get_argument("id")
            self.write(json.dumps(self.confirm_bike_confirm(_id)))

        elif query == "all_locations":
            geolocation = self.bike_parking_db.view("bike_parking_query/geolocation")
            bike_parking_location = map(lambda x: x.value, geolocation.rows)
            self.write(json.dumps(bike_parking_location))

        else:
            return json.dumps({"error": "query_invalid"})

    def post(self, *args, **kwargs):
        pass


class MainHandler(RequestHandler):
    def get_user_locale(self):
        if not self.get_cookie("locale"):
            self.set_cookie("locale", "fr")
            return locale.get("fr")

        return locale.get(self.get_cookie("locale"))

    def get(self, *args, **kwargs):
        self.render("templates/index.html", version="0.6")


class LocaleHandler(RequestHandler):

    def get(self, lang):
        self.set_cookie("locale", lang)
        self.redirect("/")


if __name__ == "__main__":
    locale.load_gettext_translations('locale', 'fr')
    print "Bike Parking Mtl Server is started!"

    application = Application([
        (r"/", MainHandler),
        (r"/bike_parking/(.*)", BikeParkingQueryHandler),
        (r"/locale/(fr|en)", LocaleHandler),
        (r"/resources/(.*)", StaticFileHandler, {"path": "resources"})
    ])
    application.listen(80)
    IOLoop.instance().start()
